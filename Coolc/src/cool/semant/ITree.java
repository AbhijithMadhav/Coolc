package cool.semant;

import cool.common.AbstractSymbol;
import cool.common.AbstractTable;
import cool.common.PossibleNullDereferenceException;
import cool.common.TreeConstants;
import cool.common.UnresolvableSelfTypeException;
import cool.common.Utilities;

import cool.parser.Classes;
import cool.parser.Features;
import cool.parser.Formals;
import cool.parser.TreeNode;
import cool.parser.attr;
import cool.parser.class_;
import cool.parser.formal;
import cool.parser.method;
import cool.parser.no_expr;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Stack;

/**
 * Inheritance tree
 * Represents the inheritance relationship between cool classes in a program.
 * 
 * @author kempa
 * 
 */
public class ITree
{
	/** Root of the tree */
	private CNode root;

	/**
	 * cool class names(AbstractSymbol) to cool class objects(INode) mappings
	 * 
	 * A LinkedHashMap is chosen because there is a need to iterate as well
	 * as do lookups. Maps are good for lookups and the links between the
	 * keys of the LinkedHashMap make the iteration a O(n) instead of
	 * O(capacity)
	 */
	private LinkedHashMap<AbstractSymbol, CNode> clsMap;

	/** Helps determining if there are cyclic inheritances in the cool program */
	private CycleFinder cf;

	/**
	 * Constructor
	 * 1. Constructs nodes representing all classes in the cool program and
	 * strings them together into suitable lists(dummyCls, basicCls, userCls).
	 * 2. Creates mappings from cool class names to INode objects
	 * 4. Constructs the inheritance inheritance tree
	 * 3. Determines if there are cyclic inheritances in the cool program
	 * 
	 * @param cls user defined cool classes
	 */
	public ITree(Classes cls)
	{
		/*
		 * Create a class name(AbstractSymbol) to INode symbol mapping. This is
		 * needed during the construction of the inheritance tree when the INode
		 * of a parent class is needed given the name of the parent class in the
		 * class_ structure of the child
		 */
		setClsMap(new LinkedHashMap<AbstractSymbol, CNode>());

		AbstractSymbol basicClassFilename = AbstractTable.stringtable
				.addString("<basic class>");

		// Dummy classes
		getClsMap().put(
				TreeConstants.No_class,
				new CNode(new class_(0, TreeConstants.No_class,
						TreeConstants.No_class, new Features(0),
						basicClassFilename), this, CNode.Basic));
		// To do: Why is this needed?
		getClsMap().put(
				TreeConstants.SELF_TYPE,
				new CNode(new class_(0, TreeConstants.SELF_TYPE,
						TreeConstants.No_class, new Features(0),
						basicClassFilename), this, CNode.Basic));
		// To do: Why is this needed?
		getClsMap().put(
				TreeConstants.prim_slot,
				new CNode(new class_(0, TreeConstants.prim_slot,
						TreeConstants.No_class, new Features(0),
						basicClassFilename), this, CNode.Basic));

		/* Basic classes */
		// There's no need for method bodies -- these are already built into
		// the runtime system.

		// The Object class has no parent class. Its methods are
		// cool_abort() : Object aborts the program
		// type_name() : Str returns a string representation
		// of class name
		// copy() : SELF_TYPE returns a copy of the object

		getClsMap().put(
				TreeConstants.Object_,
				new CNode(new class_(0, TreeConstants.Object_,
						TreeConstants.No_class, new Features(0)
								.appendElement(
										new method(0, TreeConstants.cool_abort,
												new Formals(0),
												TreeConstants.Object_,
												new no_expr(0)))
								.appendElement(
										new method(0, TreeConstants.type_name,
												new Formals(0),
												TreeConstants.Str, new no_expr(
														0)))
								.appendElement(
										new method(0, TreeConstants.copy,
												new Formals(0),
												TreeConstants.SELF_TYPE,
												new no_expr(0))),
						basicClassFilename), this, CNode.Basic));

		// The IO class inherits from Object. Its methods are
		// out_string(Str) : SELF_TYPE writes a string to the output
		// out_int(Int) : SELF_TYPE "    an int    " "     "
		// in_string() : Str reads a string from the input
		// in_int() : Int "   an int     " "     "

		getClsMap()
				.put(TreeConstants.IO,
						new CNode(
								new class_(
										0,
										TreeConstants.IO,
										TreeConstants.Object_,
										new Features(0)
												.appendElement(
														new method(
																0,
																TreeConstants.out_string,
																new Formals(0)
																		.appendElement(new formal(
																				0,
																				TreeConstants.arg,
																				TreeConstants.Str)),
																TreeConstants.SELF_TYPE,
																new no_expr(0)))
												.appendElement(
														new method(
																0,
																TreeConstants.out_int,
																new Formals(0)
																		.appendElement(new formal(
																				0,
																				TreeConstants.arg,
																				TreeConstants.Int)),
																TreeConstants.SELF_TYPE,
																new no_expr(0)))
												.appendElement(
														new method(
																0,
																TreeConstants.in_string,
																new Formals(0),
																TreeConstants.Str,
																new no_expr(0)))
												.appendElement(
														new method(
																0,
																TreeConstants.in_int,
																new Formals(0),
																TreeConstants.Int,
																new no_expr(0))),
										basicClassFilename), this, CNode.Basic));

		// The Int class has no methods and only a single attribute, the
		// "val" for the integer.

		getClsMap().put(
				TreeConstants.Int,
				new CNode(new class_(0, TreeConstants.Int,
						TreeConstants.Object_,
						new Features(0).appendElement(new attr(0,
								TreeConstants.val, TreeConstants.prim_slot,
								new no_expr(0))), basicClassFilename), this,
						CNode.Basic));

		// Bool also has only the "val" slot.
		getClsMap().put(
				TreeConstants.Bool,
				new CNode(new class_(0, TreeConstants.Bool,
						TreeConstants.Object_,
						new Features(0).appendElement(new attr(0,
								TreeConstants.val, TreeConstants.prim_slot,
								new no_expr(0))), basicClassFilename), this,
						CNode.Basic));

		// The class Str has a number of slots and operations:
		// val the length of the string
		// str_field the string itself
		// length() : Int returns length of the string
		// concat(arg: Str) : Str performs string concatenation
		// substr(arg: Int, arg2: Int): Str substring selection

		getClsMap()
				.put(TreeConstants.Str,
						new CNode(
								new class_(
										0,
										TreeConstants.Str,
										TreeConstants.Object_,
										new Features(0)
												.appendElement(
														new attr(
																0,
																TreeConstants.val,
																TreeConstants.Int,
																new no_expr(0)))
												.appendElement(
														new attr(
																0,
																TreeConstants.str_field,
																TreeConstants.prim_slot,
																new no_expr(0)))
												.appendElement(
														new method(
																0,
																TreeConstants.length,
																new Formals(0),
																TreeConstants.Int,
																new no_expr(0)))
												.appendElement(
														new method(
																0,
																TreeConstants.concat,
																new Formals(0)
																		.appendElement(new formal(
																				0,
																				TreeConstants.arg,
																				TreeConstants.Str)),
																TreeConstants.Str,
																new no_expr(0)))
												.appendElement(
														new method(
																0,
																TreeConstants.substr,
																new Formals(0)
																		.appendElement(
																				new formal(
																						0,
																						TreeConstants.arg,
																						TreeConstants.Int))
																		.appendElement(
																				new formal(
																						0,
																						TreeConstants.arg2,
																						TreeConstants.Int)),
																TreeConstants.Str,
																new no_expr(0))),
										basicClassFilename), this, CNode.Basic));

		/* User classes */
		for (TreeNode t : cls.getElements())
			getClsMap().put(((class_) t).getName(),
					new CNode((class_) t, this, CNode.NotBasic));

		// Construct the inheritance tree.
		root = getClsMap().get(TreeConstants.Object_);
		for (CNode i : getClsMap().values())
		{
			i.setParentNd(getClsMap().get(i.getParent()));
			(getClsMap().get(i.getParent())).addChild(i);
		}

		// Detect cycles
		cf = new CycleFinder(getRoot());
	}

	/**
	 * @return the clsMap
	 */
	public LinkedHashMap<AbstractSymbol, CNode> getClsMap()
	{
		return clsMap;
	}

	/**
	 * @param clsMap the clsMap to set
	 */
	public void setClsMap(LinkedHashMap<AbstractSymbol, CNode> clsMap)
	{
		this.clsMap = clsMap;
	}

	public CNode toINode(AbstractSymbol type)
	{
		return getClsMap().get(type);
	}

	public class_ toClass_(AbstractSymbol type)
	{
		return toINode(type);
	}

	public Collection<CNode> getClasses()
	{
		LinkedHashMap<AbstractSymbol, CNode> t = new LinkedHashMap<AbstractSymbol, CNode>(
				getClsMap());
		t.remove(TreeConstants.No_class);
		t.remove(TreeConstants.SELF_TYPE);
		t.remove(TreeConstants.prim_slot);
		return t.values();
	}

	CNode getRoot()
	{
		return root;
	}

	boolean isDefined(AbstractSymbol type)
	{
		return getClsMap().get(type) != null;
	}

	/**
	 * Gets a list of supertypes of the specified symbol in a given context.
	 * Note: The specified type is also its supertype
	 * 
	 * @param type The type whose supertypes is sought
	 * @return A list of supertypes in the increasing order of inheritance.
	 */
	Collection<AbstractSymbol> getSuperTypes(AbstractSymbol type)
	{
		return toINode(type).getSuperTypes(Utilities.oldestFirst);
	}

	/**
	 * Determines the least upper bound of two specified types.
	 * 
	 * The inheritance relationship in cool is a tree(due to single
	 * inheritance). Thus consider the paths from the root object to the
	 * specified types. As supertypes(type) returns the supertypes in the
	 * order of the decreasing types, the lub is the type at which the path
	 * bisects
	 * 
	 */
	AbstractSymbol lub(AbstractSymbol t1, AbstractSymbol t2)
			throws PossibleNullDereferenceException,
			UnresolvableSelfTypeException
	{
		if (t1.equals(TreeConstants.No_type))
			return t2;
		else if (t2.equals(TreeConstants.No_type))
			return t1;

		AbstractSymbol lub = TreeConstants.Object_;
		for (Iterator<AbstractSymbol> i1 = getSuperTypes(t1).iterator(), i2 = getSuperTypes(
				t2).iterator(); i1.hasNext() && i2.hasNext();)
		{
			AbstractSymbol a1 = i1.next();
			AbstractSymbol a2 = i2.next();
			if (a1 != a2)
			{
				return lub;
			}
			lub = a1; // or a2
		}
		return lub;
	}

	public boolean hasCycle()
	{
		return cf.hasCycle();
	}

	public Iterable<CNode> cycle()
	{
		return cf.cycle;
	}

	class CycleFinder
	{
		Collection<CNode> cycle = null;

		CycleFinder(CNode root)
		{
			dfs(root);
		}

		void dfs(CNode root)
		{
			root.marked = true;
			root.onStack = true;
			for (CNode i : root.getChildren())
			{
				CNode c = i;
				if (!c.marked)
				{
					c.marked = true;
					dfs(c);
				}
				else if (c.onStack)
				{
					Stack<CNode> s = new Stack<CNode>();
					for (CNode i1 = c.getParentNd(); i1 != c; i1 = i1
							.getParentNd())
						s.add(i1);

					while (!s.empty())
						cycle.add(s.pop());
				}
			}
			root.onStack = false;

		}

		boolean hasCycle()
		{
			return cycle != null;
		}

		Iterable<CNode> cycle()
		{
			return cycle;
		}
	}
}
