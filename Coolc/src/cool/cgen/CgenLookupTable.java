package cool.cgen;

import cool.common.AbstractSymbol;
import cool.common.SymbolTable;

import cool.parser.class_;

import cool.semant.ITree;

import java.util.Collection;

public class CgenLookupTable extends SymbolTable
{
	private ITree iTree;
	private DispatchTable dTab;
	private AttributeTable aTab;
	private CgenCaseTable cTab;

	public CgenLookupTable(ITree iTree)
	{
		this.iTree = iTree;
		dTab = new DispatchTable(iTree);
		aTab = new AttributeTable(iTree);
		cTab = new CgenCaseTable(iTree);
	}

	public Collection<DispatchRecord> getDispatchCandidates(AbstractSymbol sym)
	{
		return dTab.getDispatchCandidates(sym);
	}

	public int nDispatchCandidates(AbstractSymbol sym)
	{
		return dTab.getDispatchCandidates(sym).size();
	}

	public int getMethodOffset(AbstractSymbol cName, AbstractSymbol mName)
	{
		return dTab.getDispatchRecord(iTree.toINode(cName), mName).getOffset();
	}

	public int getClassTag(AbstractSymbol type)
	{
		return iTree.toINode(type).getClassTag();
	}

	public Collection<AttributeRecord> getCandidateAttributes(AbstractSymbol sym)
	{
		return aTab.getCandidateAttributes(sym);
	}

	public Collection<Integer> getCaseTableKeySet()
	{
		return cTab.keySet();
	}

	public Collection<Collection<Integer>> getCaseTableValues()
	{
		return cTab.values();
	}

	public class_ toClass_(AbstractSymbol type)
	{
		return iTree.toClass_(type);
	}
}
