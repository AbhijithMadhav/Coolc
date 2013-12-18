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

import java.io.PrintStream;

/**
 * Defines AST constructor 'branch'.
 * <p>
 * See <a href="TreeNode.html">TreeNode</a> for full documentation.
 */
public class branch extends Case
{
	private AbstractSymbol name;
	private AbstractSymbol type_decl;
	private Expression expr;

	/**
	 * Creates "branch" AST node.
	 * 
	 * @param lineNumber the line in the source file from which this node came.
	 * @param a0 initial value for name
	 * @param a1 initial value for type_decl
	 * @param a2 initial value for expr
	 */
	public branch(int lineNumber, AbstractSymbol a1, AbstractSymbol a2,
			Expression a3)
	{
		super(lineNumber);
		setName(a1);
		type_decl = a2;
		expr = a3;
	}

	public TreeNode copy()
	{
		return new branch(lineNumber, copy_AbstractSymbol(getName()),
				copy_AbstractSymbol(type_decl), (Expression) expr.copy());
	}

	public void dump(PrintStream out, int n)
	{
		out.print(Utilities.pad(n) + "branch\n");
		dump_AbstractSymbol(out, n + 2, getName());
		dump_AbstractSymbol(out, n + 2, type_decl);
		expr.dump(out, n + 2);
	}

	public void dump_with_types(PrintStream out, int n)
	{
		dump_line(out, n);
		out.println(Utilities.pad(n) + "_branch");
		dump_AbstractSymbol(out, n + 2, getName());
		dump_AbstractSymbol(out, n + 2, type_decl);
		expr.dump_with_types(out, n + 2);
	}

	public AbstractSymbol getTypeDecl()
	{
		return type_decl;
	}

	public Expression getExpr()
	{
		return expr;
	}

	public void validate(ObjectTypeEnvironment o, method m, class_ c,
			SemantError err) throws PossibleNullDereferenceException,
			UnresolvableSelfTypeException
	{
		o.enterScope(); // Branch scope start

		if (type_decl.equals(TreeConstants.SELF_TYPE))
			err.semantError(c).println(
					"Identifier " + getName()
							+ " declared with type SELF_TYPE in case branch.");
		// is branch type is a known type
		else if (!o.isDefined(type_decl))
			err.semantError(c).println(
					"Type " + type_decl + " of branch " + getName()
							+ " not defined.");

		// Introduce the branch identifier into the symbol table
		o.addId(getName(), new TypeAndInit(type_decl, new no_expr(lineNumber)));

		// Validate the branch expression
		expr.validateAndSetType(o, m, c, err);

		o.exitScope(); // Branch scope ends
	}

	/**
	 * @return the name
	 */
	public AbstractSymbol getName()
	{
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(AbstractSymbol name)
	{
		this.name = name;
	}

}
