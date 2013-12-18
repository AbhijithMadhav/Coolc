/**
 * 
 */
package cool.parser;

import cool.common.AbstractSymbol;
import cool.common.PossibleNullDereferenceException;
import cool.common.TreeConstants;
import cool.common.UnresolvableSelfTypeException;
import cool.common.Utilities;

import cool.semant.CNode;
import cool.semant.ObjectTypeEnvironment;
import cool.semant.SemantError;
import cool.semant.SemantUtil;

import cool.cgen.CgenLookupTable;
import cool.cgen.CgenSupport;
import cool.cgen.DispatchRecord;
import cool.cgen.StringSymbol;

import java.io.PrintStream;
import java.util.LinkedList;
import java.util.Vector;

/**
 * Defines AST constructor 'dispatch'.
 * <p>
 * See <a href="TreeNode.html">TreeNode</a> for full documentation.
 */
public class dispatch extends Expression
{
	private Expression expr;
	private AbstractSymbol name;
	private Expressions actual;

	/**
	 * Creates "dispatch" AST node.
	 * 
	 * @param lineNumber the line in the source file from which this node came.
	 * @param a0 initial value for expr
	 * @param a1 initial value for name
	 * @param a2 initial value for actual
	 */
	public dispatch(int lineNumber, Expression a1, AbstractSymbol a2,
			Expressions a3)
	{
		super(lineNumber);
		expr = a1;
		name = a2;
		actual = a3;
	}

	public TreeNode copy()
	{
		return new dispatch(lineNumber, (Expression) expr.copy(),
				copy_AbstractSymbol(name), (Expressions) actual.copy());
	}

	public void dump(PrintStream out, int n)
	{
		out.print(Utilities.pad(n) + "dispatch\n");
		expr.dump(out, n + 2);
		dump_AbstractSymbol(out, n + 2, name);
		actual.dump(out, n + 2);
	}

	public void dump_with_types(PrintStream out, int n)
	{
		dump_line(out, n);
		out.println(Utilities.pad(n) + "_dispatch");
		expr.dump_with_types(out, n + 2);
		dump_AbstractSymbol(out, n + 2, name);
		out.println(Utilities.pad(n + 2) + "(");
		for (TreeNode t : actual.getElements())
			((Expression) t).dump_with_types(out, n + 2);
		out.println(Utilities.pad(n + 2) + ")");
		dump_type(out, n);
	}

	public void validateAndSetType(ObjectTypeEnvironment o, method m, class_ c,
			SemantError err) throws PossibleNullDereferenceException,
			UnresolvableSelfTypeException
	{
		// validate the invoking expression
		expr.validateAndSetType(o, m, c, err);

		// Set default type of dispatch in case of an error
		set_type(TreeConstants.Object_);

		// Validate each of the actual arguments
		for (TreeNode t : actual.getElements())
			((Expression) t).validateAndSetType(o, m, c, err);

		// Type Checking

		// Get types of actual arguments
		Vector<AbstractSymbol> tActual = new Vector<AbstractSymbol>();
		for (TreeNode t : actual.getElements())
			tActual.add(SemantUtil.getResolvedType(((Expression) t).get_type(),
					c));
		// getResolvedType for <expr>.<name>(..., self, ...)

		// is the method a member of the class any of the supertypes of the of
		// the invoking object class
		if (!o.toClass_(SemantUtil.getResolvedType(expr.get_type(), c))
				.containsMethod(name, o, err))
		{
			// getResolvedType for self.<name>(...)
			err.semantError(c).println(
					"Dispatch to undefined method " + name + ".");
			return;
		}

		// If the invocation of the method is valid proceed with
		// 1. Validating the number and type of arguments
		// 2. Setting the type of the static dispatch

		// Get types of formal arguments. getResolvedType for self.<name>(...)
		Formals formals = o
				.toClass_(SemantUtil.getResolvedType(expr.get_type(), c))
				.getMethod(name, o, err).getFormals();
		Vector<AbstractSymbol> tFormal = new Vector<AbstractSymbol>();
		Vector<AbstractSymbol> paramName = new Vector<AbstractSymbol>();
		for (TreeNode t : formals.getElements())
		{
			formal param = ((formal) t);
			tFormal.add(param.getTypeDecl());
			paramName.add(param.getName());
		}
		// Wrong number of arguments
		if (tActual.size() != tFormal.size())
		{
			err.semantError(c).println(
					"Method " + name
							+ " called with wrong number of arguments.");
			return;
		}
		// Non conforming type of arguments
		for (int i = 0; i < tActual.size(); i++)
		{
			if (!o.getSuperTypes(tActual.get(i)).contains(tFormal.get(i)))
			{
				err.semantError(c).println(
						"In call of method " + name + ", type "
								+ tActual.get(i) + " of parameter "
								+ paramName.get(i)
								+ " does not conform to declared type "
								+ tFormal.get(i) + ".");
				return;
			}
		}

		// All is fine. Set the type information
		// getResolvedType for self.<name>(...)
		method meth = o
				.toClass_(SemantUtil.getResolvedType(expr.get_type(), c))
				.getMethod(name, o, err);
		if (meth.getReturnType().equals(TreeConstants.SELF_TYPE))
			set_type(expr.get_type());
		else
			set_type(meth.getReturnType());
	} // void validateAndSetType(...)

	public CNode getNode(CNode root, AbstractSymbol name)
	{
		LinkedList<CNode> q = new LinkedList<CNode>();

		q.add(root);

		while (!q.isEmpty())
		{
			CNode c = q.remove();
			if (c.getName().equals(name))
				return c;
			for (CNode t : c.getChildren())
				q.add((CNode) t);
		}
		return null;
	}

	/**
	 * Generates code for the 'dynamic dispatch' expression
	 * 
	 * @param s the output stream
	 * */
	public int code(class_ c, int nAR, CgenLookupTable tbl, PrintStream s)
	{
		int notVoidLabel = CgenSupport.getLabel();

		s.println("# Method dispatch\n");

		s.println("# Save FP");
		nAR = CgenSupport.emitPush(CgenSupport.FP, nAR, s);
		s.println();

		s.println("# Push Arguments");
		for (TreeNode t : actual.getElements())
		{
			s.println("# Save self");
			nAR = CgenSupport.emitPush(CgenSupport.ACC, nAR, s);
			s.println();

			Expression ex = (Expression) t;
			s.println("# Evaluate actual argument and get it in in $a0");
			nAR = ex.code(c, nAR, tbl, s);
			s.println();

			s.println("# Temporarily cache evaluated argument");
			CgenSupport.emitMove(CgenSupport.T1, CgenSupport.ACC, s);
			s.println();

			s.println("# Restore self");
			nAR = CgenSupport.emitPop(CgenSupport.ACC, nAR, s);
			s.println();

			s.println("# Push argument in Stack");
			nAR = CgenSupport.emitPush(CgenSupport.T1, nAR, s);
			s.println();
		}

		s.println("# Evaluate invoking object and Get it in $a0");
		nAR = expr.code(c, nAR, tbl, s);
		s.println();

		s.println("# Save it");
		nAR = CgenSupport.emitPush(CgenSupport.ACC, nAR, s);
		s.println();

		s.println("# Need to check if the invoking object is void");
		s.println("# Fetch its classtag");
		CgenSupport.emitFetchClassTag(CgenSupport.ACC, CgenSupport.ACC, s);
		s.println("# Get the classtag of void");
		CgenSupport.emitLoadImm(CgenSupport.T1, CgenSupport.VOID_CLASSTAG, s);
		s.println("# The test. Is the invoking object not void?");
		s.println("# If yes, branch to label" + notVoidLabel);
		CgenSupport.emitBne(CgenSupport.ACC, CgenSupport.T1, notVoidLabel, s);
		s.println("# Else, invoking object is void.");

		s.println("# Void ");
		int nItems = nAR; // Incomplete : Causes infinite looping in GC
		nAR = CgenSupport.emitPop(CgenSupport.ACC, nAR, s);
		CgenSupport.emitLoadString(CgenSupport.ACC,
				(StringSymbol) c.getFilename(), s);
		CgenSupport.emitLoadImm(CgenSupport.T1, 1, s);
		CgenSupport.emitJal("_dispatch_abort", s);
		nAR = CgenSupport.emitPop(CgenSupport.FP, nAR, s);
		nAR = nItems; // Incomplete: Causes infinite looping in GC

		s.println("# Not void");
		CgenSupport.emitLabelDef(notVoidLabel, s);
		s.println("# Restore the invoking object");
		nAR = CgenSupport.emitPop(CgenSupport.ACC, nAR, s);
		s.println();
		s.println("# Determine the dispatch based on the "
				+ "type of the self object");
		s.println();
		s.println("# Save the invoking object");
		nAR = CgenSupport.emitPush(CgenSupport.ACC, nAR, s);
		s.println("# Get the class tag of the invoking object");
		CgenSupport.emitFetchClassTag(CgenSupport.ACC, CgenSupport.ACC, s);
		Integer label[] = new Integer[tbl.nDispatchCandidates(name)];
		int k = 0;
		for (DispatchRecord d : tbl.getDispatchCandidates(name))
		{
			s.println("# Class tag : " + d.getClassTag());
			CgenSupport.emitLoadImm(CgenSupport.T1, d.getClassTag(), s);
			label[k] = CgenSupport.getLabel();
			CgenSupport.emitBeq(CgenSupport.ACC, CgenSupport.T1, label[k], s);
			k++;
		}
		s.println();

		nItems = nAR;
		int dispatchLabel = CgenSupport.getLabel();
		int j = 0;
		for (DispatchRecord d : tbl.getDispatchCandidates(name))
		{
			nAR = nItems;
			// s.println("# Dispatch for " + d.getClass_().getName() + "." +
			// name);
			CgenSupport.emitLabelDef(label[j], s);
			s.println("# Restore the invoking object");
			nAR = CgenSupport.emitPop(CgenSupport.ACC, nAR, s);
			s.println("# Dispatch table of invoking object");
			CgenSupport.emitFetchDispTab(CgenSupport.T1, CgenSupport.ACC, s);
			s.println("# Address of dynamic dispatch");
			CgenSupport.emitLoad(CgenSupport.T1, d.getOffset(), CgenSupport.T1,
					s);
			CgenSupport.emitBranch(dispatchLabel, s);
			j++;
		}
		CgenSupport.emitLabelDef(dispatchLabel, s);

		s.println("# Dispatch");
		CgenSupport.emitJalr(CgenSupport.T1, s);
		s.println();

		// The arguments of this AR will be popped by the
		// callee
		nAR -= actual.getLength();
		s.println("# Restore FP");
		nAR = CgenSupport.emitPop(CgenSupport.FP, nAR, s);
		s.println();
		return nAR;
	}
}