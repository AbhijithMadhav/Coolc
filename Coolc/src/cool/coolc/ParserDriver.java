package cool.coolc;

import cool.common.Flags;
import cool.common.Utilities;

import cool.lexer.CoolTokenLexer;

import cool.parser.CoolParser;
import cool.parser.Program;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import java_cup.runtime.Symbol;

/**
 * The parser driver. Makes us of in-house lexer
 * 
 * @author kempa
 * 
 */
class ParserDriver
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

				// Parse
				CoolParser parser = new CoolParser(lexer);
				Symbol result = parser.parse();
				if (parser.omerrs > 0)
				{
					System.err.println("Compilation halted due to lex and "
							+ "parse errors");
					System.exit(1);
				}
				((Program) result.value).dump_with_types(System.out, 0);
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
