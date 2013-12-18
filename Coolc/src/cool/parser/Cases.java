/**
 * 
 */
package cool.parser;

import java.util.Vector;

/**
 * Defines list phylum Cases
 * <p>
 * See <a href="ListNode.html">ListNode</a> for full documentation.
 */
public class Cases extends ListNode
{
	public final static Class<Case> elementClass = Case.class;

	/** Returns class of this lists's elements */
	public Class<Case> getElementClass()
	{
		return elementClass;
	}

	protected Cases(int lineNumber, Vector<TreeNode> elements)
	{
		super(lineNumber, elements);
	}

	/** Creates an empty "Cases" list */
	public Cases(int lineNumber)
	{
		super(lineNumber);
	}

	/** Appends "Case" element to this list */
	public Cases appendElement(TreeNode elem)
	{
		addElement(elem);
		return this;
	}

	public TreeNode copy()
	{
		return new Cases(lineNumber, copyElements());
	}
}