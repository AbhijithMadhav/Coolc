package cool.cgen;

import cool.common.AbstractSymbol;

import cool.parser.TreeNode;
import cool.parser.attr;
import cool.parser.method;

import cool.semant.CNode;
import cool.semant.ITree;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Data structure used to lookup attributes of an object dynamically.
 * 
 * Note that in cool as in many other OO languages, it is not possible to
 * override the attributes of an inherited class. This table is used to only
 * to generate code to lookup attributes at runtime based on the evaluated
 * invoking object type. There is no polymorphism involved unlike the method
 * dispatch. Thus it is required to distinguish only between same named
 * attributes of different classes.
 * 
 * @author kempa
 * 
 */
class AttributeTable
{
	private Map<AbstractSymbol, Collection<AttributeRecord>> tbl;

	AttributeTable(ITree iTree)
	{
		// only lookup required. Iteration not required
		tbl = new HashMap<AbstractSymbol, Collection<AttributeRecord>>();

		/*
		 * For all the classes in the cool program just add the non-inherited
		 * attributes into the attribute table
		 */
		for (CNode c : iTree.getClasses())
		{
			int offset = c.getOffsetOfFirstNonInheritedAttribute();
			for (TreeNode t : c.getFeatures().getElements())
			{
				if (t instanceof method)
					continue;
				attr a = (attr) t;
				if (!tbl.containsKey(a.getName()))
				{
					Collection<AttributeRecord> lst = new LinkedList<AttributeRecord>();
					for (CNode cSubType : c.getSubtypes())
						lst.add(new AttributeRecord(offset, cSubType));
					tbl.put(a.getName(), lst);
				}
				else
					for (CNode cSubType : c.getSubtypes())
						tbl.get(a.getName()).add(
								new AttributeRecord(offset, cSubType));
				offset++;
			}
		}
	}

	/**
	 * Gets the candidate attributes of the specified attribute name
	 * 
	 * @param aName The specified attribute name
	 * @return A collection of the candidate attributes with the specified
	 *         attribute name
	 */
	Collection<AttributeRecord> getCandidateAttributes(AbstractSymbol name)
	{
		return tbl.get(name);
	}
}
