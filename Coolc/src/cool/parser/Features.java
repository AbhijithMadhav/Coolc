/**
 * 
 */
package cool.parser;

import java.util.Vector;

/**
 * Defines list phylum Features
 * <p>
 * See <a href="ListNode.html">ListNode</a> for full documentation.
 */
public class Features extends ListNode
{
	public final static Class<Feature> elementClass = Feature.class;

	/** Returns class of this lists's elements */
	public Class<Feature> getElementClass()
	{
		return elementClass;
	}

	protected Features(int lineNumber, Vector<TreeNode> elements)
	{
		super(lineNumber, elements);
	}

	/** Creates an empty "Features" list */
	public Features(int lineNumber)
	{
		super(lineNumber);
	}

	/** Appends "Feature" element to this list */
	public Features appendElement(TreeNode elem)
	{
		addElement(elem);
		return this;
	}

	public TreeNode copy()
	{
		return new Features(lineNumber, copyElements());
	}

}