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

/**
 * Defines AST constructor 'class_'.
 * <p>
 * See <a href="TreeNode.html">TreeNode</a> for full documentation.
 */
public class class_ extends Class_
{
	protected AbstractSymbol name;
	protected AbstractSymbol parent;
	protected Features features;
	protected AbstractSymbol filename;

	/**
	 * Creates "class_" AST node.
	 * 
	 * @param lineNumber the line in the source file from which this node came.
	 * @param a0 initial value for name
	 * @param a1 initial value for parent
	 * @param a2 initial value for features
	 * @param a3 initial value for filename
	 */
	public class_(int lineNumber, AbstractSymbol a1, AbstractSymbol a2,
			Features a3, AbstractSymbol a4)
	{
		super(lineNumber);
		name = a1;
		parent = a2;
		features = a3;
		filename = a4;
	}

	public TreeNode copy()
	{
		return new class_(lineNumber, copy_AbstractSymbol(name),
				copy_AbstractSymbol(parent), (Features) features.copy(),
				copy_AbstractSymbol(filename));
	}

	public void dump(PrintStream out, int n)
	{
		out.print(Utilities.pad(n) + "class_\n");
		dump_AbstractSymbol(out, n + 2, name);
		dump_AbstractSymbol(out, n + 2, parent);
		features.dump(out, n + 2);
		dump_AbstractSymbol(out, n + 2, filename);
	}

	public void dump_with_types(PrintStream out, int n)
	{
		dump_line(out, n);
		out.println(Utilities.pad(n) + "_class");
		dump_AbstractSymbol(out, n + 2, name);
		dump_AbstractSymbol(out, n + 2, parent);
		out.print(Utilities.pad(n + 2) + "\"");
		Utilities.printEscapedString(out, filename.toString());
		out.println("\"\n" + Utilities.pad(n + 2) + "(");
		for (TreeNode t : features.getElements())
			((Feature) t).dump_with_types(out, n + 2);
		out.println(Utilities.pad(n + 2) + ")");
	}

	public AbstractSymbol getName()
	{
		return name;
	}

	public AbstractSymbol getParent()
	{
		return parent;
	}

	public AbstractSymbol getFilename()
	{
		return filename;
	}

	public Features getFeatures()
	{
		return features;
	}

	public void validate(ObjectTypeEnvironment o, SemantError err)
			throws PossibleNullDereferenceException,
			UnresolvableSelfTypeException
	{
		// Validate each feature of the class
		// A feature is either a method or attribute definition
		for (TreeNode t : features.getElements())
			((Feature) t).validate(o, this, err);
	}

	public boolean containsAttr(AbstractSymbol name, ObjectTypeEnvironment o,
			SemantError err) throws PossibleNullDereferenceException,
			UnresolvableSelfTypeException
	{
		for (AbstractSymbol supertype : o.getSuperTypes(getName()))
			for (TreeNode t : o.toClass_(supertype).getFeatures().getElements())
			{
				if (t instanceof method)
					continue;
				if (((attr) t).getName().equals(name))
					return true;
			}
		return false;
	}

	// look in class env
	public attr getAttr(AbstractSymbol name, ObjectTypeEnvironment o,
			SemantError err) throws PossibleNullDereferenceException,
			UnresolvableSelfTypeException
	{
		for (AbstractSymbol supertype : o.getSuperTypes(getName()))
			for (TreeNode t : o.toClass_(supertype).getFeatures().getElements())
			{
				if (t instanceof method)
					continue;
				attr a = (attr) t;
				if (a.getName().equals(name))
					return a;
			}
		throw new PossibleNullDereferenceException(
				"Internal Error : getAttr : No attribute called " + name
						+ ". Use containsAttr() first");
	}

	public boolean containsMethod(AbstractSymbol name, ObjectTypeEnvironment o,
			SemantError err) throws PossibleNullDereferenceException,
			UnresolvableSelfTypeException
	{
		for (AbstractSymbol supertype : o.getSuperTypes(getName()))
			for (TreeNode t : o.toClass_(supertype).getFeatures().getElements())
			{
				if (t instanceof attr)
					continue;

				if (((method) t).getName().equals(name))
					return true;

			}
		return false;
	}

	public method getMethod(AbstractSymbol name, ObjectTypeEnvironment o,
			SemantError err) throws PossibleNullDereferenceException,
			UnresolvableSelfTypeException
	{
		// A redefnition overrides a method in an ancestor.
		// Thus check for the method in this class first
		for (TreeNode t : features.getElements())
		{
			if (t instanceof attr)
				continue;
			method m = (method) t;
			if (m.getName().equals(name))
				return m;
		}

		// Now check in all the supertypes
		for (AbstractSymbol supertype : o.getSuperTypes(getName()))
			for (TreeNode t : o.toClass_(supertype).getFeatures().getElements())
			{
				if (t instanceof attr)
					continue;
				method m = (method) t;
				if (m.getName().equals(name))
					return m;
			}
		throw new PossibleNullDereferenceException(
				"Internal Error : getMethod : No attribute called " + name
						+ ". Use containsMethod() first");
	}

	/**
	 * code generator for each cool class
	 * 
	 * @param iTree
	 * @param tbl
	 * @param s
	 */
	public void code(CgenLookupTable tbl, PrintStream s)
	{
		for (TreeNode t : features.getElements())
		{
			if (t instanceof attr)
				continue;
			method m = ((method) t);
			tbl.enterScope();
			m.code(tbl.toClass_(name), tbl, s);
			tbl.exitScope();
		}
	}
}
