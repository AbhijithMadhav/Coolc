/**
 * 
 */
package cool.parser;

import cool.common.TreeConstants;
import cool.common.Utilities;

import cool.semant.ObjectTypeEnvironment;
import cool.semant.SemantError;

import cool.cgen.CgenLookupTable;

import java.io.PrintStream;

/**
 * Defines AST constructor 'no_expr'.
 * <p>
 * See <a href="TreeNode.html">TreeNode</a> for full documentation.
 */
public class no_expr extends Expression
{
	/**
	 * Creates "no_expr" AST node.
	 * 
	 * @param lineNumber the line in the source file from which this node came.
	 */
	public no_expr(int lineNumber)
	{
		super(lineNumber);
	}

	public TreeNode copy()
	{
		return new no_expr(lineNumber);
	}

	public void dump(PrintStream out, int n)
	{
		out.print(Utilities.pad(n) + "no_expr\n");
	}

	public void dump_with_types(PrintStream out, int n)
	{
		dump_line(out, n);
		out.println(Utilities.pad(n) + "_no_expr");
		dump_type(out, n);
	}

	public void validateAndSetType(ObjectTypeEnvironment o, method m, class_ c,
			SemantError err)
	{
		set_type(TreeConstants.No_type);
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
		return nAR;
	}
}