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
 * Defines AST constructor 'loop'.
 * <p>
 * See <a href="TreeNode.html">TreeNode</a> for full documentation.
 */
public class loop extends Expression
{
	private Expression pred;
	private Expression body;

	/**
	 * Creates "loop" AST node.
	 * 
	 * @param lineNumber the line in the source file from which this node came.
	 * @param a0 initial value for pred
	 * @param a1 initial value for body
	 */
	public loop(int lineNumber, Expression a1, Expression a2)
	{
		super(lineNumber);
		pred = a1;
		body = a2;
	}

	public TreeNode copy()
	{
		return new loop(lineNumber, (Expression) pred.copy(),
				(Expression) body.copy());
	}

	public void dump(PrintStream out, int n)
	{
		out.print(Utilities.pad(n) + "loop\n");
		pred.dump(out, n + 2);
		body.dump(out, n + 2);
	}

	public void dump_with_types(PrintStream out, int n)
	{
		dump_line(out, n);
		out.println(Utilities.pad(n) + "_loop");
		pred.dump_with_types(out, n + 2);
		body.dump_with_types(out, n + 2);
		dump_type(out, n);
	}

	public void validateAndSetType(ObjectTypeEnvironment o, method m, class_ c,
			SemantError err) throws PossibleNullDereferenceException,
			UnresolvableSelfTypeException
	{
		// Validate the predicate expression and the body expression
		pred.validateAndSetType(o, m, c, err);
		body.validateAndSetType(o, m, c, err);

		// Check the predicate expression
		if (!pred.get_type().equals(TreeConstants.Bool))
			err.semantError(c).println(
					"Predicate of 'while' does not have type Bool.");

		// set the type of the 'while' expression
		set_type(TreeConstants.Object_);
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
		s.println("# while loop");
		s.println();

		int endLabel = CgenSupport.getLabel();
		int loopLabel = CgenSupport.getLabel();

		s.println("# Save self object");
		s.println("# Will need it to evaluate the pred in the first iteration");
		nAR = CgenSupport.emitPush(CgenSupport.ACC, nAR, s);
		s.println();

		s.println("# Loop");
		CgenSupport.emitLabelDef(loopLabel, s);
		s.println();

		s.println("# Retrieve self object before evaluating the predicate");
		nAR = CgenSupport.emitPop(CgenSupport.ACC, nAR, s);
		s.println();

		s.println("# Save self object before evaluating predicate.");
		s.println("# Will need it to evaluate the body of the loop");
		nAR = CgenSupport.emitPush(CgenSupport.ACC, nAR, s);
		s.println();

		s.println("# Evaluate predicate and get reference in $a0");
		nAR = pred.code(c, nAR, tbl, s);
		CgenSupport.emitFetchBool(CgenSupport.ACC, CgenSupport.ACC, s);
		s.println();

		s.println("# Get reference to 'false' constant");
		CgenSupport.emitLoadAddress(CgenSupport.T1,
				CgenSupport.BOOLCONST_PREFIX + CgenSupport.BOOL_FALSE, s);
		CgenSupport.emitFetchBool(CgenSupport.T1, CgenSupport.T1, s);
		s.println();

		s.println("# The test. Did the predicate evaluate to 'false'?");
		s.println("# If it did jump to label" + endLabel);
		CgenSupport.emitBeq(CgenSupport.ACC, CgenSupport.T1, endLabel, s);
		s.println();

		s.println("# Retrieve self object before evaluating the body");
		nAR = CgenSupport.emitPop(CgenSupport.ACC, nAR, s);
		s.println();

		s.println("# Save self object.");
		s.println("# Will need it to evaluate the pred in the next iteration");
		nAR = CgenSupport.emitPush(CgenSupport.ACC, nAR, s);
		s.println();

		s.println("# Evaluate the body and get reference in $a0");
		nAR = body.code(c, nAR, tbl, s);
		s.println();

		s.println("# Go back to label" + loopLabel
				+ " to check if predicate is false");
		CgenSupport.emitBranch(loopLabel, s);
		s.println();

		s.println("# End of loop.");
		CgenSupport.emitLabelDef(endLabel, s);

		s.println("# Retrieve self object to restore the SP");
		nAR = CgenSupport.emitPop(CgenSupport.ACC, nAR, s);
		s.println();

		s.println("# Set return value to void");
		CgenSupport.emitLoadAddress(CgenSupport.ACC,
				CgenSupport.VOIDCONST_PREFIX, s);
		s.println();
		return nAR;
	}

}
