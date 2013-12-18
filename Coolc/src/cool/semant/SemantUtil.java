package cool.semant;

import cool.common.AbstractSymbol;
import cool.common.PossibleNullDereferenceException;
import cool.common.TreeConstants;
import cool.common.UnresolvableSelfTypeException;

import cool.parser.TypeAndInit;
import cool.parser.class_;
import cool.parser.method;

final public class SemantUtil
{
	private SemantUtil()
	{
	}

	/**
	 * Returns the type of 'name' in the environment defined by o, m, c(ct is
	 * considered included in c)
	 */
	public static AbstractSymbol typeLookup(AbstractSymbol name,
			ObjectTypeEnvironment o, method m, class_ c, SemantError err)
			throws PossibleNullDereferenceException,
			UnresolvableSelfTypeException
	{
		if (name.equals(TreeConstants.self))
			return TreeConstants.SELF_TYPE;
		else if ((TypeAndInit) o.lookup(name) != null)
			return ((TypeAndInit) o.lookup(name)).getType();
		else if (m.containsFormal(name))
			return m.getFormal(name).getTypeDecl();
		else if (c.containsAttr(name, o, err))
			return c.getAttr(name, o, err).getTypeDecl();
		else
		{
			err.semantError(c).println("Undeclared identifier " + name + ".");
			return TreeConstants.Object_;
		}
	}

	/**
	 * Resolves a type name.
	 * A type name is resolved if it is gaureented not to be a context dependent
	 * placeholder, the SELF_TYPE.
	 */
	public static AbstractSymbol getResolvedType(AbstractSymbol type,
			class_ container)
	{
		if (type.equals(TreeConstants.SELF_TYPE))
			return container.getName();
		return type;
	}
}
