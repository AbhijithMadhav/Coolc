/**
 * 
 */
package cool.parser;

import cool.common.PossibleNullDereferenceException;
import cool.common.UnresolvableSelfTypeException;

import cool.semant.ObjectTypeEnvironment;
import cool.semant.SemantError;

import java.io.PrintStream;

/** Defines simple phylum Feature */
public abstract class Feature extends TreeNode
{
	protected Feature(int lineNumber)
	{
		super(lineNumber);
	}

	public abstract void dump_with_types(PrintStream out, int n);

	public abstract void validate(ObjectTypeEnvironment o, class_ c,
			SemantError err) throws PossibleNullDereferenceException,
			UnresolvableSelfTypeException;

}
