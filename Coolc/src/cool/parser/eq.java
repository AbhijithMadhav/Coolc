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
import java.util.Vector;

/**
 * Defines AST constructor 'eq'.
 * <p>
 * See <a href="TreeNode.html">TreeNode</a> for full documentation.
 */
public class eq extends Expression
{
	private Expression e1;
	private Expression e2;

	/**
	 * Creates "eq" AST node.
	 * 
	 * @param lineNumber the line in the source file from which this node came.
	 * @param a0 initial value for e1
	 * @param a1 initial value for e2
	 */
	public eq(int lineNumber, Expression a1, Expression a2)
	{
		super(lineNumber);
		e1 = a1;
		e2 = a2;
	}

	public TreeNode copy()
	{
		return new eq(lineNumber, (Expression) e1.copy(),
				(Expression) e2.copy());
	}

	public void dump(PrintStream out, int n)
	{
		out.print(Utilities.pad(n) + "eq\n");
		e1.dump(out, n + 2);
		e2.dump(out, n + 2);
	}

	public void dump_with_types(PrintStream out, int n)
	{
		dump_line(out, n);
		out.println(Utilities.pad(n) + "_eq");
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

		Vector<AbstractSymbol> bTypes = new Vector<AbstractSymbol>();
		bTypes.add(TreeConstants.Int);
		bTypes.add(TreeConstants.Str);
		bTypes.add(TreeConstants.Bool);
		if (e1.get_type() != e2.get_type()
				&& (bTypes.contains(e1.get_type()) || bTypes.contains(e2
						.get_type())))
		{
			err.semantError(c).println("Illegal comparison with a basic type.");
			set_type(TreeConstants.Object_);
		}
		else
			set_type(TreeConstants.Bool);
	}

	/**
	 * Generates code for 'eq' expression.
	 * 
	 * @param s the output stream
	 * */
	public int code(class_ c, int nAR, CgenLookupTable tbl, PrintStream s)

	{
		int trueLabel = CgenSupport.getLabel();
		int endLabel = CgenSupport.getLabel();

		s.println("# '=' expression");

		s.println("# Save self object before evaluating the LHS.");
		s.println("# Will need it while evaluating the RHS");
		nAR = CgenSupport.emitPush(CgenSupport.ACC, nAR, s);
		s.println();

		s.println("# Evaluate the 1st operand and get reference in $a0");
		nAR = e1.code(c, nAR, tbl, s);
		s.println();

		s.println("# Temporarily cache the 1st operand");
		CgenSupport.emitMove(CgenSupport.T1, CgenSupport.ACC, s);

		s.println("# Restore self object to evaluate the 2nd operand");
		nAR = CgenSupport.emitPop(CgenSupport.ACC, nAR, s);
		s.println();

		s.println("# Save the first operand");
		nAR = CgenSupport.emitPush(CgenSupport.T1, nAR, s);
		s.println();

		s.println("# Evaluate the 2nd operand and get reference in $a0");
		nAR = e2.code(c, nAR, tbl, s);
		s.println();

		s.println("# Move 2nd operand into $t2 to do the equality testing");
		CgenSupport.emitMove(CgenSupport.T2, CgenSupport.ACC, s);
		s.println();

		s.println("# Restore 1st operand into $t1 to do the equality testing");
		nAR = CgenSupport.emitPop(CgenSupport.T1, nAR, s);
		s.println();
		s.println("# Are both operands the same object?. If yes go to label"
				+ trueLabel);
		CgenSupport.emitBeq(CgenSupport.T1, CgenSupport.T2, trueLabel, s);
		s.println();

		s.println("# equality test");
		CgenSupport.emitLoadAddress(CgenSupport.ACC,
				CgenSupport.BOOLCONST_PREFIX + CgenSupport.BOOL_TRUE, s);
		CgenSupport.emitLoadAddress(CgenSupport.A1,
				CgenSupport.BOOLCONST_PREFIX + CgenSupport.BOOL_FALSE, s);
		CgenSupport.emitJal("equality_test", s);
		CgenSupport.emitBranch(endLabel, s);

		s.println("# True Label. Return true");
		CgenSupport.emitLabelDef(trueLabel, s);
		CgenSupport.emitLoadAddress(CgenSupport.ACC,
				CgenSupport.BOOLCONST_PREFIX + CgenSupport.BOOL_TRUE, s);

		CgenSupport.emitLabelDef(endLabel, s);
		return nAR;
	}

}