package cool.lexer;

import cool.common.EscapedNullInCoolStringException;
import cool.common.NullInCoolStringException;

/**
 * The regular pattern matching in the JLex configuration has been designed to
 * recognise multi-line string constants as separate strings(Enhancement can
 * be done here). CoolString builder helps assemble these multi-line cool
 * string chunks into one cool string(append()).
 * 
 * During the process of appending chunks the CoolStringBuilder object
 * internally formats the representative escape sequences(comprising of two
 * ASCII characters) in the input string chunks to the actual intended
 * values(single ASCII character)
 */
final class CoolStringBuilder
{
	/*
	 * Can't use String here as it is an immutable object
	 * 
	 * Also a StringBuilder is used instead of a StringBuffer as this(scanning)
	 * is a single threaded execution
	 */
	private StringBuilder buf;
	private boolean err;

	CoolStringBuilder()
	{
		buf = new StringBuilder();
		err = false;
	}

	boolean inError()
	{
		return err;
	}

	void setError()
	{
		err = true;
	}

	int length()
	{
		return buf.length();
	}

	CoolStringBuilder append(String str) throws NullInCoolStringException,
			EscapedNullInCoolStringException
	{
		buf.append(toCoolString(str));

		return this;
	}

	public String toString()
	{
		return buf.toString();
	}

	/**
	 * Converts input string chunk to a cool string by converting escape
	 * sequences to the actual character representations
	 * 
	 * @param str The input string chunk
	 * @return The formatted cool string chunk
	 * 
	 */
	private StringBuilder toCoolString(String str)
			throws NullInCoolStringException, EscapedNullInCoolStringException
	{
		StringBuilder sb = new StringBuilder(str);
		for (int i = 0; i < sb.length(); i++)
		{
			// null character
			if (sb.charAt(i) == 0)
			{
				setError();
				throw new NullInCoolStringException();
			}

			// escape character
			if (sb.charAt(i) == '\\')
				if (i == sb.length() - 1) // if '\' is the last character it
											// isn't specifying an escape
											// sequence
					sb.deleteCharAt(i);
				else
				{
					sb.deleteCharAt(i); // remove the '/'
					switch (sb.charAt(i))
					{
					case 'b':
						sb.deleteCharAt(i);
						sb.insert(i, '\b');
						break;
					case 't':
						sb.deleteCharAt(i);
						sb.insert(i, '\t');
						break;
					case 'n':
						sb.deleteCharAt(i);
						sb.insert(i, '\n');
						break;
					case 'f':
						sb.deleteCharAt(i);
						sb.insert(i, '\f');
						break;
					case 0:
						setError();
						throw new EscapedNullInCoolStringException();
					}
				}
		} // end of for
		return sb;
	}
}