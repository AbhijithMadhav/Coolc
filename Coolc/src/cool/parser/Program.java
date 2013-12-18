package cool.parser;

import cool.common.PossibleNullDereferenceException;
import cool.common.UnresolvableSelfTypeException;

import cool.semant.ObjectTypeEnvironment;
import cool.semant.SemantError;

import java.io.PrintStream;

/** Defines simple phylum Program */
public abstract class Program extends TreeNode
{
	protected Program(int lineNumber)
	{
		super(lineNumber);
	}

	public abstract void dump_with_types(PrintStream out, int n);

	public abstract void semant() throws PossibleNullDereferenceException,
			UnresolvableSelfTypeException;

	public abstract void validate(ObjectTypeEnvironment o, SemantError err)
			throws PossibleNullDereferenceException,
			UnresolvableSelfTypeException;

	/**
	 * @param output
	 */
	public abstract void cgen(PrintStream output);
}
