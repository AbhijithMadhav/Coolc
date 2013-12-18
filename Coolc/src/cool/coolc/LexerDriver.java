package cool.coolc;

import cool.common.Flags;
import cool.common.Utilities;

import cool.lexer.CoolTokenLexer;

import cool.parser.TokenConstants;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import java_cup.runtime.Symbol;

/**
 * 
 * The scanner driver
 * 
 */
class LexerDriver
{

	public static void main(String[] args)
	{
		args = Flags.handleFlags(args);

		for (int i = 0; i < args.length; i++)
		{
			FileReader file = null;
			try
			{
				file = new FileReader(args[i]);

				// Scan
				CoolTokenLexer lexer = new CoolTokenLexer(file);
				lexer.set_filename(args[i]);
				Symbol s;
				while ((s = lexer.next_token()).sym != TokenConstants.EOF)
					Utilities.dumpToken(System.out, lexer.curr_lineno(), s);
			}
			catch (FileNotFoundException ex)
			{
				Utilities.fatalError("Could not open input file " + args[i]);
			}
			catch (IOException ex)
			{
				Utilities.fatalError("Unexpected exception in lexer");
			}
			catch (Exception ex)
			{
				ex.printStackTrace(System.err);
				Utilities.fatalError("Unexpected exception in parser");
			}
		}
	}
}
