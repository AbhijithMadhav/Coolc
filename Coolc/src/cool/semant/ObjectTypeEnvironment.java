package cool.semant;

import cool.common.AbstractSymbol;
import cool.common.PossibleNullDereferenceException;
import cool.common.SymbolTable;
import cool.common.UnresolvableSelfTypeException;

import cool.parser.class_;

import java.util.Collection;

public class ObjectTypeEnvironment extends SymbolTable
{
	private ITree iTree;

	public ObjectTypeEnvironment(ITree iTree)
	{
		this.iTree = iTree;
	}

	public Collection<AbstractSymbol> getSuperTypes(AbstractSymbol type)
	{
		return iTree.getSuperTypes(type);
	}

	public class_ toClass_(AbstractSymbol type)
	{
		return iTree.toClass_(type);
	}

	public boolean isDefined(AbstractSymbol type)
	{
		return iTree.isDefined(type);
	}

	public AbstractSymbol lub(AbstractSymbol t1, AbstractSymbol t2)
			throws PossibleNullDereferenceException,
			UnresolvableSelfTypeException
	{
		return iTree.lub(t1, t2);

	}
}
