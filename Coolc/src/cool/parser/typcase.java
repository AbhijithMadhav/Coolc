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
import java.util.Collection;
import java.util.Vector;

/**
 * Defines AST constructor 'typcase'.
 * <p>
 * See <a href="TreeNode.html">TreeNode</a> for full documentation.
 */
public class typcase extends Expression
{
	private Expression expr;
	private Cases cases;

	/**
	 * Creates "typcase" AST node.
	 * 
	 * @param lineNumber the line in the source file from which this node came.
	 * @param a0 initial value for expr
	 * @param a1 initial value for cases
	 */
	public typcase(int lineNumber, Expression a1, Cases a2)
	{
		super(lineNumber);
		expr = a1;
		cases = a2;
	}

	public TreeNode copy()
	{
		return new typcase(lineNumber, (Expression) expr.copy(),
				(Cases) cases.copy());
	}

	public void dump(PrintStream out, int n)
	{
		out.print(Utilities.pad(n) + "typcase\n");
		expr.dump(out, n + 2);
		cases.dump(out, n + 2);
	}

	public void dump_with_types(PrintStream out, int n)
	{
		dump_line(out, n);
		out.println(Utilities.pad(n) + "_typcase");
		expr.dump_with_types(out, n + 2);
		for (TreeNode t : cases.getElements())
			((Case) t).dump_with_types(out, n + 2);
		dump_type(out, n);
	}

	public void validateAndSetType(ObjectTypeEnvironment o, method m, class_ c,
			SemantError err) throws PossibleNullDereferenceException,
			UnresolvableSelfTypeException
	{
		// Validate the expression of the typcase
		expr.validateAndSetType(o, m, c, err);

		// Validate every branch
		AbstractSymbol lub = TreeConstants.No_type;
		Vector<AbstractSymbol> bTypes = new Vector<AbstractSymbol>();
		for (TreeNode t : cases.getElements())
		{
			branch b = (branch) t;
			b.validate(o, m, c, err);
			if (bTypes.contains(b.getTypeDecl())) // Duplicate branch
				err.semantError(c).println(
						"Duplicate branch " + b.getTypeDecl()
								+ " in case statement.");
			else
				bTypes.add(b.getTypeDecl());

			// type of typecase is the lub of all the types of the expressions
			// of its branches
			// getResolvedType for 'case x of y : SELF_TYPE => self ; z : Int =>
			// 1 ; esac'
			// Even though the first branch is illegal the semant proceeds
			// further needing a resolved type
			lub = o.lub(SemantUtil.getResolvedType(lub, c),
					SemantUtil.getResolvedType(b.getExpr().get_type(), c));
		}
		set_type(lub);
	}

	/**
	 * Generates code for the typcase expression.
	 * 
	 * @param s the output stream
	 * */
	public int code(class_ c, int nAR, CgenLookupTable tbl, PrintStream s)
	{
		int notVoidLabel = CgenSupport.getLabel();

		s.println("# Case expression");
		s.println();

		s.println("# Save self. Needed while evaluating the LUB branch");
		nAR = CgenSupport.emitPush(CgenSupport.ACC, nAR, s);
		s.println();

		s.println("# Evaluate the case expression");
		nAR = expr.code(c, nAR, tbl, s);

		s.println("# Need to check if the evaluated expression is void");
		s.println();

		s.println("# Save it");
		nAR = CgenSupport.emitPush(CgenSupport.ACC, nAR, s);
		s.println("# Fetch its classtag");
		CgenSupport.emitFetchClassTag(CgenSupport.ACC, CgenSupport.ACC, s);
		s.println("# Get the classtag of void");
		CgenSupport.emitLoadImm(CgenSupport.T1, CgenSupport.VOID_CLASSTAG, s);
		s.println("# The test. Is the evaluated expression not void?");
		s.println("# If yes, branch to label" + notVoidLabel);
		CgenSupport.emitBne(CgenSupport.ACC, CgenSupport.T1, notVoidLabel, s);
		s.println("# Else, evaluated expression is void.");
		CgenSupport.emitLoadString(CgenSupport.ACC,
				(StringSymbol) c.getFilename(), s);
		CgenSupport.emitLoadImm(CgenSupport.T1, 1, s);
		CgenSupport.emitJal("_case_abort2", s);
		s.println();

		CgenSupport.emitLabelDef(notVoidLabel, s);

		s.println("# Restore the evaluated expression");
		nAR = CgenSupport.emitPop(CgenSupport.ACC, nAR, s);
		s.println("# Save the evaluated value.");
		s.println("# Will need to bind it to the identifier of one of the branches");
		nAR = CgenSupport.emitPush(CgenSupport.ACC, nAR, s);
		s.println("# Class tag of the case expression");
		CgenSupport.emitFetchClassTag(CgenSupport.ACC, CgenSupport.ACC, s);

		// Code to jump to a particular branch of execution based on the
		// dynamic type of the case-expression
		// Once this is generated, we can generate code to find out
		// which type among all case-branches is the LUB of the dynamic type.
		int typeLabel[] = new int[tbl.getCaseTableKeySet().size()];
		int n = 0;
		for (int classTag : tbl.getCaseTableKeySet())
		{
			CgenSupport.emitLoadImm(CgenSupport.T1, classTag, s);
			typeLabel[n] = CgenSupport.getLabel();
			CgenSupport.emitBeq(CgenSupport.ACC, CgenSupport.T1, typeLabel[n],
					s);
			n++;
		}
		// Incomplete
		// CgenSupport.emitAbort();

		// Code to get the LUB of the dynamic type of the case expression
		int branchLabel[] = new int[cases.getLength()];
		for (int i = 0; i < cases.getLength(); i++)
			branchLabel[i] = CgenSupport.getLabel();
		n = 0;
		for (Collection<Integer> lst : tbl.getCaseTableValues())
		{
			CgenSupport.emitLabelDef(typeLabel[n], s);

			for (int classTag : lst)
			{
				CgenSupport.emitLoadImm(CgenSupport.ACC, classTag, s);
				int m = 0;
				for (TreeNode t : cases.getElements())
				{
					branch b = (branch) t;
					CgenSupport.emitLoadImm(CgenSupport.T1,
							tbl.getClassTag(b.getTypeDecl()), s);
					CgenSupport.emitBeq(CgenSupport.ACC, CgenSupport.T1,
							branchLabel[m++], s);
				}

			}

			int nItems = nAR;
			nAR = CgenSupport.emitPop(CgenSupport.ACC, nAR, s);
			nAR = CgenSupport.emitPop(CgenSupport.ACC, nAR, s);
			CgenSupport.emitJal("_case_abort", s);
			nAR = nItems;

			n++;
		}

		// Code for each branch of the case expression
		int endLabel = CgenSupport.getLabel();
		n = 0;
		// if (mn != null)
		int nItems = nAR;
		for (TreeNode t : cases.getElements())
		{
			branch b = (branch) t;

			tbl.enterScope();

			// if (mn != null)
			nAR = nItems;
			CgenSupport.emitLabelDef(branchLabel[n++], s);
			s.println("# Temporarily cache the evaluated variable");
			nAR = CgenSupport.emitPop(CgenSupport.T1, nAR, s);

			s.println("# Restore self");
			nAR = CgenSupport.emitPop(CgenSupport.ACC, nAR, s);

			s.println("# Save the case variable in the stack");
			nAR = CgenSupport.emitPush(CgenSupport.T1, nAR, s);
			// The offset is negative as the location of local variables are
			// below
			// the FP
			tbl.addId(b.getName(), -(nAR - 1));

			nAR = b.getExpr().code(c, nAR, tbl, s);

			s.println("# Discard case variable");
			nAR = CgenSupport.emitPop(CgenSupport.T1, nAR, s);
			tbl.exitScope();
			CgenSupport.emitBranch(endLabel, s);
		}

		CgenSupport.emitLabelDef(endLabel, s);
		return nAR;
	}

}
