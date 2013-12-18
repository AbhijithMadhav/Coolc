
LIBDIR= ../lib
BINDIR= ../bin

LEXSPEC= cool.lex
LEXGEN= ../src/cool/lexer/CoolTokenLexer.java

CUPSPEC= cool.cup
CUPGEN= ../src/cool/parser/TokenConstants.java ../src/cool/parser/CoolParser.java

BUILDCLASSPATH := ${LIBDIR}/java-cup-11a.jar:${LIBDIR}/jlex.jar:${LIBDIR}/stdlib.jar:../output
TESTCLASSPATH := ../${LIBDIR}/java-cup-11a.jar:../${LIBDIR}/jlex.jar:../${LIBDIR}/stdlib.jar:../../output

JAVAC := javac
JLEX := java  -classpath ${LIBDIR}/jlex.jar JLex.Main
CUP := ${BINDIR}/java_cup 

coolc: Makefile ${LEXGEN} ${CUPGEN}  mylexer myparser mysemant mycoolc

${LEXGEN}: ${LEXSPEC}
	${JLEX} $<
	mv cool.lex.java ${LEXGEN}

${CUPGEN}: ${CUPSPEC}
	${CUP} -parser CoolParser -symbols TokenConstants -expect 10000 -dump -nopositions < $< >cool.output 2>&1 || { egrep '^Error at ' cool.output; false; }
	mv CoolParser.java TokenConstants.java ../src/cool/parser

mylexer: Makefile
	@rm -f $@
	echo '#! /bin/bash' >> $@
	echo 'java -classpath ${BUILDCLASSPATH} cool/coolc/LexerDriver $$*' >> $@
	chmod 755 $@

myparser: Makefile mylexer
	@rm -f $@
	echo '#! /bin/bash' >> $@
	echo 'java -classpath ${BUILDCLASSPATH} cool/coolc/ParserDriver $$*' >> $@
	chmod 755 $@

mysemant: Makefile mylexer myparser
	@rm -f $@
	echo '#! /bin/bash' >> $@
	echo 'java -classpath ${BUILDCLASSPATH} cool/coolc/SemantDriver $$*' >> $@
	chmod 755 $@

mycoolc: Makefile
	@rm -f $@
	echo '#! /bin/bash' >> $@
	echo 'java -classpath ${BUILDCLASSPATH} cool/coolc/Coolc $$*' >> $@
	chmod 755 $@

testlexer:
	@rm -f lexer
	echo '#! /bin/bash' >> lexer
	echo 'java -classpath ${TESTCLASSPATH} cool/coolc/LexerDriver $$*' >> lexer
	chmod 755 lexer
	perl ./lexer-grading.pl -r
	@rm -f lexer

testparser:
	@rm -f parser
	echo '#! /bin/bash' >> parser
	echo 'java -classpath ${TESTCLASSPATH} cool/coolc/ParserDriver $$*' >> parser
	chmod 755 parser
	ln -s -f ../parser parser_grading/myparser
	perl ./parser-grading.pl -r
	@rm -f parser parser_grading/myparser

testsemant:
	@rm -f semant
	echo '#! /bin/bash' >> semant
	echo 'java -classpath ${TESTCLASSPATH} cool/coolc/SemantDriver $$*' >> semant
	chmod 755 semant
	ln -s -f ../semant semant_grading/mysemant
	perl ./semant-grading.pl -r -skip
	@rm -f semant semant_grading/mysemant

testcoolc:
	@rm -f cgen
	echo '#! /bin/bash' >> cgen
	echo 'java -classpath ${TESTCLASSPATH} cool/coolc/Coolc $$*' >> cgen
	chmod 755 cgen
	ln -s -f ../cgen coolc_grading/mycoolc
	perl ./coolc-grading.pl -r -skip
	@rm -f cgen coolc_grading/mycoolc

dotest: testlexer testparser testsemant testcoolc

clean :
	-rm -r -f *.s *~ ${LEXGEN} ${CUPGEN} core mycoolc cool.output mylexer myparser mysemant lexer parser semant coolc coolc_grading/test-output coolc_grading/*.out coolc_grading/*.s coolc/SCORE coolc/RESULTS semant_grading/test-output/ semant_grading/*.out semant_grading/*.s semant_grading/SCORE semant_grading/RESULTS parser_grading/test-output/ parser_grading/*.out parser_grading/*.s parser_grading/SCORE parser_grading/RESULTS lexer_grading/test-output/ lexer_grading/*.out lexer_grading/*.s lexer_grading/SCORE lexer_grading/RESULTS
