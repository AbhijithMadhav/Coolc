/**
 * 
 */
package cool.parser;

import cool.common.AbstractSymbol;
import cool.common.PossibleNullDereferenceException;
import cool.common.TreeConstants;
import cool.common.UnresolvableSelfTypeException;
import cool.common.Utilities;

import cool.semant.ObjectTypeEnvironment;
import cool.semant.SemantError;
import cool.semant.SemantUtil;

import cool.cgen.AttributeRecord;
import cool.cgen.CgenLookupTable;
import cool.cgen.CgenSupport;

import java.io.PrintStream;

/**
 * Defines AST constructor 'object'.
 * <p>
 * See <a href="TreeNode.html">TreeNode</a> for full documentation.
 */
public class object extends Expression
{
	private AbstractSymbol name;

	/**
	 * Creates "object" AST node.
	 * 
	 * @param lineNumber the line in the source file from which this node came.
	 * @param a0 initial value for name
	 */
	public object(int lineNumber, AbstractSymbol a1)
	{
		super(lineNumber);
		name = a1;
	}

	public TreeNode copy()
	{
		return new object(lineNumber, copy_AbstractSymbol(name));
	}

	public void dump(PrintStream out, int n)
	{
		out.print(Utilities.pad(n) + "object\n");
		dump_AbstractSymbol(out, n + 2, name);
	}

	public void dump_with_types(PrintStream out, int n)
	{
		dump_line(out, n);
		out.println(Utilities.pad(n) + "_object");
		dump_AbstractSymbol(out, n + 2, name);
		dump_type(out, n);
	}

	public void validateAndSetType(ObjectTypeEnvironment o, method m, class_ c,
			SemantError err) throws PossibleNullDereferenceException,
			UnresolvableSelfTypeException
	{
		set_type(SemantUtil.typeLookup(name, o, m, c, err));
	}

	/**
	 * Generates code for the 'object' expression.
	 * 
	 * @param s the output stream
	 * */
	public int code(class_ c, int nAR, CgenLookupTable tbl, PrintStream s)

	{
		s.println("# Expression - Object");
		s.println();
		if (name.toString().equals(TreeConstants.self.toString()))
		{
			s.println("# Invoking object is self");
		}
		else
		{
			if (tbl.lookup(name) == null)
			{
				// attribute
				s.println("# Identifier is an attribute");

				s.println("# Class tag of invoking object");
				nAR = CgenSupport.emitPush(CgenSupport.ACC, nAR, s);
				CgenSupport.emitFetchClassTag(CgenSupport.ACC, CgenSupport.ACC,
						s);

				int aLabel[] = new int[tbl.getCandidateAttributes(name).size()];
				int endLabel = CgenSupport.getLabel();
				int nLabel = 0;
				for (AttributeRecord a : tbl.getCandidateAttributes(name))
				{
					aLabel[nLabel] = CgenSupport.getLabel();
					CgenSupport.emitLoadImm(CgenSupport.T1, a.getClass_()
							.getClassTag(), s);
					CgenSupport.emitBeq(CgenSupport.ACC, CgenSupport.T1,
							aLabel[nLabel], s);
					nLabel++;
				}
				int nItems = nAR;
				nLabel = 0;
				for (AttributeRecord a : tbl.getCandidateAttributes(name))
				{
					nAR = nItems;
					CgenSupport.emitLabelDef(aLabel[nLabel], s);
					nAR = CgenSupport.emitPop(CgenSupport.ACC, nAR, s);
					CgenSupport.emitLoad(CgenSupport.ACC, a.getOffset(),
							CgenSupport.ACC, s);
					CgenSupport.emitBranch(endLabel, s);
					nLabel++;
				}
				CgenSupport.emitLabelDef(endLabel, s);
			}
			else
			{
				s.println("# Identifier is local");
				CgenSupport.emitLoad(CgenSupport.ACC,
						(Integer) tbl.lookup(name), CgenSupport.FP, s);
			}
		}
		s.println("# End - Expression - Object");
		s.println();
		return nAR;
	}
}