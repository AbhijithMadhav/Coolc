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

import cool.cgen.AttributeRecord;
import cool.cgen.CgenLookupTable;
import cool.cgen.CgenSupport;

import java.io.PrintStream;

/**
 * Defines AST constructor 'assign'.
 * <p>
 * See <a href="TreeNode.html">TreeNode</a> for full documentation.
 */
public class assign extends Expression
{
	private AbstractSymbol name;
	private Expression expr;

	/**
	 * Creates "assign" AST node.
	 * 
	 * @param lineNumber the line in the source file from which this node came.
	 * @param a0 initial value for name
	 * @param a1 initial value for expr
	 */
	public assign(int lineNumber, AbstractSymbol a1, Expression a2)
	{
		super(lineNumber);
		name = a1;
		expr = a2;
	}

	public TreeNode copy()
	{
		return new assign(lineNumber, copy_AbstractSymbol(name),
				(Expression) expr.copy());
	}

	public void dump(PrintStream out, int n)
	{
		out.print(Utilities.pad(n) + "assign\n");
		dump_AbstractSymbol(out, n + 2, name);
		expr.dump(out, n + 2);
	}

	public void dump_with_types(PrintStream out, int n)
	{
		dump_line(out, n);
		out.println(Utilities.pad(n) + "_assign");
		dump_AbstractSymbol(out, n + 2, name);
		expr.dump_with_types(out, n + 2);
		dump_type(out, n);
	}

	public void validateAndSetType(ObjectTypeEnvironment o, method m, class_ c,
			SemantError err) throws PossibleNullDereferenceException,
			UnresolvableSelfTypeException
	{
		// Set the default type of expression in case of error
		set_type(TreeConstants.Object_);

		if (name.equals(TreeConstants.self))
		{
			err.semantError(c).println("Cannot assign to 'self'");
			return;
		}
		// Is the name bound to a declaration?
		if (o.lookup(name) == null && !m.containsFormal(name)
				&& !c.containsAttr(name, o, err))
			err.semantError(c).println(
					"Assignment to undeclared variable " + name + ".");

		// Validate the initializer
		expr.validateAndSetType(o, m, c, err);

		// Check if the LHS is compatible with the RHS
		AbstractSymbol tName = SemantUtil.typeLookup(name, o, m, c, err);
		if (!(tName == TreeConstants.SELF_TYPE && expr.get_type() == TreeConstants.SELF_TYPE))
			if (!o.getSuperTypes(SemantUtil.getResolvedType(expr.get_type(), c))
					.contains(tName))
			{
				// getResolvedType for '<name> <- self'
				err.semantError(c)
						.println(
								"Type "
										+ expr.get_type()
										+ " of "
										+ "assigned expression does not conform to declared type "
										+ tName + " of identifier " + name
										+ ".");
				return;
			}

		// set type of expression
		set_type(expr.get_type());
	}

	/**
	 * Generates code for the assign expression.
	 * 
	 * @param s the output stream
	 * */
	public int code(class_ c, int nAR, CgenLookupTable tbl, PrintStream s)
	{
		s.println("# Start - Assign expression");
		s.println();

		s.println("# Save self object");
		nAR = CgenSupport.emitPush(CgenSupport.ACC, nAR, s);
		s.println();

		s.println("# Evaluate the RHS and get the reference in $a0");
		nAR = expr.code(c, nAR, tbl, s);
		s.println();

		s.println("# Get the address of the identifier");
		if (tbl.lookup(name) != null)
		{
			s.println("# The identifier is a local variable. Store relative to $fp");
			CgenSupport.emitStore(CgenSupport.ACC, (Integer) tbl.lookup(name),
					CgenSupport.FP, s);

			s.println("# Discard stored self object");
			nAR = CgenSupport.emitPop(CgenSupport.T1, nAR, s);
		}
		else
		{
			s.println("# The identifier is an object attribute");

			s.println("# Temporarily cache the result");
			CgenSupport.emitMove(CgenSupport.T1, CgenSupport.ACC, s);
			s.println();

			s.println("# Restore the self object");
			nAR = CgenSupport.emitPop(CgenSupport.ACC, nAR, s);
			s.println();

			s.println("# Save the RHS");
			nAR = CgenSupport.emitPush(CgenSupport.T1, nAR, s);

			s.println("# Class tag of invoking object");
			nAR = CgenSupport.emitPush(CgenSupport.ACC, nAR, s);
			CgenSupport.emitFetchClassTag(CgenSupport.ACC, CgenSupport.ACC, s);

			int aLabel[] = new int[tbl.getCandidateAttributes(name).size()];
			int endLabel = CgenSupport.getLabel();
			int nLabel = 0;
			for (AttributeRecord a : tbl.getCandidateAttributes(name))
			{
				aLabel[nLabel] = CgenSupport.getLabel();
				CgenSupport.emitLoadImm(CgenSupport.T1, a.getClass_()
						.getClassTag(), s);
				CgenSupport.emitBeq(CgenSupport.ACC, CgenSupport.T1,
						aLabel[nLabel], s);
				nLabel++;
			}

			int nItems = nAR;
			nLabel = 0;
			for (AttributeRecord a : tbl.getCandidateAttributes(name))
			{

				nAR = nItems;
				CgenSupport.emitLabelDef(aLabel[nLabel], s);
				nAR = CgenSupport.emitPop(CgenSupport.ACC, nAR, s);
				nAR = CgenSupport.emitPop(CgenSupport.T1, nAR, s);
				CgenSupport.emitStore(CgenSupport.T1, a.getOffset(),
						CgenSupport.ACC, s);
				CgenSupport.emitMove(CgenSupport.ACC, CgenSupport.T1, s);

				CgenSupport.emitBranch(endLabel, s);
				nLabel++;
			}
			CgenSupport.emitLabelDef(endLabel, s);
		}
		s.println("# End of assign expression");
		s.println();
		return nAR;
	}
}
