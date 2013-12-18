package cool.cgen;
import java.io.PrintStream;

import cool.common.AbstractSymbol;
import cool.common.AbstractTable;

public class IntTable extends AbstractTable
{
	/**
	 * Creates a new IntSymbol object.
	 * 
	 * @see IntSymbol
	 * */
	protected AbstractSymbol getNewSymbol(String s, int len, int index)
	{
		return new IntSymbol(s, len, index);
	}

	/**
	 * Generates code for all int constants in the int table.
	 * 
	 * @param intclasstag the class tag for Int
	 * @param s the output stream
	 * */
	void codeStringTable(int intclasstag, PrintStream s)
	{
		for (AbstractSymbol sym: tbl.values())
			((IntSymbol)sym).codeDef(intclasstag, s);
	}
}
