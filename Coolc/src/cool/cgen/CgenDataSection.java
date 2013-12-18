package cool.cgen;

import cool.common.AbstractSymbol;
import cool.common.AbstractTable;
import cool.common.Flags;
import cool.common.TreeConstants;
import cool.common.Utilities;

import cool.parser.TreeNode;
import cool.parser.attr;
import cool.parser.method;
import cool.parser.no_expr;

import cool.semant.CNode;
import cool.semant.ITree;

import java.io.PrintStream;
import java.util.PriorityQueue;

public class CgenDataSection
{

	private CgenDataSection()
	{
	}

	public static void code(ITree iTree, CgenLookupTable tbl, PrintStream str)
	{
		if (Flags.cgen_debug)
			System.out.println("coding global data");
		codeGlobalData(iTree, str);

		if (Flags.cgen_debug)
			System.out.println("choosing gc");
		codeSelectGc(str);

		if (Flags.cgen_debug)
			System.out.println("coding constants");
		codeConstants(iTree, str);

		str.println("# Begin - Class name table");
		str.println("# Class name table contains addresses of String objects"
				+ " representing all the classes in the program. These can be"
				+ " indexed using the classtag of objects");
		str.println("# Used by Object.type_name() to return name of the "
				+ "invoking object");
		codeClassNameTab(iTree, str);
		str.println("# End - Class name table\n");

		str.println("# Begin - Class Object table");
		str.println("# Contains pointers to prototype objects of all classes "
				+ "and to their initializers. These can be indexed using the "
				+ "classtag of objexts");
		str.println("# Used to create objects for the expression, "
				+ "new SELF_TYPE");
		codeObjTab(iTree, str);
		str.println("# End - Class Object table\n");

		str.println("# Begin - Dispatch tables");
		str.println("# Dispatch table of all classes");
		codeDispTab(iTree, str);
		str.println("# End - Dispatch tables\n");

		str.println("# Begin - Prototype objects");
		str.println("# Prototype objects for all classes. Used to create new "
				+ "objects for the respective classes");
		codePrototypeObjects(iTree, str);
		str.println("# End - Prototype objects");

		if (Flags.cgen_debug)
			System.out.println("coding global text");
		codeGlobalText(str);

		str.println("# Begin - Object Initializers");
		codeObjInit(iTree, tbl, str);
		str.println("# End - Object Initializers");
	}

	/**
	 * Emits code to start the .data segment and to
	 * declare the global names.
	 * */
	private static void codeGlobalData(ITree iTree, PrintStream str)
	{
		// The following global names must be defined first.

		str.print("\t.data\n" + CgenSupport.ALIGN);
		str.println(CgenSupport.GLOBAL + CgenSupport.CLASSNAMETAB);
		str.print(CgenSupport.GLOBAL);
		CgenSupport.emitProtObjRef(TreeConstants.Main, str);
		str.println("");
		str.print(CgenSupport.GLOBAL);
		CgenSupport.emitProtObjRef(TreeConstants.Int, str);
		str.println("");
		str.print(CgenSupport.GLOBAL);
		CgenSupport.emitProtObjRef(TreeConstants.Str, str);
		str.println("");
		str.print(CgenSupport.GLOBAL);
		BoolConst.falsebool.codeRef(str);
		str.println("");
		str.print(CgenSupport.GLOBAL);
		BoolConst.truebool.codeRef(str);
		str.println("");
		str.println(CgenSupport.GLOBAL + CgenSupport.INTTAG);
		str.println(CgenSupport.GLOBAL + CgenSupport.BOOLTAG);
		str.println(CgenSupport.GLOBAL + CgenSupport.STRINGTAG);

		// We also need to know the tag of the Int, String, and Bool classes
		// during code generation.
		// why??? Incomplete

		str.println(CgenSupport.INTTAG + CgenSupport.LABEL + CgenSupport.WORD
				+ (iTree.toINode(TreeConstants.Int)).getClassTag());
		str.println(CgenSupport.BOOLTAG + CgenSupport.LABEL + CgenSupport.WORD
				+ (iTree.toINode(TreeConstants.Bool)).getClassTag());
		str.println(CgenSupport.STRINGTAG + CgenSupport.LABEL
				+ CgenSupport.WORD
				+ (iTree.toINode(TreeConstants.Str)).getClassTag());
	}

	/** Generates GC choice constants (pointers to GC functions) */
	private static void codeSelectGc(PrintStream str)
	{
		str.println(CgenSupport.GLOBAL + "_MemMgr_INITIALIZER");
		str.println("_MemMgr_INITIALIZER:");
		str.println(CgenSupport.WORD
				+ CgenSupport.gcInitNames[Flags.cgen_Memmgr]);

		str.println(CgenSupport.GLOBAL + "_MemMgr_COLLECTOR");
		str.println("_MemMgr_COLLECTOR:");
		str.println(CgenSupport.WORD
				+ CgenSupport.gcCollectNames[Flags.cgen_Memmgr]);

		str.println(CgenSupport.GLOBAL + "_MemMgr_TEST");
		str.println("_MemMgr_TEST:");
		str.println(CgenSupport.WORD
				+ ((Flags.cgen_Memmgr_Test == Flags.GC_TEST) ? "1" : "0"));
	}

	/**
	 * Emits code to reserve space for and initialise all of the
	 * constants. Class names should have been added to the string
	 * table (in the supplied code, is is done during the construction
	 * of the inheritance graph), and code for emitting string constants
	 * as a side effect adds the string's length to the integer table.
	 * The constants are emmitted by running through the stringtable and
	 * inttable and producing code for each entry.
	 */
	private static void codeConstants(ITree iTree, PrintStream str)
	{
		// Add constants that are required by the code generator.
		AbstractTable.stringtable.addString("");
		AbstractTable.inttable.addString("0");

		// The string table must first be codified as the sizes of the string
		// are then added to the int table which is then codified
		AbstractTable.stringtable.codeStringTable(
				(iTree.toINode(TreeConstants.Str)).getClassTag(), str);
		AbstractTable.inttable.codeStringTable(
				(iTree.toINode(TreeConstants.Int)).getClassTag(), str);

		codeBools((iTree.toINode(TreeConstants.Bool)).getClassTag(), str);
		codeVoid(str);
	}

	/** Emits code definitions for boolean constants. */
	private static void codeBools(int classtag, PrintStream str)
	{
		BoolConst.falsebool.codeDef(classtag, str);
		BoolConst.truebool.codeDef(classtag, str);
	}

	/** Emits code definition for void */
	private static void codeVoid(PrintStream str)
	{
		// Add -1 eye catcher
		str.println(CgenSupport.WORD + "-1");
		str.println(CgenSupport.VOIDCONST_PREFIX + CgenSupport.LABEL
				+ CgenSupport.WORD + CgenSupport.VOID_CLASSTAG); // tag
		str.println(CgenSupport.WORD + (CgenSupport.DEFAULT_OBJFIELDS - 1)); // size
	}

	/**
	 * Class name table
	 * 
	 * A table, which at index, (class tag) ∗ 4, contains a pointer
	 * to a 'String' object containing the name of the class associated
	 */
	private static void codeClassNameTab(ITree iTree, PrintStream str)
	{
		// The PQ sorts the nodes w.r.t the class tags.
		PriorityQueue<CNode> pq = new PriorityQueue<CNode>(iTree.getClasses());

		// Generate the class-name table
		str.print(CgenSupport.CLASSNAMETAB + CgenSupport.LABEL);
		while (!pq.isEmpty())
		{
			AbstractSymbol cName = pq.remove().getName();
			str.println(CgenSupport.WORD + CgenSupport.STRCONST_PREFIX
					+ AbstractTable.stringtable.lookup(cName.toString()).index);
		}

	}

	/**
	 * Class Object table
	 * 
	 * A table, which at index (class tag) ∗ 8 contains a pointer to the
	 * prototype object and at index (class tag) ∗ 8 + 4 contains a pointer to
	 * the initialization method for that class.
	 * 
	 * This is required so that objects can be created for the expression
	 * 'new SELF_TYPE'
	 */
	private static void codeObjTab(ITree iTree, PrintStream str)
	{
		// The PQ sorts the nodes w.r.t the class tags.
		PriorityQueue<CNode> pq = new PriorityQueue<CNode>(iTree.getClasses());

		// Generate the object table
		str.print(CgenSupport.CLASSOBJTAB + CgenSupport.LABEL);
		while (!pq.isEmpty())
		{
			CNode nd = pq.remove();
			str.println(CgenSupport.WORD + nd.getName().toString()
					+ CgenSupport.PROTOBJ_SUFFIX);
			str.println(CgenSupport.WORD + nd.getName().toString()
					+ CgenSupport.CLASSINIT_SUFFIX);
		}
	}

	/**
	 * Index for dispatch tables of all classes
	 * Also construb the method table used to generate code for dynamic dispatch
	 */
	private static void codeDispTab(ITree iTree, PrintStream str)
	{
		for (CNode nd : iTree.getClasses())
		{// For each class

			str.print(nd.getName().toString() + CgenSupport.DISPTAB_SUFFIX
					+ CgenSupport.LABEL);

			// For each ancestor of this class
			for (CNode p : nd.getAncestors(Utilities.oldestFirst))
			{
				for (TreeNode t1 : p.getFeatures().getElements())
				{
					if (t1 instanceof attr)
						continue;
					// An entry of each method
					str.println(CgenSupport.WORD + p.getName().toString() + "."
							+ ((method) t1).getName());
				}
			}
		}
	}

	/**
	 * Prototype Objects - Prototype objects of all classes of the
	 * program. Used by Object.copy() to create new objects
	 * 
	 */
	private static void codePrototypeObjects(ITree iTree, PrintStream str)
	{
		for (CNode c : iTree.getClasses())
		{

			str.println(CgenSupport.WORD + "-1"); // Add -1 eye catcher
			str.print(c.getName() + CgenSupport.PROTOBJ_SUFFIX
					+ CgenSupport.LABEL); // label
			str.println(CgenSupport.WORD
					+ (iTree.toINode(c.getName())).getClassTag()); // tag

			if (c.getName().equals(TreeConstants.Object_))
			{
				// Object size
				str.println(CgenSupport.WORD + (CgenSupport.DEFAULT_OBJFIELDS));
				str.println(CgenSupport.WORD + c.getName().toString()
						+ CgenSupport.DISPTAB_SUFFIX); // dispatch table
			}
			else if (c.getName().equals(TreeConstants.Str))
			{
				// object size
				str.println(CgenSupport.WORD
						+ (CgenSupport.DEFAULT_OBJFIELDS
								+ CgenSupport.STRING_SLOTS + (CgenSupport.STR_DEFAULT
								.length() + 4) / 4));
				str.println(CgenSupport.WORD + c.getName().toString()
						+ CgenSupport.DISPTAB_SUFFIX); // dispatch table
				// length
				str.print(CgenSupport.WORD);
				((IntSymbol) (AbstractTable.inttable
						.lookup(((Integer) CgenSupport.STR_DEFAULT.length())
								.toString()))).codeRef(str);
				str.println();

				// default string
				CgenSupport.emitStringConstant(CgenSupport.STR_DEFAULT, str);
			}
			else if (c.getName().equals(TreeConstants.Int))
			{
				str.println(CgenSupport.WORD
						+ (CgenSupport.DEFAULT_OBJFIELDS + CgenSupport.INT_SLOTS));// object
																					// size
				str.println(CgenSupport.WORD + c.getName().toString()
						+ CgenSupport.DISPTAB_SUFFIX); // dispatch table
				str.println(CgenSupport.WORD + CgenSupport.INT_DEFAULT);
			}
			else if (c.getName().equals(TreeConstants.Bool))
			{
				str.println(CgenSupport.WORD
						+ (CgenSupport.DEFAULT_OBJFIELDS + CgenSupport.BOOL_SLOTS));// object
																					// size
				str.println(CgenSupport.WORD + c.getName().toString()
						+ CgenSupport.DISPTAB_SUFFIX); // dispatch table
				str.println(CgenSupport.WORD + CgenSupport.BOOL_DEFAULT);
			}
			else if (c.getName().equals(TreeConstants.IO))
			{
				str.println(CgenSupport.WORD + CgenSupport.DEFAULT_OBJFIELDS);// object
																				// size
				str.println(CgenSupport.WORD + c.getName().toString()
						+ CgenSupport.DISPTAB_SUFFIX); // dispatch table
			}
			else
			// user class prototype object
			{
				str.println(CgenSupport.WORD
						+ (CgenSupport.DEFAULT_OBJFIELDS + c.getAllAttributes()
								.size())); // size
				str.println(CgenSupport.WORD + c.getName().toString()
						+ CgenSupport.DISPTAB_SUFFIX); // dispatch table

				// Initialize all attributes from the
				// inheritance hierarchy in the parent first order.
				for (attr a : c.getAllAttributes())
					str.println(CgenSupport.WORD
							+ CgenUtilities.getDefaultObjectAddress(a
									.getTypeDecl()));
			}
		}
	}

	/**
	 * Emits code to start the .text segment and to
	 * declare the global names.
	 * */
	private static void codeGlobalText(PrintStream str)
	{
		str.println(CgenSupport.GLOBAL + CgenSupport.HEAP_START);
		str.print(CgenSupport.HEAP_START + CgenSupport.LABEL);
		str.println(CgenSupport.WORD + 0);
		str.println("\t.text");
		str.print(CgenSupport.GLOBAL);
		CgenSupport.emitInitRef(TreeConstants.Main, str);
		str.println("");
		str.print(CgenSupport.GLOBAL);
		CgenSupport.emitInitRef(TreeConstants.Int, str);
		str.println("");
		str.print(CgenSupport.GLOBAL);
		CgenSupport.emitInitRef(TreeConstants.Str, str);
		str.println("");
		str.print(CgenSupport.GLOBAL);
		CgenSupport.emitInitRef(TreeConstants.Bool, str);
		str.println("");
		str.print(CgenSupport.GLOBAL);
		CgenSupport.emitMethodRef(TreeConstants.Main, TreeConstants.main_meth,
				str);
		str.println("");
	}

	/**
	 * Initializers for objects of all classes
	 */
	static void codeObjInit(ITree iTree, CgenLookupTable tbl, PrintStream str)
	{
		for (CNode c : iTree.getClasses())
		{
			int nAR = 0;
			str.print(c.getName().toString() + CgenSupport.CLASSINIT_SUFFIX
					+ CgenSupport.LABEL);

			// Callee responsibility
			str.println("#Set up the FP");
			CgenSupport.emitMove(CgenSupport.FP, CgenSupport.SP, str);
			str.println();
			str.println("#Save return address");
			nAR = CgenSupport.emitPush(CgenSupport.RA, nAR, str);
			str.println();

			// Need to call the init of the parent of the class of this object

			// Caller responsibility
			// 1. Save my FP
			nAR = CgenSupport.emitPush(CgenSupport.FP, nAR, str);

			// Save self Object
			nAR = CgenSupport.emitPush(CgenSupport.ACC, nAR, str);
			// Call the initializer of the parent
			if (!c.getName().equals(TreeConstants.Object_))
				CgenSupport.emitJal(c.getParentNd().getName().toString()
						+ CgenSupport.CLASSINIT_SUFFIX, str);
			// Restore the self Object
			nAR = CgenSupport.emitPop(CgenSupport.ACC, nAR, str);

			// Caller responsibility
			// 1. Restore my FP
			nAR = CgenSupport.emitPop(CgenSupport.FP, nAR, str);

			// Basic classes whose attributes cannot be initialised explicitly
			if (c.getName().equals(TreeConstants.Int)
					|| c.getName().equals(TreeConstants.Str)
					|| c.getName().equals(TreeConstants.Bool))
			{
				nAR = CgenSupport.emitPop(CgenSupport.RA, nAR, str);
				CgenSupport.emitReturn(str); // return
				continue;
			}

			// Initialize attributes of this object. The inherited attributes
			// are initialised by the parent initializer
			int offset = c.getOffsetOfFirstNonInheritedAttribute();
			for (TreeNode t : c.getFeatures().getElements())
			{
				if (t instanceof method)
					continue;
				attr a = (attr) t;

				// Save self Object
				nAR = CgenSupport.emitPush(CgenSupport.ACC, nAR, str);

				if (a.getInit() instanceof no_expr)// Incomplete hack
				{
					// Restore the self Object
					nAR = CgenSupport.emitPop(CgenSupport.ACC, nAR, str);

					CgenSupport.emitLoadAddress(CgenSupport.T1, CgenUtilities
							.getDefaultObjectAddress(a.getTypeDecl()), str);
					CgenSupport.emitStore(CgenSupport.T1, offset,
							CgenSupport.ACC, str);
				}
				else
				{
					tbl.enterScope();
					a.getInit().code(c, nAR, tbl, str);
					tbl.exitScope();
					CgenSupport.emitMove(CgenSupport.T1, CgenSupport.ACC, str);

					// Restore the self Object
					nAR = CgenSupport.emitPop(CgenSupport.ACC, nAR, str);

					// Set initializer
					CgenSupport.emitStore(CgenSupport.T1, offset,
							CgenSupport.ACC, str);
				}
				offset++;
			}
			nAR = CgenSupport.emitPop(CgenSupport.RA, nAR, str);
			CgenSupport.emitReturn(str); // return
		}
	}

}
