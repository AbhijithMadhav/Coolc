package cool.semant;

import cool.common.AbstractSymbol;
import cool.common.TreeConstants;
import cool.common.Utilities;

import cool.parser.Class_;
import cool.parser.TreeNode;
import cool.parser.attr;
import cool.parser.class_;
import cool.parser.method;

import cool.cgen.CgenSupport;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Vector;

/*class_ nodes in the AST lack pointers to parent classes(only names of
 * parents are stored as AbstractSymbols) among many other things. An
 * inheritance tree is thus constructed using INodes which include this
 * and many other useful details.*/
/**
 * Data structure representing a cool class in the cool inheritance tree, iTree.
 * Contains information about a class required for semantic analysis and code
 * generation.
 * 
 * @author kempa
 * 
 */
public class CNode extends class_ implements Comparable<CNode>
{
	// DFS: cycle finder
	boolean marked;
	boolean onStack;

	/** The parent of this node in the inheritance tree */
	private CNode parent;

	/** The children of this node in the inheritance tree */
	private Collection<CNode> children;

	private ITree iTree;

	/** Indicates a basic class */
	final static int Basic = 0;

	/** Indicates a class that came from a Cool program */
	final static int NotBasic = 1;

	/** Does this node correspond to a basic class? */
	private int basic_status;

	/** The class tag of the class this node represents */
	private int classTag;

	/** Used to assign unique class tags */
	private static int tag = 0;

	// must start with '0' as this is used to index the class-name table

	/**
	 * Constructs a new INode to represent class "c".
	 * 
	 * @param c the class
	 * @param iTree the inheritance tree to which this node belongs to
	 * @param basic_status is this class basic or not
	 * @param table the class table
	 * */
	CNode(Class_ c, ITree iTree, int basic_status)
	{
		super(0, c.getName(), c.getParent(), c.getFeatures(), c.getFilename());
		this.parent = null;
		this.children = new Vector<CNode>();
		this.iTree = iTree;
		this.basic_status = basic_status;
		if ((name.toString().equals("<basic class>")
				|| name.equals(TreeConstants.No_class)
				|| name.equals(TreeConstants.SELF_TYPE) || name
					.equals(TreeConstants.prim_slot)))
			classTag = -1;
		else
			classTag = tag++;
	}

	void addChild(CNode child)
	{
		children.add(child);
	}

	/**
	 * Gets the children of this class
	 * 
	 * @return the children
	 * */
	public Collection<CNode> getChildren()
	{
		return children;
	}

	/**
	 * Sets the parent of this class.
	 * 
	 * @param parent the parent
	 * */
	void setParentNd(CNode parent)
	{
		if (this.parent != null)
			Utilities.fatalError("parent already set in INode.setParent()");

		if (parent == null)
			Utilities.fatalError("null parent in INode.setParent() : "
					+ getName());
		this.parent = parent;
	}

	/**
	 * Gets the parent of this class
	 * 
	 * @return the parent
	 * */
	public CNode getParentNd()
	{
		return parent;
	}

	/**
	 * Returns true is this is a basic class.
	 * 
	 * @return true or false
	 * */
	boolean basic()
	{
		return basic_status == Basic;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(CNode nd)
	{
		if (this.getClassTag() < nd.getClassTag())
			return -1;
		else if (this.getClassTag() > nd.getClassTag())
			return 1;
		else
			return 0;
	}

	/**
	 * @return the classTag
	 */
	public int getClassTag()
	{
		return classTag;
	}

	/**
	 * Gets a list of ancestors of this class.
	 * The current node is also its own ancestor
	 * 
	 * @return
	 */
	public Collection<CNode> getAncestors(final boolean oldestFirst)
	{
		LinkedList<CNode> l = new LinkedList<CNode>();
		for (CNode i = this; i != iTree.getClsMap().get(TreeConstants.No_class); i = i
				.getParentNd())
			if (oldestFirst)
				l.addFirst(i);
			else
				l.add(i);
		return l;
	}

	Collection<AbstractSymbol> getSuperTypes(final boolean oldestFirst)
	{
		LinkedList<AbstractSymbol> l = new LinkedList<AbstractSymbol>();
		for (CNode i = this; i != iTree.getClsMap().get(TreeConstants.No_class); i = i
				.getParentNd())
			if (oldestFirst)
				l.addFirst(i.getName());
			else
				l.add(i.getName());
		return l;
	}

	/**
	 * Gets the attributes(inherited ones included) of the given node in
	 * parent-attributes-first order
	 * 
	 * @return A collection of all attributes of this class
	 */
	public Collection<attr> getAllAttributes()
	{
		Collection<attr> l = new LinkedList<attr>();
		for (CNode p : getAncestors(!Utilities.oldestFirst))
			for (TreeNode t : p.getFeatures().getElements())
			{
				if (t instanceof method)
					continue;
				l.add((attr) t);
			}
		return l;
	}

	/**
	 * Gives the offset of the first non-inherited attribute of this class
	 * 
	 * @return Offset of the first attribute of this class
	 */
	public int getOffsetOfFirstNonInheritedAttribute()
	{
		int offset = CgenSupport.DEFAULT_OBJFIELDS;

		for (CNode c : getAncestors(Utilities.oldestFirst))
		{
			if (c.getName().equals(name))
				break;
			for (TreeNode t : c.getFeatures().getElements())
				if (t instanceof attr)
					offset++;
		}
		return offset;
	}

	/**
	 * 
	 * Does this class have a method which overrides the method, ancestor.mName?
	 * 
	 * @return
	 */
	public boolean overridesMethod(CNode ancestor, AbstractSymbol mName)
	{
		for (CNode c : getAncestors(!Utilities.oldestFirst))
		{
			if (c.name.equals(ancestor.name))
				return false;

			for (TreeNode t : c.getFeatures().getElements())
			{
				if (t instanceof attr)
					continue;
				if (((method) t).getName().equals(mName))
					return true;
			}
		}
		return false;
	}

	/**
	 * Gives the sub-types of this class. The sub-types of a class includes
	 * itself
	 * 
	 * @return A collection containing the sub-types of this class
	 */
	public Collection<CNode> getSubtypes()
	{
		Queue<CNode> q = new LinkedList<CNode>();
		Queue<CNode> subTypes = new LinkedList<CNode>();

		// A work-list type algorithm
		q.add(this);
		while (!q.isEmpty())
		{
			CNode c = q.remove();
			subTypes.add(c);

			for (CNode child : c.getChildren())
				q.add(child);
		}
		return subTypes;
	}
}