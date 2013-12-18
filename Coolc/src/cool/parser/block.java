package cool.parser;

import cool.common.PossibleNullDereferenceException;
import cool.common.UnresolvableSelfTypeException;
import cool.common.Utilities;

import cool.semant.ObjectTypeEnvironment;
import cool.semant.SemantError;

import cool.cgen.CgenLookupTable;
import cool.cgen.CgenSupport;

import java.io.PrintStream;
import java.util.Iterator;

/**
 * Defines AST constructor 'block'.
 * <p>
 * See <a href="TreeNode.html">TreeNode</a> for full documentation.
 */
public class block extends Expression
{
	private Expressions body;

	/**
	 * Creates "block" AST node.
	 * 
	 * @param lineNumber the line in the source file from which this node came.
	 * @param a0 initial value for body
	 */
	public block(int lineNumber, Expressions a1)
	{
		super(lineNumber);
		body = a1;
	}

	public TreeNode copy()
	{
		return new block(lineNumber, (Expressions) body.copy());
	}

	public void dump(PrintStream out, int n)
	{
		out.print(Utilities.pad(n) + "block\n");
		body.dump(out, n + 2);
	}

	public void dump_with_types(PrintStream out, int n)
	{
		dump_line(out, n);
		out.println(Utilities.pad(n) + "_block");
		for (TreeNode t : body.getElements())
			((Expression) t).dump_with_types(out, n + 2);
		dump_type(out, n);
	}

	public void validateAndSetType(ObjectTypeEnvironment o, method m, class_ c,
			SemantError err) throws PossibleNullDereferenceException,
			UnresolvableSelfTypeException
	{
		// Validate each expression and set its type
		for (TreeNode t : body.getElements())
		{
			Expression expr = (Expression) t;
			expr.validateAndSetType(o, m, c, err);
			set_type(expr.get_type());
		}
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
		s.println("# Start of block");
		s.println();

		int n = 0;
		for (@SuppressWarnings("unused")
		TreeNode t : body.getElements())
			n++;

		Iterator<TreeNode> i = body.getElements().iterator();
		for (; n > 1; n--)
		{
			s.println("# Save self object before evaluating this expression.");
			s.println("# Will need it to evaluate the subsequent expressions");
			nAR = CgenSupport.emitPush(CgenSupport.ACC, nAR, s);
			s.println();

			s.println("# Evaluate the expression and "
					+ "get the reference in $a0");
			nAR = ((Expression) (i.next())).code(c, nAR, tbl, s);
			s.println();

			s.println("# Retrieve self object before evaluating the next "
					+ "expression");
			nAR = CgenSupport.emitPop(CgenSupport.ACC, nAR, s);
			s.println();
		}
		s.println("# Evaluate the last expression and "
				+ "get the reference in $a0");
		nAR = ((Expression) (i.next())).code(c, nAR, tbl, s);
		s.println();

		s.println("# End of block");
		s.println();
		return nAR;
	}
}
