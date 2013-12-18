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

import cool.cgen.CgenLookupTable;
import cool.cgen.CgenSupport;
import cool.cgen.CgenUtilities;

import java.io.PrintStream;

/**
 * Defines AST constructor 'let'.
 * <p>
 * See <a href="TreeNode.html">TreeNode</a> for full documentation.
 */
public class let extends Expression
{
	private AbstractSymbol identifier;
	private AbstractSymbol type_decl;
	private Expression init;
	private Expression body;

	/**
	 * Creates "let" AST node.
	 * 
	 * @param lineNumber the line in the source file from which this node came.
	 * @param a0 initial value for identifier
	 * @param a1 initial value for type_decl
	 * @param a2 initial value for init
	 * @param a3 initial value for body
	 */
	public let(int lineNumber, AbstractSymbol a1, AbstractSymbol a2,
			Expression a3, Expression a4)
	{
		super(lineNumber);
		identifier = a1;
		type_decl = a2;
		init = a3;
		body = a4;
	}

	public TreeNode copy()
	{
		return new let(lineNumber, copy_AbstractSymbol(identifier),
				copy_AbstractSymbol(type_decl), (Expression) init.copy(),
				(Expression) body.copy());
	}

	public void dump(PrintStream out, int n)
	{
		out.print(Utilities.pad(n) + "let\n");
		dump_AbstractSymbol(out, n + 2, identifier);
		dump_AbstractSymbol(out, n + 2, type_decl);
		init.dump(out, n + 2);
		body.dump(out, n + 2);
	}

	public void dump_with_types(PrintStream out, int n)
	{
		dump_line(out, n);
		out.println(Utilities.pad(n) + "_let");
		dump_AbstractSymbol(out, n + 2, identifier);
		dump_AbstractSymbol(out, n + 2, type_decl);
		init.dump_with_types(out, n + 2);
		body.dump_with_types(out, n + 2);
		dump_type(out, n);
	}

	public void validateAndSetType(ObjectTypeEnvironment o, method m, class_ c,
			SemantError err) throws PossibleNullDereferenceException,
			UnresolvableSelfTypeException
	{

		// set default type of expression in case of error
		set_type(TreeConstants.Object_);

		if (identifier.equals(TreeConstants.self))
			err.semantError(c).println(
					"'self' cannot be bound in a 'let' expression.");

		// getResolvedType for 'let <identifier> : SELFTYPE in ...'
		if (!o.isDefined(SemantUtil.getResolvedType(type_decl, c)))
			err.semantError(c).println(
					"Type " + type_decl + " of let not defined.");

		// Validate the initializer expression
		init.validateAndSetType(o, m, c, err);

		// let-init
		if (!(SemantUtil.getResolvedType(init.get_type(), c))
				.equals(TreeConstants.No_type))
		{
			// getResolvedType for let <identifier> : <type_name> <- self in
			// ...'

			// The inferred type of the initialization and the type declaration
			// must be compatible
			if (type_decl.equals(TreeConstants.SELF_TYPE))
				if (init.get_type().equals(TreeConstants.SELF_TYPE))
					; // compatible
				else
				{
					err.semantError(c)
							.println(
									"Inferred type "
											+ init.get_type()
											+ " of initialization of "
											+ identifier
											+ " does not conform to identifier's declared type "
											+ type_decl + ".");
					return;
				}
			else if (!o.getSuperTypes(
					SemantUtil.getResolvedType(init.get_type(), c)).contains(
					type_decl))
			{
				err.semantError(c)
						.println(
								"Inferred type "
										+ init.get_type()
										+ " of initialization of "
										+ identifier
										+ " does not conform to identifier's declared type "
										+ type_decl + ".");
				return;
			}
		}

		o.enterScope(); // Scope of let starts

		o.addId(identifier, new TypeAndInit(type_decl, init));
		body.validateAndSetType(o, m, c, err);
		set_type(body.get_type());

		o.exitScope(); // Scope of let ends
	}

	/**
	 * Generates code for the 'let' expression.
	 * 
	 * @param s the output stream
	 * */
	public int code(class_ c, int nAR, CgenLookupTable tbl, PrintStream s)
	{

		s.println("# let expression");
		s.println();

		tbl.enterScope();

		s.println("# Save the self object before evaluating the "
				+ "initializer.");
		s.println("# Will need it while evaluating the body of the let");
		nAR = CgenSupport.emitPush(CgenSupport.ACC, nAR, s);
		s.println();

		// Hack. Incomplete
		if (init instanceof no_expr)
		{
			s.println("# No Initializer. Get the reference to the default "
					+ "value in $a0 ");
			CgenSupport.emitLoadAddress(CgenSupport.ACC,
					CgenUtilities.getDefaultObjectAddress(type_decl), s);
			s.println();
		}
		else
		{
			s.println("# Evaluate the initializer and get the reference in "
					+ "$a0");
			nAR = init.code(c, nAR, tbl, s);
			s.println();
		}

		s.println("# Temporarily hold the let identifier");
		CgenSupport.emitMove(CgenSupport.T1, CgenSupport.ACC, s);
		s.println();

		s.println("# Restore the self object");
		nAR = CgenSupport.emitPop(CgenSupport.ACC, nAR, s);
		s.println();

		s.println("# Save the let variable in the stack");
		nAR = CgenSupport.emitPush(CgenSupport.T1, nAR, s);
		// The offset is negative as the location of local variables are below
		// the FP
		tbl.addId(identifier, -(nAR - 1));
		s.println();

		s.println("# Evaluate let-body and get the reference in $a0");
		nAR = body.code(c, nAR, tbl, s);
		s.println();

		s.println("# Remove the let variable introduced");
		nAR = CgenSupport.emitPop(CgenSupport.T1, nAR, s);
		tbl.exitScope();
		s.println();

		s.println("# let expression ends");
		return nAR;
	}

}
