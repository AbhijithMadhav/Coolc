/**
 * 
 */
package cool.parser;

import java.util.Vector;

/**
 * Defines list phylum Classes
 * <p>
 * See <a href="ListNode.html">ListNode</a> for full documentation.
 */
public class Classes extends ListNode
{
	public final static Class<Class_> elementClass = Class_.class;

	/** Returns class of this lists's elements */
	public Class<Class_> getElementClass()
	{
		return elementClass;
	}

	protected Classes(int lineNumber, Vector<TreeNode> elements)
	{
		super(lineNumber, elements);
	}

	/** Creates an empty "Classes" list */
	public Classes(int lineNumber)
	{
		super(lineNumber);
	}

	/** Appends "Class_" element to this list */
	public Classes appendElement(TreeNode elem)
	{
		addElement(elem);
		return this;
	}

	public TreeNode copy()
	{
		return new Classes(lineNumber, copyElements());
	}
}
