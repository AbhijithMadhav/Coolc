package cool.semant;
import java.io.PrintStream;

import cool.common.AbstractSymbol;
import cool.parser.TreeNode;
import cool.parser.class_;

public class SemantError
{
	// For error reporting
	private int semantErrors;
	private PrintStream errorStream;

	public SemantError()
	{
		semantErrors = 0;
		errorStream = System.err;
	}

	/** Returns true if there are any static semantic errors. */
	public boolean errors()
	{
		return semantErrors != 0;
	}

	/**
	 * Increments semantic error count and returns the print stream for
	 * error messages.
	 * 
	 * @return a print stream to which the error message is
	 *         to be printed.
	 * 
	 * */
	public PrintStream semantError()
	{
		semantErrors++;
		return errorStream;
	}

	/**
	 * Prints line number and file name of the given class.
	 * 
	 * Also increments semantic error count.
	 * 
	 * @param c the class
	 * @return a print stream to which the rest of the error message is
	 *         to be printed.
	 * 
	 * */
	public PrintStream semantError(class_ c)
	{
		return semantError(c.getFilename(), c);
	}

	/**
	 * Prints the file name and the line number of the given tree node.
	 * 
	 * Also increments semantic error count.
	 * 
	 * @param filename the file name
	 * @param t the tree node
	 * @return a print stream to which the rest of the error message is
	 *         to be printed.
	 * 
	 * */
	private PrintStream semantError(AbstractSymbol filename, TreeNode t)
	{
		errorStream.print(filename + ":" + t.getLineNumber() + ": ");
		return semantError();
	}
}
