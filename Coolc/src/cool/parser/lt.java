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
 * Defines AST constructor 'lt'.
 * <p>
 * See <a href="TreeNode.html">TreeNode</a> for full documentation.
 */
public class lt extends Expression
{
	private Expression e1;
	private Expression e2;

	/**
	 * Creates "lt" AST node.
	 * 
	 * @param lineNumber the line in the source file from which this node came.
	 * @param a0 initial value for e1
	 * @param a1 initial value for e2
	 */
	public lt(int lineNumber, Expression a1, Expression a2)
	{
		super(lineNumber);
		e1 = a1;
		e2 = a2;
	}

	public TreeNode copy()
	{
		return new lt(lineNumber, (Expression) e1.copy(),
				(Expression) e2.copy());
	}

	public void dump(PrintStream out, int n)
	{
		out.print(Utilities.pad(n) + "lt\n");
		e1.dump(out, n + 2);
		e2.dump(out, n + 2);
	}

	public void dump_with_types(PrintStream out, int n)
	{
		dump_line(out, n);
		out.println(Utilities.pad(n) + "_lt");
		e1.dump_with_types(out, n + 2);
		e2.dump_with_types(out, n + 2);
		dump_type(out, n);
	}

	public void validateAndSetType(ObjectTypeEnvironment o, method m, class_ c,
			SemantError err) throws PossibleNullDereferenceException,
			UnresolvableSelfTypeException
	{
		e1.validateAndSetType(o, m, c, err);
		e2.validateAndSetType(o, m, c, err);
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
		s.println("# < expression");
		s.println("# ");
		int trueLabel = CgenSupport.getLabel();
		int endLabel = CgenSupport.getLabel();

		s.println("# Save self object. Needed during evaluation of the 2nd "
				+ "operand");
		nAR = CgenSupport.emitPush(CgenSupport.ACC, nAR, s);
		s.println();

		s.println("#Evaluate 1st operand and get reference in $a0");
		nAR = e1.code(c, nAR, tbl, s);
		s.println();

		s.println("#Get value of 1st operand");
		CgenSupport.emitFetchInt(CgenSupport.T1, CgenSupport.ACC, s);
		s.println();

		s.println("# Restore self object before evaluation of the 2nd "
				+ "operand");
		nAR = CgenSupport.emitPop(CgenSupport.ACC, nAR, s);
		s.println();

		s.println("#Save the value of the 1st operand in the stack");
		nAR = CgenSupport.emitPush(CgenSupport.T1, nAR, s);
		s.println();

		s.println("# Evaluate the 2nd operand and get reference in $a0");
		nAR = e2.code(c, nAR, tbl, s);
		s.println("#");

		s.println("# Get the value of the 2nd operand");
		CgenSupport.emitFetchInt(CgenSupport.ACC, CgenSupport.ACC, s);
		s.println();

		s.println("# Restore the value of the first operand");
		nAR = CgenSupport.emitPop(CgenSupport.T1, nAR, s);
		s.println();

		s.println("# The test. Branch to label" + trueLabel + " if true");
		CgenSupport.emitBlt(CgenSupport.T1, CgenSupport.ACC, trueLabel, s);
		s.println();

		/** False **/
		s.println("# False part");
		s.println("# Set return value to the boolean constant, false");
		CgenSupport.emitLoadAddress(CgenSupport.ACC,
				CgenSupport.BOOLCONST_PREFIX + CgenSupport.BOOL_FALSE, s);
		s.println();

		s.println("#Branch to the end, label" + endLabel);
		CgenSupport.emitBranch(endLabel, s);
		s.println("#");

		s.println("# True part");
		CgenSupport.emitLabelDef(trueLabel, s);
		s.println();

		s.println("# Set return value to the boolean constant, true");
		CgenSupport.emitLoadAddress(CgenSupport.ACC,
				CgenSupport.BOOLCONST_PREFIX + CgenSupport.BOOL_TRUE, s);
		s.println();

		s.println("# End of < expression");
		CgenSupport.emitLabelDef(endLabel, s);
		s.println();
		return nAR;

	}
}
