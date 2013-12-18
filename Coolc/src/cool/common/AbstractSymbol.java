package cool.common;
/**
 * A String table entry(StringTable, IntTable, IdTable)
 * 
 * There are three kinds of string table entries:
 * <ul>
 * <li>a true string,
 * <li>a string representation of an identifier, and
 * <li>a string representation of an integer.
 * </ul>
 * 
 * The 'AbstractSymbol' object associates cool string constants with integers.
 * The code generator which generates code for each cool string constant makes
 * use of these associated integers to generate references whenever the
 * respective string constants are used in the cool program.
 * 
 * codeDef and codeRef (defined by subclasses) are used by the code to produce
 * definitions and references (respectively) to constants.
 * 
 * @see AbstractTable
 * */
public abstract class AbstractSymbol
{
	/** The stored string */
	protected String str;

	/**
	 * The index of this entry in the string table. Used to define and generate
	 * references to the associated string constant
	 */
	public int index;

	/**
	 * Constructs a new table entry.
	 * 
	 * @param str the entry string
	 * @param len the length of the str prefix that is actually used
	 * @param index the table index
	 * */
	protected AbstractSymbol(String str, int len, int index)
	{
		this.str = str.length() == len ? str : str.substring(0, len);
		this.index = index;
	}

	/**
	 * Tests if the string argument is equal to the string in this symbol.
	 * 
	 * @param str the string to compare
	 * @return true if the strings are equal
	 * */
	boolean equalString(String str, int len)
	{
		String other = str.length() == len ? str : str.substring(0, len);
		return this.str.equals(other);
	}

	/**
	 * Tests if the index argument is equal to the index of this symbol.
	 * 
	 * It is only meaningful to compare indices of symbols from the
	 * same string table.
	 * 
	 * @param index the index to compare
	 * @return true if the indices are equal
	 * */
	/*
	 * boolean equalsIndex(int index)
	 * {
	 * return this.index == index;
	 * }
	 */

	/**
	 * Tests if two symbols are equal.
	 * 
	 * Symbol equality is equivalent to equality of their indices, so it
	 * is only meaningful to compare symbols that came from the same
	 * string table.
	 * 
	 * @param another the other symbol
	 * @return true if the symbols are equal
	 * */
	public boolean equals(Object another)
	{
		return (another instanceof AbstractSymbol)
				&& ((AbstractSymbol) another).index == this.index;
	}

	/** Returns a printable representation of this symbol. */
	public String toString()
	{
		return str;
	}

	/** Returns a copy of this symbol */
	public abstract Object clone();
}
