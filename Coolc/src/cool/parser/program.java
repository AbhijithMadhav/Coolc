/**
 * 
 */
package cool.parser;

import cool.common.AbstractTable;
import cool.common.PossibleNullDereferenceException;
import cool.common.UnresolvableSelfTypeException;
import cool.common.Utilities;

import cool.semant.CNode;
import cool.semant.ITree;
import cool.semant.ObjectTypeEnvironment;
import cool.semant.SemantChecker;
import cool.semant.SemantError;

import cool.cgen.CgenDataSection;
import cool.cgen.CgenLookupTable;

import java.io.PrintStream;

/**
 * Defines AST constructor 'program'.
 * <p>
 * See <a href="TreeNode.html">TreeNode</a> for full documentation.
 */
public class program extends Program
{
	protected Classes classes;

	/**
	 * Creates "program" AST node.
	 * 
	 * @param lineNumber the line in the source file from which this node came.
	 * @param a0 initial value for classes
	 */
	public program(int lineNumber, Classes a1)
	{
		super(lineNumber);
		classes = a1;
	}

	public TreeNode copy()
	{
		return new program(lineNumber, (Classes) classes.copy());
	}

	public void dump(PrintStream out, int n)
	{
		out.print(Utilities.pad(n) + "program\n");
		classes.dump(out, n + 2);
	}

	public void dump_with_types(PrintStream out, int n)
	{
		dump_line(out, n);
		out.println(Utilities.pad(n) + "_program");
		for (TreeNode t : classes.getElements())
			((Class_) t).dump_with_types(out, n + 1);
	}

	/**
	 * This method is the entry point to the semantic checker.
	 */
	public void semant() throws PossibleNullDereferenceException,
			UnresolvableSelfTypeException
	{
		SemantError err = new SemantError();
		@SuppressWarnings("unused")
		SemantChecker sc = new SemantChecker(classes, err);
		if (err.errors())
		{
			System.err
					.println("Compilation halted due to static semantic errors.");
			System.exit(1);
		}
		// if all is well construct a inheritance graph and check for a cycle
		ITree iTree = new ITree(classes);
		if (iTree.hasCycle())
			for (CNode s : iTree.cycle())
				err.semantError(s).println(
						"Class " + s.getName() + ", or an ancestor of "
								+ s.getName()
								+ ", is involved in an inheritance cycle.");

		validate(new ObjectTypeEnvironment(iTree), err);
		if (err.errors())
		{
			System.err.println("Compilation halted due to static semantic"
					+ " errors.");
			System.exit(1);
		}
	}

	public void validate(ObjectTypeEnvironment o, SemantError err)
			throws PossibleNullDereferenceException,
			UnresolvableSelfTypeException
	{
		o.enterScope(); // program scope start

		// Validate each class of the program
		for (TreeNode t : classes.getElements())
			((Class_) t).validate(o, err);

		o.exitScope(); // program scope end
	}

	/**
	 * This method is the entry point to the code generator. All of the work
	 * of the code generator takes place within CgenClassTable constructor.
	 * 
	 * @param s the output stream
	 * @see CgenClassTable
	 * */
	public void cgen(PrintStream s)
	{
		ITree iTree = new ITree(classes);

		/**
		 * The names of the classes are added to the string table so that
		 * string constant objects can be generated in the code. These
		 * objects are indexed in the class-name table and used by
		 * Object.type_name() to return string objects representative of
		 * class names of the invoking objects.
		 */
		for (CNode c : iTree.getClasses())
			AbstractTable.stringtable.addString(c.getName().toString());

		CgenLookupTable tbl = new CgenLookupTable(iTree);

		s.print("# start of generated code\n");

		CgenDataSection.code(iTree, tbl, s);

		s.println("# Begin - Class Methods");
		for (TreeNode t : classes.getElements())
			((class_) t).code(tbl, s);
		s.println("# End - Class Methods");

		s.print("\n# end of generated code\n");
	}

}
