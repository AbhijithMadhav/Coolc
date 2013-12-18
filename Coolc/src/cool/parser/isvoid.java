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

import cool.cgen.BoolConst;
import cool.cgen.CgenLookupTable;
import cool.cgen.CgenSupport;

import java.io.PrintStream;

/**
 * Defines AST constructor 'isvoid'.
 * <p>
 * See <a href="TreeNode.html">TreeNode</a> for full documentation.
 */
public class isvoid extends Expression
{
	private Expression e1;

	/**
	 * Creates "isvoid" AST node.
	 * 
	 * @param lineNumber the line in the source file from which this node came.
	 * @param a0 initial value for e1
	 */
	public isvoid(int lineNumber, Expression a1)
	{
		super(lineNumber);
		e1 = a1;
	}

	public TreeNode copy()
	{
		return new isvoid(lineNumber, (Expression) e1.copy());
	}

	public void dump(PrintStream out, int n)
	{
		out.print(Utilities.pad(n) + "isvoid\n");
		e1.dump(out, n + 2);
	}

	public void dump_with_types(PrintStream out, int n)
	{
		dump_line(out, n);
		out.println(Utilities.pad(n) + "_isvoid");
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
		s.println("# isvoid expression");
		s.println();

		int falseLabel = CgenSupport.getLabel();
		int endLabel = CgenSupport.getLabel();

		s.println("# Evaluate the operand and get reference in $a0");
		nAR = e1.code(c, nAR, tbl, s);
		s.println();

		s.println("# Get the class tag of the object");
		CgenSupport.emitFetchClassTag(CgenSupport.ACC, CgenSupport.ACC, s);
		s.println();

		s.println(" #isvoid?? Branch to the 'false' branch if not void, label"
				+ falseLabel);
		CgenSupport.emitBgti(CgenSupport.ACC, CgenSupport.VOID_CLASSTAG,
				falseLabel, s);
		s.println();

		s.println("# True branch");
		s.println("# Return value is true");
		CgenSupport.emitLoadBool(CgenSupport.ACC, new BoolConst(true), s);
		CgenSupport.emitBranch(endLabel, s);
		s.println();

		s.println("# False branch");
		CgenSupport.emitLabelDef(falseLabel, s);

		s.println("# Return value is true");
		CgenSupport.emitLoadBool(CgenSupport.ACC, new BoolConst(false), s);
		s.println();

		s.println("# End of isvoid expression");
		CgenSupport.emitLabelDef(endLabel, s);
		CgenSupport.emitStoreBool(CgenSupport.T1, CgenSupport.ACC, s);
		s.println();
		return nAR;
	}
}
