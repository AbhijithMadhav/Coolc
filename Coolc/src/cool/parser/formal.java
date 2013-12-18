/**
 * 
 */
package cool.parser;

import cool.common.AbstractSymbol;
import cool.common.TreeConstants;
import cool.common.UnresolvableSelfTypeException;
import cool.common.Utilities;

import cool.semant.ObjectTypeEnvironment;
import cool.semant.SemantError;

import java.io.PrintStream;

/**
 * Defines AST constructor 'formal'.
 * <p>
 * See <a href="TreeNode.html">TreeNode</a> for full documentation.
 */
public class formal extends Formal
{
	private AbstractSymbol name;
	private AbstractSymbol type_decl;

	/**
	 * Creates "formal" AST node.
	 * 
	 * @param lineNumber the line in the source file from which this node came.
	 * @param a0 initial value for name
	 * @param a1 initial value for type_decl
	 */
	public formal(int lineNumber, AbstractSymbol a1, AbstractSymbol a2)
	{
		super(lineNumber);
		name = a1;
		type_decl = a2;
	}

	public TreeNode copy()
	{
		return new formal(lineNumber, copy_AbstractSymbol(name),
				copy_AbstractSymbol(type_decl));
	}

	public void dump(PrintStream out, int n)
	{
		out.print(Utilities.pad(n) + "formal\n");
		dump_AbstractSymbol(out, n + 2, name);
		dump_AbstractSymbol(out, n + 2, type_decl);
	}

	public void dump_with_types(PrintStream out, int n)
	{
		dump_line(out, n);
		out.println(Utilities.pad(n) + "_formal");
		dump_AbstractSymbol(out, n + 2, name);
		dump_AbstractSymbol(out, n + 2, type_decl);
	}

	public AbstractSymbol getName()
	{
		return name;
	}

	public AbstractSymbol getTypeDecl()
	{
		return type_decl;
	}

	public void validate(ObjectTypeEnvironment o, method m, class_ c,
			SemantError err) throws UnresolvableSelfTypeException
	{
		// formal parameter cannot be self type
		if (type_decl.equals(TreeConstants.SELF_TYPE))
		{
			err.semantError(c).println(
					"Formal parameter " + type_decl
							+ " cannot have type SELF_TYPE.");
			return;
		}

		// Is formal type is a known type
		if (!o.isDefined(type_decl))
		{
			err.semantError(c).println(
					"Type " + type_decl + " of formal parameter " + name
							+ " not defined.");
			return;
		}
	}

}