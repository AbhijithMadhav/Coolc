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

import cool.cgen.CgenLookupTable;
import cool.cgen.CgenSupport;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.Vector;

/**
 * Defines AST constructor 'method'.
 * <p>
 * See <a href="TreeNode.html">TreeNode</a> for full documentation.
 */
public class method extends Feature
{
	private AbstractSymbol name;
	private Formals formals;
	private AbstractSymbol return_type;
	private Expression expr;

	/**
	 * Creates "method" AST node.
	 * 
	 * @param lineNumber the line in the source file from which this node came.
	 * @param a0 initial value for name
	 * @param a1 initial value for formals
	 * @param a2 initial value for return_type
	 * @param a3 initial value for expr
	 */
	public method(int lineNumber, AbstractSymbol a1, Formals a2,
			AbstractSymbol a3, Expression a4)
	{
		super(lineNumber);
		name = a1;
		formals = a2;
		return_type = a3;
		expr = a4;
	}

	public TreeNode copy()
	{
		return new method(lineNumber, copy_AbstractSymbol(name),
				(Formals) formals.copy(), copy_AbstractSymbol(return_type),
				(Expression) expr.copy());
	}

	public void dump(PrintStream out, int n)
	{
		out.print(Utilities.pad(n) + "method\n");
		dump_AbstractSymbol(out, n + 2, name);
		formals.dump(out, n + 2);
		dump_AbstractSymbol(out, n + 2, return_type);
		expr.dump(out, n + 2);
	}

	public void dump_with_types(PrintStream out, int n)
	{
		dump_line(out, n);
		out.println(Utilities.pad(n) + "_method");
		dump_AbstractSymbol(out, n + 2, name);
		for (TreeNode t : formals.getElements())
			((Formal) t).dump_with_types(out, n + 2);
		dump_AbstractSymbol(out, n + 2, return_type);
		expr.dump_with_types(out, n + 2);
	}

	public AbstractSymbol getName()
	{
		return name;
	}

	public Formals getFormals()
	{
		return formals;
	}

	public AbstractSymbol getReturnType()
	{
		return return_type;
	}

	public void validate(ObjectTypeEnvironment o, class_ c, SemantError err)
			throws PossibleNullDereferenceException,
			UnresolvableSelfTypeException
	{
		// is this a redefined method? If so, check on the number and type of
		// parameters
		if (!isOriginalMethod(c, o))
		{
			Iterator<TreeNode> eOriginal = getOriginalMethod(c, o).getFormals()
					.getElements().iterator();
			Iterator<TreeNode> eRedefined = formals.getElements().iterator();
			while (eOriginal.hasNext() && eRedefined.hasNext())
			{
				formal fOriginal = (formal) eOriginal.next();
				formal fRedefined = (formal) eRedefined.next();
				if (!fOriginal.getTypeDecl().equals(fRedefined.getTypeDecl()))
					err.semantError(c).println(
							"In redefined method " + name + ", parameter type "
									+ fRedefined.getTypeDecl()
									+ " is different from original type "
									+ fOriginal.getTypeDecl());
			}
			if (eOriginal.hasNext() || eRedefined.hasNext())
				err.semantError(c).println(
						"Incompatible number of formal parameters in "
								+ "redefined method " + name + ".");
		}

		// Validate each formal parameter
		Vector<AbstractSymbol> lst = new Vector<AbstractSymbol>();
		for (TreeNode t : formals.getElements())
		{
			formal f = (formal) t;
			if (f.getName().equals(TreeConstants.self))
				err.semantError(c).println(
						"'self' cannot be the name of a formal parameter.");
			if (lst.contains(f.getName()))
				err.semantError(c).println(
						"Formal parameter " + f.getName()
								+ " is multiply defined.");
			else
				lst.add(f.getName());
			f.validate(o, this, c, err);
		}

		// Is return type of method defined
		// getResolvedType for <name>(...) : SELF_TYPE
		if (!o.isDefined(SemantUtil.getResolvedType(return_type, c)))
		{
			err.semantError(c).println(
					"Return type " + return_type + " not defined.");
			return;
		}
		// Validate the body of the method only if the return type is defined as
		// the type of the body needs to be a subtype of the return type
		expr.validateAndSetType(o, this, c, err);

		// Check if the return type is compatible with the inferred type
		if (return_type.equals(TreeConstants.SELF_TYPE))
		{
			if (expr.get_type().equals(TreeConstants.SELF_TYPE))
				return; // compatible
			else
				// T <= SELF_TYPE(c) is false
				err.semantError(c).println(
						"Inferred return type " + expr.get_type()
								+ " of method " + name
								+ " does not conform to declared return type "
								+ return_type + ".");
		}
		else
		{
			if (!o.getSuperTypes(SemantUtil.getResolvedType(expr.get_type(), c))
					.contains(return_type))
			{
				err.semantError(c).println(
						"Inferred return type " + expr.get_type()
								+ " of method " + name
								+ " does not conform to declared return type "
								+ return_type + ".");
			}
		}
	}

	// look for name in in method env
	public boolean containsFormal(AbstractSymbol name)
	{
		for (TreeNode t : formals.getElements())
			if (((formal) t).getName().equals(name))
				return true;
		return false;
	}

	// look for name in in method env
	public formal getFormal(AbstractSymbol name)
	{
		for (TreeNode t : formals.getElements())
		{
			formal f = (formal) t;
			if (f.getName().equals(name))
				return f;
		}
		return null;
	}

	// Is this method original, and not one which is redefined and thus
	// overridding its namesake in a supertype?
	public boolean isOriginalMethod(class_ c, ObjectTypeEnvironment o)
			throws PossibleNullDereferenceException,
			UnresolvableSelfTypeException
	{
		for (AbstractSymbol supertype : o.getSuperTypes(c.getName()))
		{
			if (supertype.equals(c.getName()))
				continue;
			for (TreeNode t : o.toClass_(supertype).getFeatures().getElements())
			{
				if (t instanceof attr)
					continue;
				if (((method) t).getName().equals(name))
					return false;
			}
		}
		return true;
	}

	// Get the original method that this one is overridding
	public method getOriginalMethod(class_ c, ObjectTypeEnvironment o)
			throws PossibleNullDereferenceException,
			UnresolvableSelfTypeException
	{
		for (AbstractSymbol supertype : o.getSuperTypes(c.getName()))
		{
			if (supertype.equals(c.getName()))
				continue;
			for (TreeNode t : o.toClass_(supertype).getFeatures().getElements())
			{
				if (t instanceof attr)
					continue;
				method m = (method) t;
				if (m.getName().equals(name))
					return m;
			}
		}
		throw new PossibleNullDereferenceException(
				"Internal Error : getOriginalMethod : Trying to get the "
						+ "original method when this one is the one. Use "
						+ "isOriginalMethod() before.");
	}

	/**
	 * Generates code for this method
	 * 
	 * @param c The class_ object representing the method in which the method
	 *            is statically defined.
	 */
	public void code(class_ c, CgenLookupTable tbl, PrintStream str)
	{
		int nAR = 0; // number of items on the current activation record

		CgenSupport.emitMethodDef(c.getName(), name, str);

		str.println("# Set FP");
		CgenSupport.emitMove(CgenSupport.FP, CgenSupport.SP, str);
		str.println();

		str.println("# Save return address");
		nAR = CgenSupport.emitPush(CgenSupport.RA, nAR, str);
		str.println();

		// Add the addresses of the formal parameters, relative to the FP,
		// into the symbol table. Note that the arguments are in the caller
		// AR above the current AR's FP
		int offset = formals.getLength();
		for (TreeNode t : formals.getElements())
			tbl.addId(((formal) (t)).getName(), offset--);

		str.println("# Emit code for method body");
		nAR = expr.code(c, nAR, tbl, str); // Return address is pushed
		str.println();

		str.println("# Get return address");
		nAR = CgenSupport.emitPop(CgenSupport.RA, nAR, str);
		str.println();
		if (nAR != 0)
			System.err.println("Error: nAR = " + nAR);

		// callee responsibilities after method body execution
		if (formals.getLength() > 0)
		{
			str.println("# Pop AR");
			// This in effect is popping entries in the caller activation record
			// and this has to be accounted for after the dispatch of a method
			CgenSupport.emitPopAR(formals.getLength(), str);
			str.println();
		}

		str.println("# Return");
		CgenSupport.emitReturn(str);
		str.println();
	}
}
