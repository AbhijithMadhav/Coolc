/**
 * 
 */
package cool.common;

public class UnresolvableSelfTypeException extends Exception
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3868617949554612816L;

	UnresolvableSelfTypeException()
	{
		super("Internal Error: Unresolved SELF_TYPE");
	}
}
