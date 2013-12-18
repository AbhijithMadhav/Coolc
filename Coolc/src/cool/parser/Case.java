/**
 * 
 */
package cool.parser;

import cool.common.PossibleNullDereferenceException;
import cool.common.UnresolvableSelfTypeException;

import cool.semant.ObjectTypeEnvironment;
import cool.semant.SemantError;

import java.io.PrintStream;

/** Defines simple phylum Case */
public abstract class Case extends TreeNode
{
	protected Case(int lineNumber)
	{
		super(lineNumber);
	}

	public abstract void dump_with_types(PrintStream out, int n);

	public abstract void validate(ObjectTypeEnvironment o, method m, class_ c,
			SemantError err) throws PossibleNullDereferenceException,
			UnresolvableSelfTypeException;

}