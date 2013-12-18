/**
 * 
 */
package cool.cgen;

import cool.semant.CNode;

/**
 * Data structure to generate code for an object attribute reference
 * 
 * @author kempa
 * 
 */
public class AttributeRecord
{
	private int offset; // Offset of this attribute in the object
	private CNode c; // Class to which the attribute belongs

	AttributeRecord(int offset, CNode c)
	{
		setOffset(offset);
		this.setClass_(c);
	}

	/**
	 * @return the offset
	 */
	public int getOffset()
	{
		return offset;
	}

	/**
	 * @param offset the offset to set
	 */
	void setOffset(int offset)
	{
		this.offset = offset;
	}

	/**
	 * @return the c
	 */
	public CNode getClass_()
	{
		return c;
	}

	/**
	 * @param c the c to set
	 */
	void setClass_(CNode c)
	{
		this.c = c;
	}
}