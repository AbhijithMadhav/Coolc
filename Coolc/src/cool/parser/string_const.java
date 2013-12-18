/**
 * 
 */
package cool.parser;

import cool.common.AbstractSymbol;
import cool.common.TreeConstants;
import cool.common.Utilities;

import cool.semant.ObjectTypeEnvironment;
import cool.semant.SemantError;

import cool.cgen.CgenLookupTable;
import cool.cgen.CgenSupport;
import cool.cgen.StringSymbol;

import java.io.PrintStream;

/**
 * Defines AST constructor 'string_const'.
 * <p>
 * See <a href="TreeNode.html">TreeNode</a> for full documentation.
 */
public class string_const extends Expression
{
	private AbstractSymbol token;

	/**
	 * Creates "string_const" AST node.
	 * 
	 * @param lineNumber the line in the source file from which this node came.
	 * @param a0 initial value for token
	 */
	public string_const(int lineNumber, AbstractSymbol a1)
	{
		super(lineNumber);
		token = a1;
	}

	public TreeNode copy()
	{
		return new string_const(lineNumber, copy_AbstractSymbol(token));
	}

	public void dump(PrintStream out, int n)
	{
		out.print(Utilities.pad(n) + "string_const\n");
		dump_AbstractSymbol(out, n + 2, token);
	}

	public void dump_with_types(PrintStream out, int n)
	{
		dump_line(out, n);
		out.println(Utilities.pad(n) + "_string");
		out.print(Utilities.pad(n + 2) + "\"");
		Utilities.printEscapedString(out, token.toString());
		out.println("\"");
		dump_type(out, n);
	}

	public void validateAndSetType(ObjectTypeEnvironment o, method m, class_ c,
			SemantError err)
	{
		set_type(TreeConstants.Str);
	}

	/**
	 * Generates code for the 'string_const' expression.
	 * 
	 * @param s the output stream
	 * */
	public int code(class_ c, int nAR, CgenLookupTable tbl, PrintStream s)

	{
		s.println("# String constant");
		CgenSupport.emitLoadString(CgenSupport.ACC, (StringSymbol) token, s);
		s.println();
		return nAR;
	}
}