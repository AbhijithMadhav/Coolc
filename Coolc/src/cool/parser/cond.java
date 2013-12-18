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
import cool.semant.SemantUtil;

import cool.cgen.CgenLookupTable;
import cool.cgen.CgenSupport;

import java.io.PrintStream;

/**
 * Defines AST constructor 'cond'.
 * <p>
 * See <a href="TreeNode.html">TreeNode</a> for full documentation.
 */
public class cond extends Expression
{
	private Expression pred;
	private Expression then_exp;
	private Expression else_exp;

	/**
	 * Creates "cond" AST node.
	 * 
	 * @param lineNumber the line in the source file from which this node came.
	 * @param a0 initial value for pred
	 * @param a1 initial value for then_exp
	 * @param a2 initial value for else_exp
	 */
	public cond(int lineNumber, Expression a1, Expression a2, Expression a3)
	{
		super(lineNumber);
		pred = a1;
		then_exp = a2;
		else_exp = a3;
	}

	public TreeNode copy()
	{
		return new cond(lineNumber, (Expression) pred.copy(),
				(Expression) then_exp.copy(), (Expression) else_exp.copy());
	}

	public void dump(PrintStream out, int n)
	{
		out.print(Utilities.pad(n) + "cond\n");
		pred.dump(out, n + 2);
		then_exp.dump(out, n + 2);
		else_exp.dump(out, n + 2);
	}

	public void dump_with_types(PrintStream out, int n)
	{
		dump_line(out, n);
		out.println(Utilities.pad(n) + "_cond");
		pred.dump_with_types(out, n + 2);
		then_exp.dump_with_types(out, n + 2);
		else_exp.dump_with_types(out, n + 2);
		dump_type(out, n);
	}

	public void validateAndSetType(ObjectTypeEnvironment o, method m, class_ c,
			SemantError err) throws PossibleNullDereferenceException,
			UnresolvableSelfTypeException
	{
		// validate the predicate, 'then' expression and the 'else' expression
		pred.validateAndSetType(o, m, c, err);
		then_exp.validateAndSetType(o, m, c, err);
		else_exp.validateAndSetType(o, m, c, err);

		// Check type of predicate
		if (!pred.get_type().equals(TreeConstants.Bool))
			err.semantError(c).println(
					"Predicate of 'if' does not have type Bool.");

		// Set type of the 'cond' expression.
		// getResolvedType for cases where the the 'then' or 'else' expression
		// evalutes to SELF_TYPE
		set_type(o.lub(SemantUtil.getResolvedType(then_exp.get_type(), c),
				SemantUtil.getResolvedType(else_exp.get_type(), c)));
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
		s.println("# If-then-else");
		s.println();

		int elseLabel = CgenSupport.getLabel();
		int endLabel = CgenSupport.getLabel();

		s.println("# Save self object before evaluating predicate. ");
		s.println("# Will need it to evaluate then-expression and else-expression");
		nAR = CgenSupport.emitPush(CgenSupport.ACC, nAR, s);
		s.println();

		s.println("# Evaluate predicate and get reference in $a0");
		nAR = pred.code(c, nAR, tbl, s);
		s.println();

		s.println("# Fetch the bool value of the object");
		CgenSupport.emitFetchBool(CgenSupport.ACC, CgenSupport.ACC, s);

		s.println("# Get reference to 'false' constant");
		CgenSupport.emitLoadAddress(CgenSupport.T1,
				CgenSupport.BOOLCONST_PREFIX + CgenSupport.BOOL_FALSE, s);
		CgenSupport.emitFetchBool(CgenSupport.T1, CgenSupport.T1, s);
		s.println();

		s.println("# The test. Did the predicate evaluate to 'false'?");
		s.println("# If it did jump to label" + elseLabel);
		CgenSupport.emitBeq(CgenSupport.ACC, CgenSupport.T1, elseLabel, s);
		s.println();

		// Save the number of items in the current AR
		// This is needed to restore the same while generating code for the else
		// branch
		int nItems = nAR;
		s.println("# If-Then Branch");
		s.println("# Retrieve self object before evaluating the then-expr");
		nAR = CgenSupport.emitPop(CgenSupport.ACC, nAR, s);
		s.println();

		s.println("# Evaluate the then-expr and get reference in $a0");
		nAR = then_exp.code(c, nAR, tbl, s);
		s.println();

		s.println("# End of if-then. Branch to label" + endLabel);
		CgenSupport.emitBranch(endLabel, s);
		s.println();

		nAR = nItems;
		s.println("# The else branch");
		CgenSupport.emitLabelDef(elseLabel, s);
		s.println();

		s.println("# Retrieve self object before evaluating the else-expr");
		nAR = CgenSupport.emitPop(CgenSupport.ACC, nAR, s);
		s.println();

		s.println("# Evaluate the else-expr and get reference in $a0");
		nAR = else_exp.code(c, nAR, tbl, s);
		s.println();

		s.println("# End of if-then-else");
		CgenSupport.emitLabelDef(endLabel, s);
		s.println();
		return nAR;
	}

}
