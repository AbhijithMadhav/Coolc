/**
 * 
 */
package cool.parser;

import cool.common.AbstractSymbol;
import cool.common.PossibleNullDereferenceException;
import cool.common.UnresolvableSelfTypeException;
import cool.common.Utilities;

import cool.semant.ObjectTypeEnvironment;
import cool.semant.SemantError;

import cool.cgen.CgenLookupTable;

import java.io.PrintStream;

/** Defines simple phylum Expression */
public abstract class Expression extends TreeNode
{
	protected Expression(int lineNumber)
	{
		super(lineNumber);
	}

	private AbstractSymbol type = null;

	public AbstractSymbol get_type() throws PossibleNullDereferenceException
	{
		if (type == null)
			throw new PossibleNullDereferenceException(
					"Internal Error: get_type : Attempting to retrieve type before it is set.");
		return type;
	}

	public Expression set_type(AbstractSymbol s)
	{
		type = s;
		return this;
	}

	public abstract void dump_with_types(PrintStream out, int n);

	public void dump_type(PrintStream out, int n)
	{
		if (type != null)
			out.println(Utilities.pad(n) + ": " + type.toString());
		else
			out.println(Utilities.pad(n) + ": _no_type");
	}

	public abstract void validateAndSetType(ObjectTypeEnvironment o, method m,
			class_ c, SemantError err) throws PossibleNullDereferenceException,
			UnresolvableSelfTypeException;

	/**
	 * 
	 * @param n
	 * @param c Represents the class in which this expression lies
	 * @param m Represents the method in which this expression lies
	 * @param env
	 * @param s
	 */
	public abstract int code(class_ c, int nAR, CgenLookupTable tbl,
			PrintStream s);

}
