package cool.semant;

import cool.common.AbstractSymbol;
import cool.common.TreeConstants;

import cool.parser.Classes;
import cool.parser.TreeNode;
import cool.parser.class_;

import java.util.HashMap;

/**
 * Before a type checking and semantic checking is done on the AST, certain
 * basic semantic checks have to be done to verify the sanity of the program.
 * An instance of this class does the same.
 * 
 * @author kempa
 * 
 */
public class SemantChecker
{
	/**
	 * Constructor
	 * 
	 * @param cls List of cool classes in the program
	 * @param err Used to format, keep track and spew out semantic errors
	 */
	public SemantChecker(Classes cls, SemantError err)
	{
		// Policy on error: Return immediately

		// Check for duplicate definitions of classes
		HashMap<AbstractSymbol, class_> tbl = new HashMap<AbstractSymbol, class_>();
		for (TreeNode t : cls.getElements())
		{
			class_ c = (class_) t;
			if (c.getName().equals(TreeConstants.SELF_TYPE))
			{
				err.semantError(c).println(
						"Redefinition of basic class " + c.getName() + ".");
				return;
			}
			else if (c.getName().equals(TreeConstants.Object_)
					|| c.getName().equals(TreeConstants.Bool)
					|| c.getName().equals(TreeConstants.Int)
					|| c.getName().equals(TreeConstants.Str)
					|| c.getName().equals(TreeConstants.IO))
			{
				err.semantError(c).println(
						"Redefinition of basic class " + c.getName() + ".");
				return;
			}
			else if (tbl.containsKey(c.getName()))
			{
				err.semantError(c).println(
						"Class " + c.getName() + " was previously defined.");
				return;
			}
			else
				tbl.put(c.getName(), c);
		}

		// The below can be done only after it is ensured that there is no
		// redefinition of user defined classes

		// is class Main defined?
		if (!tbl.containsKey(TreeConstants.Main))
		{
			err.semantError().println("Class Main is not defined.");
			return;
		}

		// inheriting from a valid type.
		for (TreeNode t : cls.getElements())
		{
			class_ c = (class_) t;
			if (c.getParent().equals(TreeConstants.SELF_TYPE)
					|| c.getParent().equals(TreeConstants.Bool)
					|| c.getParent().equals(TreeConstants.Str)
					|| c.getParent().equals(TreeConstants.Int))
			{
				err.semantError(c).println(
						"Class " + c.getName() + " cannot inherit class "
								+ c.getParent() + ".");
				return;
			}
			if (!tbl.containsKey(c.getParent())
					&& c.getParent() != TreeConstants.Object_
					&& c.getParent() != TreeConstants.IO)
			{
				err.semantError(c).println(
						"Class " + c.getName()
								+ " inherits from an undefined class "
								+ c.getParent() + ".");
				return;
			}
		}
	}
}// class ClassTable