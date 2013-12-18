/**
 * 
 */
package cool.parser;

import cool.common.AbstractSymbol;

/**
 * A class to represent the type and initializer of a let variable.
 * This is needed as the object symbol table records have a name-value fields.
 * This class helps stick both the type and initializer expression together to
 * put as the value into the object symbol table
 */
public class TypeAndInit
{
	private AbstractSymbol type;
	private Expression init;

	public TypeAndInit(AbstractSymbol t, Expression e)
	{
		type = t;
		init = e;
	}

	public AbstractSymbol getType()
	{
		return type;
	}

	public Expression getInit()
	{
		return init;
	}
}