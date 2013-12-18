package cool.cgen;
import java.io.PrintStream;

import cool.common.AbstractSymbol;
import cool.common.AbstractTable;

public class StringTable extends AbstractTable
{
	/**
	 * Creates a new StringSymbol object.
	 * 
	 * @see StringSymbol
	 * */
	protected AbstractSymbol getNewSymbol(String s, int len, int index)
	{
		return new StringSymbol(s, len, index);
	}

	/**
	 * Generates code for all string constants in the string table.
	 * 
	 * @param stringclasstag the class tag for String
	 * @param s the output stream
	 * */
	public void codeStringTable(int stringclasstag, PrintStream s)
	{
		for(AbstractSymbol sym : tbl.values())
			((StringSymbol)sym).codeDef(stringclasstag, s);
	}
}
