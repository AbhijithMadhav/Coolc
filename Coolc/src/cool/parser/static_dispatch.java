/**
 * 
 */
package cool.parser;

import cool.common.AbstractSymbol;
import cool.common.PossibleNullDereferenceException;
import cool.common.TreeConstants;
import cool.common.UnresolvableSelfTypeException;
import cool.common.Utilities;

import cool.semant.ObjectTypeEnvironment;
import cool.semant.SemantError;
import cool.semant.SemantUtil;

import cool.cgen.CgenLookupTable;
import cool.cgen.CgenSupport;
import cool.cgen.StringSymbol;

import java.io.PrintStream;
import java.util.Vector;

/**
 * Defines AST constructor 'static_dispatch'.
 * <p>
 * See <a href="TreeNode.html">TreeNode</a> for full documentation.
 */
public class static_dispatch extends Expression
{
	private Expression expr;
	private AbstractSymbol type_name;
	private AbstractSymbol name;
	private Expressions actual;

	/**
	 * Creates "static_dispatch" AST node.
	 * 
	 * @param lineNumber the line in the source file from which this node came.
	 * @param a0 initial value for expr
	 * @param a1 initial value for type_name
	 * @param a2 initial value for name
	 * @param a3 initial value for actual
	 */
	public static_dispatch(int lineNumber, Expression a1, AbstractSymbol a2,
			AbstractSymbol a3, Expressions a4)
	{
		super(lineNumber);
		expr = a1;
		type_name = a2;
		name = a3;
		actual = a4;
	}

	public TreeNode copy()
	{
		return new static_dispatch(lineNumber, (Expression) expr.copy(),
				copy_AbstractSymbol(type_name), copy_AbstractSymbol(name),
				(Expressions) actual.copy());
	}

	public void dump(PrintStream out, int n)
	{
		out.print(Utilities.pad(n) + "static_dispatch\n");
		expr.dump(out, n + 2);
		dump_AbstractSymbol(out, n + 2, type_name);
		dump_AbstractSymbol(out, n + 2, name);
		actual.dump(out, n + 2);
	}

	public void dump_with_types(PrintStream out, int n)
	{
		dump_line(out, n);
		out.println(Utilities.pad(n) + "_static_dispatch");
		expr.dump_with_types(out, n + 2);
		dump_AbstractSymbol(out, n + 2, type_name);
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
		// Validate the invoking expression
		expr.validateAndSetType(o, m, c, err);

		// Set default type of dispatch in case of an error
		set_type(TreeConstants.Object_);

		// Validate the actual arguments
		for (TreeNode t : actual.getElements())
			((Expression) t).validateAndSetType(o, m, c, err);

		// Type Checking

		// Get types of actual arguments
		Vector<AbstractSymbol> tActual = new Vector<AbstractSymbol>();
		for (TreeNode t : actual.getElements())
			tActual.add(SemantUtil.getResolvedType(((Expression) t).get_type(),
					c));
		// getResolvedType for <expr>@<type_name>.<name>(..., self, ...)

		// static type qualifier cannot be SELF
		if (type_name.equals(TreeConstants.SELF_TYPE))
		{
			err.semantError(c).println("Static dispatch to SELF_TYPE.");
			return;
		}

		// is the invoking expression a descendent of the static type qualifier?
		if (!o.getSuperTypes(SemantUtil.getResolvedType(expr.get_type(), c))
				.contains(type_name))
		{
			// getResolvedType for self@<type_name>.<name>(..., <actual>, ...)
			err.semantError(c)
					.println(
							"Expression type "
									+ expr.get_type()
									+ " does not conform to declared static dispatch type "
									+ type_name + ".");
			return;
		}

		// is the method a member of the class of the static type qualifier
		if (!o.toClass_(type_name).containsMethod(name, o, err))
		{
			err.semantError(c).println(
					"Static dispatch to undefined method " + name + ".");
			return;
		}

		// If the invocation of the method is valid proceed with
		// 1. Validating the number and type of arguments
		// 2. Setting the type of the static dispatch

		// Get types of formal arguments
		Formals formals = o.toClass_(type_name).getMethod(name, o, err)
				.getFormals();
		Vector<AbstractSymbol> tFormal = new Vector<AbstractSymbol>();
		Vector<AbstractSymbol> paramName = new Vector<AbstractSymbol>();
		for (TreeNode t : formals.getElements())
		{
			formal param = ((formal) t);
			tFormal.add(param.getTypeDecl());
			paramName.add(param.getName());
		}

		// wrong number of arguments
		if (tActual.size() != tFormal.size())
		{
			err.semantError(c).println(
					"Method " + name
							+ " called with wrong number of arguments.");
			return;
		}

		// non-conforming type of arguments
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
		method meth = o.toClass_(type_name).getMethod(name, o, err);
		if (meth.getReturnType().equals(TreeConstants.SELF_TYPE))
			set_type(expr.get_type());
		else
			set_type(meth.getReturnType());
	}

	/**
	 * Generates code for the 'static dispatch' expression
	 * 
	 * @param s the output stream
	 * */
	public int code(class_ c, int nAR, CgenLookupTable tbl, PrintStream s)
	{
		int voidLabel = CgenSupport.getLabel();
		int endLabel = CgenSupport.getLabel();

		s.println("# Static Method dispatch\n");

		s.println("# Save FP");
		nAR = CgenSupport.emitPush(CgenSupport.FP, nAR, s);
		s.println();

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

		int nItems = 0;

		{
			s.println("# Test if invoking object is void");
			nAR = CgenSupport.emitPush(CgenSupport.ACC, nAR, s);
			CgenSupport.emitFetchClassTag(CgenSupport.ACC, CgenSupport.ACC, s);
			CgenSupport.emitLoadImm(CgenSupport.T1, CgenSupport.VOID_CLASSTAG,
					s);
			nItems = nAR;
			CgenSupport.emitBeq(CgenSupport.ACC, CgenSupport.T1, voidLabel, s);
			nAR = CgenSupport.emitPop(CgenSupport.ACC, nAR, s);
		}

		s.println("# Get reference to dispatch Table");
		CgenSupport.emitFetchDispTab(CgenSupport.T1, CgenSupport.ACC, s);

		// To do :
		CgenSupport.emitLoad(CgenSupport.T1,
				tbl.getMethodOffset(type_name, name), CgenSupport.T1, s);
		s.println();

		s.println("# Dispatch");
		CgenSupport.emitJalr(CgenSupport.T1, s);
		s.println();

		// The arguments of this AR will be popped by the
		// callee
		nAR -= actual.getLength();
		s.println("# Restore FP");
		nAR = CgenSupport.emitPop(CgenSupport.FP, nAR, s);
		s.println();
		CgenSupport.emitBranch(endLabel, s);

		nAR = nItems - actual.getLength();
		{
			CgenSupport.emitLabelDef(voidLabel, s);
			nAR = CgenSupport.emitPop(CgenSupport.ACC, nAR, s);
			CgenSupport.emitLoadString(CgenSupport.ACC,
					(StringSymbol) c.getFilename(), s);
			CgenSupport.emitLoadImm(CgenSupport.T1, 1, s);
			CgenSupport.emitJal("_dispatch_abort", s);
			nAR = CgenSupport.emitPop(CgenSupport.FP, nAR, s);
		}

		CgenSupport.emitLabelDef(endLabel, s);
		return nAR;
	}

}
