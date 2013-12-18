/**
 * 
 */
package cool.parser;

import java.util.Vector;

/**
 * Defines list phylum Formals
 * <p>
 * See <a href="ListNode.html">ListNode</a> for full documentation.
 */
public class Formals extends ListNode
{
	public final static Class<Formal> elementClass = Formal.class;

	/** Returns class of this lists's elements */
	public Class<Formal> getElementClass()
	{
		return elementClass;
	}

	protected Formals(int lineNumber, Vector<TreeNode> elements)
	{
		super(lineNumber, elements);
	}

	/** Creates an empty "Formals" list */
	public Formals(int lineNumber)
	{
		super(lineNumber);
	}

	/** Appends "Formal" element to this list */
	public Formals appendElement(TreeNode elem)
	{
		addElement(elem);
		return this;
	}

	public TreeNode copy()
	{
		return new Formals(lineNumber, copyElements());
	}
}