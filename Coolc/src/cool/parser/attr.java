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
import cool.semant.SemantUtil;

import java.io.PrintStream;

/**
 * Defines AST constructor 'attr'.
 * <p>
 * See <a href="TreeNode.html">TreeNode</a> for full documentation.
 */
public class attr extends Feature
{
	private AbstractSymbol name;
	private AbstractSymbol type_decl;
	private Expression init;
	private method dummy;

	/**
	 * Creates "attr" AST node.
	 * 
	 * @param lineNumber the line in the source file from which this node came.
	 * @param a0 initial value for name
	 * @param a1 initial value for type_decl
	 * @param a2 initial value for init
	 */
	public attr(int lineNumber, AbstractSymbol a1, AbstractSymbol a2,
			Expression a3)
	{
		super(lineNumber);
		name = a1;
		type_decl = a2;
		init = a3;
		dummy = new method(lineNumber, TreeConstants.dummyMethodForAttr,
				new Formals(lineNumber), TreeConstants.Object_, new no_expr(
						lineNumber));
	}

	public TreeNode copy()
	{
		return new attr(lineNumber, copy_AbstractSymbol(name),
				copy_AbstractSymbol(type_decl), (Expression) init.copy());
	}

	public void dump(PrintStream out, int n)
	{
		out.print(Utilities.pad(n) + "attr\n");
		dump_AbstractSymbol(out, n + 2, name);
		dump_AbstractSymbol(out, n + 2, type_decl);
		init.dump(out, n + 2);
	}

	public void dump_with_types(PrintStream out, int n)
	{
		dump_line(out, n);
		out.println(Utilities.pad(n) + "_attr");
		dump_AbstractSymbol(out, n + 2, name);
		dump_AbstractSymbol(out, n + 2, type_decl);
		init.dump_with_types(out, n + 2);
	}

	public AbstractSymbol getName()
	{
		return name;
	}

	public AbstractSymbol getTypeDecl()
	{
		return type_decl;
	}

	public Expression getInit()
	{
		return init;
	}

	public void validate(ObjectTypeEnvironment o, class_ c, SemantError err)
			throws PossibleNullDereferenceException,
			UnresolvableSelfTypeException
	{
		// an attribute cannot be named 'self'
		if (name.equals(TreeConstants.self))
			err.semantError(c).println(
					"'self' cannot be the name of an attribute.");

		// is attr type is known?
		// getResolvedType for '<name> : SELF_TYPE'
		if (!o.isDefined(SemantUtil.getResolvedType(type_decl, c)))
			err.semantError(c).println(
					"Type " + type_decl + " of attribute " + name
							+ " not defined.");

		// Should not override attributes of supertypes
		for (AbstractSymbol cName : o.getSuperTypes(c.getName()))
			if (!cName.equals(c.getName())
					&& o.toClass_(cName).containsAttr(name, o, err))
				err.semantError(c).println(
						"Attribute " + name
								+ " is an attribute of an inherited class.");

		// validate the initializer
		init.validateAndSetType(o, dummy, c, err);
	}

}