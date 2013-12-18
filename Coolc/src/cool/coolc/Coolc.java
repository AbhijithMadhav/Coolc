package cool.coolc;

import cool.common.Flags;
import cool.common.Utilities;

import cool.lexer.CoolTokenLexer;

import cool.parser.CoolParser;
import cool.parser.Program;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Makes us of inhouse components
 * 
 */
class Coolc
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
				Object result = parser.parse().value;
				if (parser.omerrs > 0)
				{
					System.err.println("Compilation halted due to lex and "
							+ "parse errors");
					System.exit(1);
				}

				// Semantic analysis
				((Program) result).semant();

				// Generate code
				PrintStream output = System.out;
				String filename = null;
				if (Flags.out_filename == null)
				{
					if (Flags.in_filename != null)
						filename = Flags.in_filename.substring(0,
								Flags.in_filename.lastIndexOf('.')) + ".s";
				}
				else
					filename = Flags.out_filename;

				if (filename != null)
					try
					{
						output = new PrintStream(new FileOutputStream(filename));
					}
					catch (IOException ex)
					{
						Utilities.fatalError("Cannot open output file "
								+ filename);
					}

				((Program) result).cgen(output);
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
