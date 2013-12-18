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

import cool.cgen.CgenLookupTable;
import cool.cgen.CgenSupport;

import java.io.PrintStream;

/**
 * Defines AST constructor 'sub'.
 * <p>
 * See <a href="TreeNode.html">TreeNode</a> for full documentation.
 */
public class sub extends Expression
{
	private Expression e1;
	private Expression e2;

	/**
	 * Creates "sub" AST node.
	 * 
	 * @param lineNumber the line in the source file from which this node came.
	 * @param a0 initial value for e1
	 * @param a1 initial value for e2
	 */
	public sub(int lineNumber, Expression a1, Expression a2)
	{
		super(lineNumber);
		e1 = a1;
		e2 = a2;
	}

	public TreeNode copy()
	{
		return new sub(lineNumber, (Expression) e1.copy(),
				(Expression) e2.copy());
	}

	public void dump(PrintStream out, int n)
	{
		out.print(Utilities.pad(n) + "sub\n");
		e1.dump(out, n + 2);
		e2.dump(out, n + 2);
	}

	public void dump_with_types(PrintStream out, int n)
	{
		dump_line(out, n);
		out.println(Utilities.pad(n) + "_sub");
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

		// type checking
		AbstractSymbol t1 = e1.get_type();
		AbstractSymbol t2 = e2.get_type();
		set_type(TreeConstants.Object_);
		if (t1.equals(TreeConstants.Int) && t2.equals(TreeConstants.Int))
			set_type(TreeConstants.Int);
		else
			err.semantError(c).println("Non-Int arguments: " + t1 + " - " + t2);
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
		s.println("# - expression");
		s.println();

		s.println("#Save reference to self object.");
		s.println("# Needed while evaluating the second operand");
		nAR = CgenSupport.emitPush(CgenSupport.ACC, nAR, s);

		s.println("# evaluate the first operand and get reference in $a0");
		nAR = e1.code(c, nAR, tbl, s);

		s.println("# Get the value of the first operand");
		CgenSupport.emitFetchInt(CgenSupport.T1, CgenSupport.ACC, s);

		s.println("# Restore the self object before evaluating the second operand");
		nAR = CgenSupport.emitPop(CgenSupport.ACC, nAR, s);

		s.println("# Save the value of the first operand in the stack");
		nAR = CgenSupport.emitPush(CgenSupport.T1, nAR, s);

		s.println("# evaluate the second operand and get reference in $a0");
		nAR = e2.code(c, nAR, tbl, s);

		s.println("# Get the value of the first operand");
		CgenSupport.emitFetchInt(CgenSupport.ACC, CgenSupport.ACC, s);

		s.println("# Restore the value of the first operand from the stack");
		nAR = CgenSupport.emitPop(CgenSupport.T1, nAR, s);

		s.println("# Compute and then save result in stack");
		CgenSupport
				.emitSub(CgenSupport.ACC, CgenSupport.T1, CgenSupport.ACC, s);
		nAR = CgenSupport.emitPush(CgenSupport.ACC, nAR, s);

		s.println("# Create new object to store the result");
		nAR = CgenSupport.emitCallObjectCopy(TreeConstants.Int, nAR, s);

		s.println("# Store result in new object");
		nAR = CgenSupport.emitPop(CgenSupport.T1, nAR, s);
		CgenSupport.emitStoreInt(CgenSupport.T1, CgenSupport.ACC, s);
		return nAR;
	}

}
