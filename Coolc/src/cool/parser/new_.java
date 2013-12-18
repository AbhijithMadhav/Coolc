/**
 * 
 */
package cool.parser;

import cool.common.AbstractSymbol;
import cool.common.TreeConstants;
import cool.common.UnresolvableSelfTypeException;
import cool.common.Utilities;

import cool.semant.ObjectTypeEnvironment;
import cool.semant.SemantError;
import cool.semant.SemantUtil;

import cool.cgen.CgenLookupTable;
import cool.cgen.CgenSupport;

import java.io.PrintStream;

/**
 * Defines AST constructor 'new_'.
 * <p>
 * See <a href="TreeNode.html">TreeNode</a> for full documentation.
 */
public class new_ extends Expression
{
	private AbstractSymbol type_name;

	/**
	 * Creates "new_" AST node.
	 * 
	 * @param lineNumber the line in the source file from which this node came.
	 * @param a0 initial value for type_name
	 */
	public new_(int lineNumber, AbstractSymbol a1)
	{
		super(lineNumber);
		type_name = a1;
	}

	public TreeNode copy()
	{
		return new new_(lineNumber, copy_AbstractSymbol(type_name));
	}

	public void dump(PrintStream out, int n)
	{
		out.print(Utilities.pad(n) + "new_\n");
		dump_AbstractSymbol(out, n + 2, type_name);
	}

	public void dump_with_types(PrintStream out, int n)
	{
		dump_line(out, n);
		out.println(Utilities.pad(n) + "_new");
		dump_AbstractSymbol(out, n + 2, type_name);
		dump_type(out, n);
	}

	public void validateAndSetType(ObjectTypeEnvironment o, method m, class_ c,
			SemantError err) throws UnresolvableSelfTypeException
	{
		// check if name is known type
		// getResolvedType for 'new SELF_TYPE'
		if (!o.isDefined(SemantUtil.getResolvedType(type_name, c)))
			err.semantError(c).println(
					"'new' used with undefined class " + type_name + ".");

		// type check
		set_type(type_name);
	}

	/**
	 * Generates code for this expression. This method is to be completed
	 * in programming assignment 5. (You may add or remove parameters as
	 * you wish.)
	 * 
	 * @param s the output stream
	 * */
	public int code(class_ c, int nAR, CgenLookupTable tbl, PrintStream s)

	{
		s.println("# Expression 'new' + : " + nAR);
		s.println();
		if (type_name.equals(TreeConstants.SELF_TYPE))
		{
			s.println("# Get the class tag of the invoking object");
			CgenSupport.emitFetchClassTag(CgenSupport.ACC, CgenSupport.ACC, s);
			s.println();

			s.println("# Get the address to the prototype object of self");
			s.println();
			s.println("# Offset in object table at which required prototype");
			s.println("# object is obtained");
			CgenSupport.emitSll(CgenSupport.ACC, CgenSupport.ACC, 3, s);

			s.println("# Base of the object table");
			CgenSupport.emitLoadAddress(CgenSupport.T1,
					CgenSupport.CLASSOBJTAB, s);

			s.println("# Address of the required prototype object");
			CgenSupport.emitAdd(CgenSupport.ACC, CgenSupport.T1,
					CgenSupport.ACC, s);

			s.println("# Save address. Will need it to calculate the");
			s.println("# reference to the init method");
			nAR = CgenSupport.emitPush(CgenSupport.ACC, nAR, s);

			s.println("# Create new object");
			nAR = CgenSupport.emitPush(CgenSupport.FP, nAR, s);
			CgenSupport.emitLoad(CgenSupport.ACC, 0, CgenSupport.ACC, s); // How??
																			// Incomplete
			CgenSupport.emitJal("Object.copy", s);
			nAR = CgenSupport.emitPop(CgenSupport.FP, nAR, s);

			s.println("# Restore address of prototype object");
			nAR = CgenSupport.emitPop(CgenSupport.T1, nAR, s);
			s.println("# Address of init method");
			// CgenSupport.emitAddiu(CgenSupport.T1, CgenSupport.T1, 4, s);
			CgenSupport.emitLoad(CgenSupport.T1, 1, CgenSupport.T1, s); // How??
																		// Incomplete

			nAR = CgenSupport.emitPush(CgenSupport.FP, nAR, s);
			CgenSupport.emitJalr(CgenSupport.T1, s);
			nAR = CgenSupport.emitPop(CgenSupport.FP, nAR, s);
		}
		else
		{
			s.println("# Create new object of type " + type_name);
			nAR = CgenSupport.emitCallObjectCopy(type_name, nAR, s);
			s.println();
			// Attributes are initialized to default values or according to
			// their initializers by init call as this is a copy
			// of a prototype object
		}
		s.println("# End - Expression 'new'");
		s.println();
		return nAR;
	}
}
