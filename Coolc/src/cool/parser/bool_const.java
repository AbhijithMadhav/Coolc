/**
 * 
 */
package cool.parser;

import cool.common.TreeConstants;
import cool.common.Utilities;

import cool.semant.ObjectTypeEnvironment;
import cool.semant.SemantError;

import cool.cgen.BoolConst;
import cool.cgen.CgenLookupTable;
import cool.cgen.CgenSupport;

import java.io.PrintStream;

/**
 * Defines AST constructor 'bool_const'.
 * <p>
 * See <a href="TreeNode.html">TreeNode</a> for full documentation.
 */
public class bool_const extends Expression
{
	private Boolean val;

	/**
	 * Creates "bool_const" AST node.
	 * 
	 * @param lineNumber the line in the source file from which this node came.
	 * @param a0 initial value for val
	 */
	public bool_const(int lineNumber, Boolean a1)
	{
		super(lineNumber);
		val = a1;
	}

	public TreeNode copy()
	{
		return new bool_const(lineNumber, copy_Boolean(val));
	}

	public void dump(PrintStream out, int n)
	{
		out.print(Utilities.pad(n) + "bool_const\n");
		dump_Boolean(out, n + 2, val);
	}

	public void dump_with_types(PrintStream out, int n)
	{
		dump_line(out, n);
		out.println(Utilities.pad(n) + "_bool");
		dump_Boolean(out, n + 2, val);
		dump_type(out, n);
	}

	public void validateAndSetType(ObjectTypeEnvironment o, method m, class_ c,
			SemantError err)
	{
		set_type(TreeConstants.Bool);
	}

	/**
	 * Generates code for this expression. This method method is provided
	 * to you as an example of code generation.
	 * 
	 * @param s the output stream
	 * */
	public int code(class_ c, int nAR, CgenLookupTable tbl, PrintStream s)

	{
		s.println("# Boolean constant");
		CgenSupport.emitLoadBool(CgenSupport.ACC, new BoolConst(val), s);
		s.println();
		return nAR;
	}
}