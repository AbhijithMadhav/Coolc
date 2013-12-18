package cool.cgen;

import cool.common.AbstractSymbol;
import cool.common.Utilities;

import cool.parser.TreeNode;
import cool.parser.attr;
import cool.parser.method;

import cool.semant.CNode;
import cool.semant.ITree;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

// Done
/**
 * Data structure used to implement dynamic dispatch
 */
class DispatchTable
{
	/**
	 * The code for dynamic dispatch will need to check the class tag of the
	 * invoking object first. Based on this it needs to generate a jump which
	 * will lead to the execution of the specified method belonging to that
	 * class whose class tag is equal to that of the invoking object.
	 * 
	 * Thus, given a method name, a DispatchTable object must mainly be able
	 * to return the list of of classes in which a similarly named method exists
	 * 
	 * This is done by maintaining a table of key-value pairs where the key
	 * is the method name and the value is a list DispatchRecord objects.
	 * Each DispatchRecord object is representative of a possible dispatch
	 * candidate for the associated method key.
	 * 
	 */
	private Map<AbstractSymbol, Collection<DispatchRecord>> tbl;

	/**
	 * Constructor which constructs the above described table
	 * 
	 * @param iTree The inheritance tree
	 */
	DispatchTable(ITree iTree)
	{
		// only lookup required. No iteration through records required
		tbl = new HashMap<AbstractSymbol, Collection<DispatchRecord>>();

		// For each class in the program
		for (CNode c : iTree.getClasses())
		{
			/*
			 * The methods of a class includes that of all its ancestors. So
			 * iterate through the methods of its ancestors to add them to the
			 * dispatch table. Since the ancestors if a class A includes A
			 * itself, this iteration adds the methods of the the considered
			 * class.
			 */
			int mOffset = 0;
			for (CNode ancestor : c.getAncestors(Utilities.oldestFirst))
			{
				for (TreeNode t : ancestor.getFeatures().getElements())
				{
					if (t instanceof attr)
						continue;

					method m = (method) t;
					/*
					 * If a method in the considered class, c, overrides
					 * the method ancestor.m, there is no need to add it to
					 * the list of probable candidates associated with m.name.
					 * 
					 * This is how polymorphism is implemented, by making sure
					 * that an a method and its overridden version are associted
					 * with the respective appropriate class tags.
					 */
					if (!c.overridesMethod(ancestor, m.getName()))
					{
						DispatchRecord d = new DispatchRecord(c.getClassTag(),
								mOffset);

						if (tbl.containsKey(m.getName()))
							tbl.get(m.getName()).add(d);
						else
						{
							Collection<DispatchRecord> l = new LinkedList<DispatchRecord>();
							l.add(d);
							tbl.put(m.getName(), l);
						}
					}
					mOffset++;
				}
			}
		}
	}

	/**
	 * Get the dispatch candidates for the dispatch of the specified method name
	 * 
	 * @param mName The specified method
	 * @return The corresponding dispatch records
	 */
	Collection<DispatchRecord> getDispatchCandidates(AbstractSymbol mName)
	{
		return tbl.get(mName);
	}

	/**
	 * Get the dispatch record for the specified method
	 * 
	 * @param c The specified class in which the method exists
	 * @param mName The specified name of the method
	 * @return The sought dispatch method
	 */
	DispatchRecord getDispatchRecord(CNode c, AbstractSymbol mName)
	{
		for (DispatchRecord d : tbl.get(mName))
			if (d.getClassTag() == c.getClassTag())
				return d;
		return null;
	}
}
