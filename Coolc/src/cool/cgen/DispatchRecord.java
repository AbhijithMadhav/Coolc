/**
 * 
 */
package cool.cgen;

/**
 * Data structure which contains information required for the dispatch of an
 * associated method
 * 
 * @author kempa
 * 
 */

public class DispatchRecord
{
	private int classtag; // The class in which the method is defined
	private int offset; // Offset of this method in the object dispatch table

	DispatchRecord(int classtag, int offset)
	{
		this.classtag = classtag;
		this.offset = offset;
	}

	public int getClassTag()
	{
		return classtag;
	}

	public int getOffset()
	{
		return offset;
	}
}