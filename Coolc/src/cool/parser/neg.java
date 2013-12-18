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
 * Defines AST constructor 'neg'.
 * <p>
 * See <a href="TreeNode.html">TreeNode</a> for full documentation.
 */
public class neg extends Expression
{
	private Expression e1;

	/**
	 * Creates "neg" AST node.
	 * 
	 * @param lineNumber the line in the source file from which this node came.
	 * @param a0 initial value for e1
	 */
	public neg(int lineNumber, Expression a1)
	{
		super(lineNumber);
		e1 = a1;
	}

	public TreeNode copy()
	{
		return new neg(lineNumber, (Expression) e1.copy());
	}

	public void dump(PrintStream out, int n)
	{
		out.print(Utilities.pad(n) + "neg\n");
		e1.dump(out, n + 2);
	}

	public void dump_with_types(PrintStream out, int n)
	{
		dump_line(out, n);
		out.println(Utilities.pad(n) + "_neg");
		e1.dump_with_types(out, n + 2);
		dump_type(out, n);
	}

	public void validateAndSetType(ObjectTypeEnvironment o, method m, class_ c,
			SemantError err) throws PossibleNullDereferenceException,
			UnresolvableSelfTypeException
	{
		e1.validateAndSetType(o, m, c, err);
		set_type(TreeConstants.Int);
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
		s.println("# neg expression");
		s.println();

		s.println("# Evaluate the operand and get reference in $a0");
		nAR = e1.code(c, nAR, tbl, s);
		s.println();

		s.println("# Negate the value and save it");
		CgenSupport.emitFetchInt(CgenSupport.ACC, CgenSupport.ACC, s);
		CgenSupport.emitNeg(CgenSupport.ACC, CgenSupport.ACC, s);
		nAR = CgenSupport.emitPush(CgenSupport.ACC, nAR, s);
		s.println();

		s.println("#Create new integer object");
		nAR = CgenSupport.emitCallObjectCopy(TreeConstants.Int, nAR, s);
		s.println();

		s.println("# Retrieve the negated value");
		nAR = CgenSupport.emitPop(CgenSupport.T1, nAR, s);
		s.println();

		s.println("# Initialize the new object with it");
		CgenSupport.emitStoreInt(CgenSupport.T1, CgenSupport.ACC, s);
		s.println();

		s.println("# End of neg expression");
		s.println();
		return nAR;
	}

}
