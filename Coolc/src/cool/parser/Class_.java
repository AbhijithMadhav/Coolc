/**
 * 
 */
package cool.parser;

import cool.common.AbstractSymbol;
import cool.common.PossibleNullDereferenceException;
import cool.common.UnresolvableSelfTypeException;

import cool.semant.ObjectTypeEnvironment;
import cool.semant.SemantError;

import cool.cgen.CgenLookupTable;

import java.io.PrintStream;

/** Defines simple phylum Class_ */
public abstract class Class_ extends TreeNode
{
	protected Class_(int lineNumber)
	{
		super(lineNumber);
	}

	public abstract void dump_with_types(PrintStream out, int n);

	public abstract AbstractSymbol getName();

	public abstract AbstractSymbol getParent();

	public abstract AbstractSymbol getFilename();

	public abstract Features getFeatures();

	public abstract void validate(ObjectTypeEnvironment o, SemantError err)
			throws PossibleNullDereferenceException,
			UnresolvableSelfTypeException;

	public abstract void code(CgenLookupTable tbl, PrintStream s);
}
