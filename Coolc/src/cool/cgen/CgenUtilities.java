package cool.cgen;

import cool.common.AbstractSymbol;
import cool.common.AbstractTable;
import cool.common.SymbolTable;
import cool.common.TreeConstants;

import cool.parser.TreeNode;
import cool.parser.attr;
import cool.parser.method;

import cool.semant.CNode;

public class CgenUtilities
{

	public static String getDefaultObjectAddress(AbstractSymbol type)
	{
		String defaultAddress;
		if (type.equals(TreeConstants.Int))
			defaultAddress = CgenSupport.INTCONST_PREFIX
					+ AbstractTable.inttable.lookup(CgenSupport.INT_DEFAULT).index;
		else if (type.equals(TreeConstants.Str))
			defaultAddress = CgenSupport.STRCONST_PREFIX
					+ AbstractTable.stringtable.lookup(CgenSupport.STR_DEFAULT).index;
		else if (type.equals(TreeConstants.Bool))
			defaultAddress = CgenSupport.BOOLCONST_PREFIX
					+ CgenSupport.BOOL_FALSE;
		else
			defaultAddress = CgenSupport.VOIDCONST_PREFIX;

		// System.out.println(type.toString() + " : " + defaultAddress);
		return defaultAddress;
	}

	/**
	 * Returns the address of variables which are either
	 * 1. Object attributes or
	 * 2. Parameters of class methods or
	 * 3. Variables introduced by the let expression
	 * 
	 * Address of an object attribute is the sum of the address of the invoking
	 * object(present in $a0) and the offset of attribute in the class of the
	 * invoking object.
	 * The class of the invoking object must be either the class in which the
	 * attribute is defined or one of its subclass. The offset of the attribute
	 * in a class and in any of its subclass is the same and thus the class in
	 * which the attribute is originally defined, c, can be used to calculate
	 * the offset.
	 * 
	 * In the later two cases,
	 * Address of an identifier is the sum of the address of the frame pointer
	 * of
	 * that particular activation and an offset, given by the position of the
	 * parameter in the parameter list.
	 * 
	 * @param name Variable whose address is to be found.
	 * @param c The class in which the name is defined.
	 * @param env Symbol table containing offsets of formal parameters.
	 * @return A <register, offset> tuple. The 'offset' specifies the number of
	 *         words.
	 */
	// Incomplete - c is the class in which the name is found. c should be the
	// self object
	static offsetAddress getAddress(AbstractSymbol name, CNode c,
			SymbolTable env)
	{
		int offset = 0;
		if (env.lookup(name) != null)
		{
			// Cases 2 and 3
			offset = (Integer) (env.lookup(name));

			// The offset is negative if it is a local variable
			// The offset is positive the name is a function parameter
			return new offsetAddress(offset, CgenSupport.FP);
		}
		else
		{
			// Case 1
			offset = CgenSupport.DEFAULT_OBJFIELDS;
			for (TreeNode t : c.getFeatures().getElements())
			{
				if (t instanceof method)
					continue;
				if (((attr) t).getName().equals(name))
					return new offsetAddress(offset, CgenSupport.ACC);
			}
			// throw new AddressNotFoundException("");
			return null;
		}
	}
}

class offsetAddress
{
	int offset;
	String reg;

	public offsetAddress(int offset, String reg)
	{
		this.offset = offset;
		this.reg = reg;
	}
}