/**
 * 
 */
package cool.parser;

import cool.common.PossibleNullDereferenceException;
import cool.common.TreeConstants;
import cool.common.UnresolvableSelfTypeException;
import cool.common.Utilities;

import cool.semant.ObjectTypeEnvironment;
import cool.semant.SemantError;

import cool.cgen.CgenLookupTable;
import cool.cgen.CgenSupport;

import java.io.PrintStream;

/**
 * Defines AST constructor 'comp'.
 * <p>
 * See <a href="TreeNode.html">TreeNode</a> for full documentation.
 */
public class comp extends Expression
{
	private Expression e1;

	/**
	 * Creates "comp" AST node.
	 * 
	 * @param lineNumber the line in the source file from which this node came.
	 * @param a0 initial value for e1
	 */
	public comp(int lineNumber, Expression a1)
	{
		super(lineNumber);
		e1 = a1;
	}

	public TreeNode copy()
	{
		return new comp(lineNumber, (Expression) e1.copy());
	}

	public void dump(PrintStream out, int n)
	{
		out.print(Utilities.pad(n) + "comp\n");
		e1.dump(out, n + 2);
	}

	public void dump_with_types(PrintStream out, int n)
	{
		dump_line(out, n);
		out.println(Utilities.pad(n) + "_comp");
		e1.dump_with_types(out, n + 2);
		dump_type(out, n);
	}

	public void validateAndSetType(ObjectTypeEnvironment o, method m, class_ c,
			SemantError err) throws PossibleNullDereferenceException,
			UnresolvableSelfTypeException
	{
		e1.validateAndSetType(o, m, c, err);
		set_type(TreeConstants.Bool);
	}

	/**
	 * Generates code for this expression. This method is to be completed
	 * in programming assignment 5. (You may add or remove parameters as
	 * you wish.)
	 * 
	 * @param s the output stream
	 * */
	public int code(class_ c, int nAR, CgenLookupTable tbl, PrintStream s)

	{
		s.println("# Complement expression");
		s.println();

		int endLabel = CgenSupport.getLabel();
		int returnFalseLabel = CgenSupport.getLabel();

		s.println("# Evaluate the operand");
		nAR = e1.code(c, nAR, tbl, s);
		s.println();

		s.println("# Fetch the boolean value of the operand");
		CgenSupport.emitFetchBool(CgenSupport.T1, CgenSupport.ACC, s);
		s.println();

		s.println("# Save the operand");
		nAR = CgenSupport.emitPush(CgenSupport.ACC, nAR, s);
		s.println();

		s.println("# Get the 'true' value");
		CgenSupport.emitLoadImm(CgenSupport.ACC, CgenSupport.BOOL_TRUE, s);
		s.println();

		int nItems = nAR;
		s.println("# If operand is true, branch to label" + returnFalseLabel);
		CgenSupport.emitBeq(CgenSupport.T1, CgenSupport.ACC, returnFalseLabel,
				s);
		s.println();

		s.println("# Return true branch");
		s.println("# Restore operand");
		nAR = CgenSupport.emitPop(CgenSupport.ACC, nAR, s);
		s.println();

		s.println("# Return true");
		CgenSupport.emitLoadAddress(CgenSupport.ACC,
				CgenSupport.BOOLCONST_PREFIX + CgenSupport.BOOL_TRUE, s);
		CgenSupport.emitBranch(endLabel, s);
		s.println();

		nAR = nItems;
		s.println("# Return False branch");
		CgenSupport.emitLabelDef(returnFalseLabel, s);
		s.println();

		s.println("# Restore operand");
		nAR = CgenSupport.emitPop(CgenSupport.ACC, nAR, s);
		s.println();

		s.println("# Return False");
		CgenSupport.emitLoadAddress(CgenSupport.ACC,
				CgenSupport.BOOLCONST_PREFIX + CgenSupport.BOOL_TRUE, s);
		s.println();

		s.println("# End of complement expression");
		CgenSupport.emitLabelDef(endLabel, s);
		s.println();
		return nAR;
	}
}