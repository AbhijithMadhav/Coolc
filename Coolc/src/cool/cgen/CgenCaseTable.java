package cool.cgen;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import cool.common.Utilities;
import cool.semant.CNode;
import cool.semant.ITree;

/**
 * Data structure to generate code for the 'case' cool construct
 * 
 * The table contains mappings which give the ancestors of each class in
 * the child first order. This makes it easier to determine the LUB of a
 * type.
 * 
 * The LUB of the type of the case-expression is determined by sequentially
 * comparing each ancestor in the corresponding list(child-first order)
 * with all the types of the branches. The first matched ancestor is the LUB
 */

class CgenCaseTable
{
	/**
	 * Used to find the LUB of the type of the case expression from among its
	 * branches
	 **/
	private Map<Integer, Collection<Integer>> tbl;

	CgenCaseTable(ITree iTree)
	{
		tbl = new HashMap<Integer, Collection<Integer>>();

		// for all classes in the program
		for (CNode c : iTree.getClasses())
		{
			Collection<Integer> lst = new LinkedList<Integer>();
			for (CNode ancestor : c.getAncestors(!Utilities.oldestFirst))
				lst.add(ancestor.getClassTag());
			tbl.put(c.getClassTag(), lst);
		}
	}

	int size()
	{
		return tbl.size();
	}

	Collection<Collection<Integer>> values()
	{
		return tbl.values();
	}

	Collection<Integer> keySet()
	{
		return tbl.keySet();
	}
}
