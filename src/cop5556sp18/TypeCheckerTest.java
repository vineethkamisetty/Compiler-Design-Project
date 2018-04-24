package cop5556sp18;

import cop5556sp18.AST.ASTVisitor;
import cop5556sp18.AST.Program;
import cop5556sp18.TypeChecker.SemanticException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TypeCheckerTest {

    /**
     * Prints objects in a way that is easy to turn on and off
     */
    static final boolean doPrint = true;
    /*
     * set Junit to be able to catch exceptions
     */
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private void show(Object input) {
        if (doPrint) {
            System.out.println(input.toString());
        }
    }

    /**
     * Scans, parses, and type checks the input string
     *
     * @param input
     * @throws Exception
     */
    void typeCheck(String input) throws Exception {
        show(input);
        // instantiate a Scanner and scan input
        Scanner scanner = new Scanner(input).scan();
        show(scanner);
        // instantiate a Parser and parse input to obtain and AST
        Program ast = new Parser(scanner).parse();
        show(ast);
        // instantiate a TypeChecker and visit the ast to perform type checking and
        // decorate the AST.
        ASTVisitor v = new TypeChecker();
        ast.visit(v, null);
    }


    /**
     * Simple test case with an almost empty program.
     *
     * @throws Exception
     */
    @Test
    public void emptyProg() throws Exception {
        String input = "emptyProg{}";
        typeCheck(input);
    }

    @Test
    public void expression1() throws Exception {
        String input = "prog {show 3+4;}";
        typeCheck(input);
    }

    @Test
    public void expression2_fail() throws Exception {
        String input = "prog { show true+4; }"; //error, incompatible types in binary expression
        thrown.expect(SemanticException.class);
        try {
            typeCheck(input);
        } catch (SemanticException e) {
            show(e);
            throw e;
        }
    }

    @Test
    public void declaration0() throws Exception {
        String input = "prog {image a[12, 12]; image a;}";
        thrown.expect(SemanticException.class);
        try {
            typeCheck(input);
        } catch (SemanticException e) {
            show(e);
            throw e;
        }
    }

    @Test
    public void declaration1() throws Exception {
        String input = "prog {image a; show a;}";
        typeCheck(input);
    }

    @Test
    public void declarationNested() throws Exception {
        String input = "prog {int a; while(true) { int a; }; }";
        typeCheck(input);
    }

    @Test
    public void statementShow0() throws Exception {
        String input = "prog {show a;}"; //no declaration before use
        thrown.expect(SemanticException.class);
        try {
            typeCheck(input);
        } catch (SemanticException e) {
            show(e);
            throw e;
        }
    }

    @Test
    public void statementShow1() throws Exception {
        String input = "prog {image a[3,4]; show a;}";
        typeCheck(input);
    }

    @Test
    public void statementWrite0() throws Exception {
        String input = "prog {image a[3,4]; int b; write a to b;}";
        thrown.expect(SemanticException.class);
        try {
            typeCheck(input);
        } catch (SemanticException e) {
            show(e);
            throw e;
        }
    }

    @Test
    public void statementWrite1() throws Exception {
        String input = "prog {int a; int b; write a to b;}";
        thrown.expect(SemanticException.class);
        try {
            typeCheck(input);
        } catch (SemanticException e) {
            show(e);
            throw e;
        }
    }

    @Test
    public void statementWrite2() throws Exception {
        String input = "prog {image a[3,4]; filename b; write a to b;}";
        typeCheck(input);
    }

    @Test
    public void statementInput0() throws Exception {
        String input = "prog {int a; int b; input a from @ b;}";
        typeCheck(input);
    }

    @Test
    public void statementInput1() throws Exception {
        String input = "prog {int a; float b; input a from @ b;}";
        thrown.expect(SemanticException.class);
        try {
            typeCheck(input);
        } catch (SemanticException e) {
            show(e);
            throw e;
        }
    }

    @Test
    public void statementInput2() throws Exception {
        String input = "prog {int a; int b; input a from @ 1+2;}";
        typeCheck(input);
    }

    @Test
    public void statementInput3() throws Exception {
        String input = "prog {int a; int b; input a from @ 1*2+3;}";
        typeCheck(input);
    }

    @Test
    public void statementAssignment0() throws Exception {
        String input = "prog {int b; b := 3;}";
        typeCheck(input);
    }

    @Test
    public void statementAssignment1() throws Exception {
        String input = "prog {float b; b := 3;}";
        thrown.expect(SemanticException.class);
        try {
            typeCheck(input);
        } catch (SemanticException e) {
            show(e);
            throw e;
        }
    }

    @Test
    public void statementAssignmentNested() throws Exception {
        String input = "prog {float a; while(true) { a := 3; }; }";
        thrown.expect(SemanticException.class);
        try {
            typeCheck(input);
        } catch (SemanticException e) {
            show(e);
            throw e;
        }
    }


    @Test
    public void statementWhile0() throws Exception {
        String input = "prog {while(true) {int b;}; }";
        typeCheck(input);
    }

    @Test
    public void statementWhile1() throws Exception {
        String input = "prog {while(1) {int b;}; }";
        thrown.expect(SemanticException.class);
        try {
            typeCheck(input);
        } catch (SemanticException e) {
            show(e);
            throw e;
        }
    }

    @Test
    public void test1() throws Exception {
        String input = "prog{int var1[500, 1];}";
        thrown.expect(Parser.SyntaxException.class);
        try {
            typeCheck(input);
        } catch (Parser.SyntaxException e) {
            show(e);
            throw e;
        }
    }

    @Test
    public void test2() throws Exception {
        String input = "prog {int var1; float var2; image var3;var1 := width(var3); var1 := height(var3); var2 := float(1); var1 := int(1.0);}";
        typeCheck(input);
    }

    @Test
    public void test3() throws Exception {
        String input = "prog{image image1; write image1 to image1;}";
        thrown.expect(SemanticException.class);
        try {
            typeCheck(input);
        } catch (SemanticException e) {
            show(e);
            throw e;
        }
    }

    @Test
    public void simpleImage() throws Exception {
        String input = "X{ image im[1,2]; }";
        typeCheck(input);
    }

    @Test
    public void simpleImageFail() throws Exception {
        String input = "X{ image im[1.0, 2]; }";
        thrown.expect(SemanticException.class);
        try {
            typeCheck(input);
        } catch (SemanticException e) {
            show(e);
            throw e;
        }
    }

    @Test
    public void nestedDec1() throws Exception {
        String input = "X{ int x; int y; while (x == y) {int x;}; }";
        typeCheck(input);
    }

    @Test
    public void nestedDec2() throws Exception {
        String input = "X{ int x; int z; while (x == y) {int x;}; }";
        thrown.expect(SemanticException.class);
        try {
            typeCheck(input);
        } catch (SemanticException e) {
            show(e);
            throw e;
        }
    }

    @Test
    public void nestedDec3() throws Exception {
        String input = "X{ int x; int y; while (x == y) { show x;}; }";
        typeCheck(input);
    }

    @Test
    public void nestedDec4() throws Exception {
        String input = "X{ int x; int y; while (x == y) { int z;}; show z;}";
        thrown.expect(SemanticException.class);
        try {
            typeCheck(input);
        } catch (SemanticException e) {
            show(e);
            throw e;
        }
    }

    @Test
    public void test5() throws Exception {
        String input = "prog {int var1; var1 := abs(1); var1 := red(1); var1 := green(1); var1 := blue(1); var1 := int(1); var1 := alpha(1);}";
        typeCheck(input);
    }

    @Test
    public void test6() throws Exception {
        String input = "prog {float var2;var2 := abs(1.0); var2 := sin(1.0); var2 := cos(1.0); var2 := atan(1.0); var2 := float(1.0); var2 := log(1.0);}";
        typeCheck(input);
    }

    @Test
    public void test7() throws Exception {
        String input = "prog {int var1; float var2; image var3;var1 := width(var3); var1 := height(var3); var2 := float(1); var1 := int(1.0);}";
        typeCheck(input);
    }

    @Test
    public void test8() throws Exception {
        String input = "prog{int var1; float var2; boolean var3; image var4; filename var5; image var6[500,500];if(true){int var1; float var2; boolean var3; image var4; filename var5; image var6[500,500];};float var1;}";
        thrown.expect(SemanticException.class);
        try {
            typeCheck(input);
        } catch (SemanticException e) {
            show(e);
            throw e;
        }
    }

    @Test
    public void test9() throws Exception {
        String input = "prog{image var1[1.0, 500];}";
        thrown.expect(SemanticException.class);
        try {
            typeCheck(input);
        } catch (SemanticException e) {
            show(e);
            throw e;
        }
    }

    @Test
    public void test10() throws Exception {
        String input = "prog{image var1[500, 1.0];}";
        thrown.expect(SemanticException.class);
        try {
            typeCheck(input);
        } catch (SemanticException e) {
            show(e);
            throw e;
        }
    }

    @Test
    public void test11() throws Exception {
        String input = "prog{ int var1; boolean var2;var1 := 1 + 2; var1 := 1 - 2; var1 := 1 * 2; var1 := 1 / 2; var1 := 1 ** 2; var1 := 1 % 2; var1 := 1 & 2; var1 := 1 | 2; var2 := 1 == 2; var2 := 1 != 2; var2 := 1 > 2; var2 := 1 >= 2; var2 := 1 < 2; var2 := 1 <= 2;}";
        typeCheck(input);
    }

    @Test
    public void test12() throws Exception {
        String input = "prog{ float var1; boolean var2;var1 := 1.0 + 2.0; var1 := 1.0 - 2.0; var1 := 1.0 * 2.0; var1 := 1.0 / 2.0; var1 := 1.0 ** 2.0; var2 := 1.0 == 2.0; var2 := 1.0 != 2.0; var2 := 1.0 > 2.0; var2 := 1.0 >= 2.0; var2 := 1.0 < 2.0; var2 := 1.0 <= 2.0;}";
        typeCheck(input);
    }

    @Test
    public void test13() throws Exception {
        String input = "prog{ float var2;var2 := 1 + 2.0; var2 := 1 - 2.0; var2 := 1 * 2.0; var2 := 1 / 2.0; var2 := 1 ** 2.0; var2 := 1.0 + 2; var2 := 1.0 - 2; var2 := 1.0 * 2; var2 := 1.0 / 2; var2 := 1.0 ** 2;}";
        typeCheck(input);
    }

    @Test
    public void test14() throws Exception {
        String input = "prog{ boolean var1;var1 := true & false; var1 := true | false; var1 := true == false; var1 := true != false; var1 := true > false; var1 := true >= false; var1 := true < false; var1 := true <= false;}";
        typeCheck(input);
    }

    @Test
    public void test15() throws Exception {
        String input = "prog {int var1; var1 := cart_x[1.0,1.0]; var1 := cart_y[1.0,1.0];}";
        typeCheck(input);
    }

    @Test
    public void test16() throws Exception {
        String input = "prog {float var1; var1 := polar_a[1,1]; var1 := polar_r[1,1];}";
        typeCheck(input);
    }

    @Test
    public void test17() throws Exception {
        String input = "prog{ image var1; int var2; var2 := var1[0,0];}";
        typeCheck(input);
    }

    @Test
    public void test18() throws Exception {
        String input = "prog{boolean a; boolean b; if(a & b){};}";
        typeCheck(input);
    }

    @Test
    public void test19() throws Exception {
        String input = "prog{boolean cond;int var1; var1 := cond ? var1+ 1 : var1;float var2; var2 := cond ? var2 + 1.0 : var2;}";
        typeCheck(input);
    }

    @Test
    public void test20() throws Exception {
        String input = "prog{int var1; float var2; boolean var3; image var4; filename var5; image var6[500,500];}";
        typeCheck(input);
    }

    @Test
    public void test21() throws Exception {
        String input = "prog{int var1; float var2; boolean var3; image var4; filename var5; image var6[500,500];if(true){int var1; float var2; boolean var3; image var4; filename var5; image var6[500,500];};}";
        typeCheck(input);
    }

    @Test
    public void test22() throws Exception {
        String input = "prog{if(false){int var1; float var2; boolean var3; image var4; filename var5; image var6[500,500];};if(true){int var1; float var2; boolean var3; image var4; filename var5; image var6[500,500];};}";
        typeCheck(input);
    }

    @Test
    public void test23() throws Exception {
        String input = "prog{if(true){int var;}; if(true){input var from @1;};}";
        thrown.expect(SemanticException.class);
        try {
            typeCheck(input);
        } catch (SemanticException e) {
            show(e);
            throw e;
        }
    }

    @Test
    public void test24() throws Exception {
        String input = "prog{if(true){int var;}; input var from @1;}";
        thrown.expect(SemanticException.class);
        try {
            typeCheck(input);
        } catch (SemanticException e) {
            show(e);
            throw e;
        }
    }

    @Test
    public void test25() throws Exception {
        String input = "prog{boolean a; int b; float c; image d;show a; show b; show c; show d;}";
        typeCheck(input);
    }

    @Test
    public void test26() throws Exception {
        String input = "prog{sleep 1.0;}";
        thrown.expect(SemanticException.class);
        try {
            typeCheck(input);
        } catch (SemanticException e) {
            show(e);
            throw e;
        }
    }

    @Test
    public void test27() throws Exception {
        String input = "prog{ int a; float b; boolean c;a := +a; a := -a; a := !a; b := +b; b := -b; b := !b; c := +c; c := -c; c := !c;}";
        thrown.expect(SemanticException.class);
        try {
            typeCheck(input);
        } catch (SemanticException e) {
            show(e);
            throw e;
        }
    }

    @Test
    public void test28() throws Exception {
        String input = "prog{ filename a; image b;a := +a; a := -a; a := !a; b := +b; b := -b; b := !b;}";
        thrown.expect(SemanticException.class);
        try {
            typeCheck(input);
        } catch (SemanticException e) {
            show(e);
            throw e;
        }
    }

    @Test
    public void test29() throws Exception {
        String input = "prog{image var1; red( var1[1,1]) := 5; red( var1[1.0,1.0]) := 5;}";
        typeCheck(input);
    }

    @Test
    public void test30() throws Exception {
        String input = "prog{int var; int var2;input var from @1; input var from @var2; input var from @<<1,2,3,4>>;}";
        typeCheck(input);
    }

    @Test
    public void test31() throws Exception {
        String input = "prog{sleep 1;}";
        typeCheck(input);
    }

    @Test
    public void test32() throws Exception {
        String input = "prog{boolean a; boolean b; while(a & b){};}";
        typeCheck(input);
    }

    @Test
    public void test33() throws Exception {
        String input = "prog{image image1; filename f1; write image1 to f1;}";
        typeCheck(input);
    }


    @Test
    public void test34() throws Exception {
        String input = "p{int var; if(true) {float var; var := 5.0;}; var := 5;}";
        typeCheck(input);
    }

    @Test
    public void test35() throws Exception {
        String input = "p{int var; if(true) {float var; var := 5.0;}; var := 5.0;}";
        thrown.expect(SemanticException.class);
        try {
            typeCheck(input);
        } catch (SemanticException e) {
            show(e);
            throw e;
        }
    }

    @Test
    public void test36() throws Exception {
        String input = "p{int var; if(true) {float var; var := 5;}; var := 5;}";
        thrown.expect(SemanticException.class);
        try {
            typeCheck(input);
        } catch (SemanticException e) {
            show(e);
            throw e;
        }
    }

    @Test
    public void test37() throws Exception {
        String input = "p{int var; if(true) {float var; var := 5;}; var := 5.0;}";
        thrown.expect(SemanticException.class);
        try {
            typeCheck(input);
        } catch (SemanticException e) {
            show(e);
            throw e;
        }
    }

    @Test
    public void test38() throws Exception {
        String input = "prog{int var1; var1 := 1; float var2; var2 := 1.0;boolean var3; var3 := true;filename f1; filename f2; f1 := f2;image var4; image var5[500,500]; var4 := var5;}";
        typeCheck(input);
    }

    @Test
    public void test39() throws Exception {
        String input = "prog{image var; var[0,0] := 1; alpha(var[0,0]) := 1; red(var[0,0]) := 1; green(var[0,0]) := 1; blue(var[0,0]) := 1;}";
        typeCheck(input);
    }

    @Test
    public void test40() throws Exception {
        String input = "prog{ show (1.0 % 2.0);}";
        thrown.expect(SemanticException.class);
        try {
            typeCheck(input);
        } catch (SemanticException e) {
            show(e);
            throw e;
        }
    }

    @Test
    public void test41() throws Exception {
        String input = "prog{ show (1.0 % 2);}";
        thrown.expect(SemanticException.class);
        try {
            typeCheck(input);
        } catch (SemanticException e) {
            show(e);
            throw e;
        }
    }

    @Test
    public void test42() throws Exception {
        String input = "prog{ show (1 & 2.0); show (1 | 2.0);}";
        thrown.expect(SemanticException.class);
        try {
            typeCheck(input);
        } catch (SemanticException e) {
            show(e);
            throw e;
        }
    }

    @Test
    public void test43() throws Exception {
        String input = "prog{ show (1 == 2.0); show (1 != 2.0); show (1 > 2.0); show (1 >= 2.0); show (1 < 2.0); show (1 <= 2.0);}";
        thrown.expect(SemanticException.class);
        try {
            typeCheck(input);
        } catch (SemanticException e) {
            show(e);
            throw e;
        }
    }

    @Test
    public void test44() throws Exception {
        String input = "prog{image var1; red( var1[0,0.0]) := 5;}";
        thrown.expect(SemanticException.class);
        try {
            typeCheck(input);
        } catch (SemanticException e) {
            show(e);
            throw e;
        }
    }

    @Test
    public void test45() throws Exception {
        String input = "prog{image var1; red( var1[0.0,0]) := 5;}";
        thrown.expect(SemanticException.class);
        try {
            typeCheck(input);
        } catch (SemanticException e) {
            show(e);
            throw e;
        }
    }

    @Test
    public void test46() throws Exception {
        String input = "prog{image var1; red( var1[true,false]) := 5;}";
        thrown.expect(SemanticException.class);
        try {
            typeCheck(input);
        } catch (SemanticException e) {
            show(e);
            throw e;
        }
    }

    @Test
    public void test47() throws Exception {
        String input = "prog{red (var1[0,0]) := 5;}";
        thrown.expect(SemanticException.class);
        try {
            typeCheck(input);
        } catch (SemanticException e) {
            show(e);
            throw e;
        }
    }

    @Test
    public void test48() throws Exception {
        String input = "prog{int var1; red( var1[0,0]) := 5;}";
        thrown.expect(SemanticException.class);
        try {
            typeCheck(input);
        } catch (SemanticException e) {
            show(e);
            throw e;
        }
    }

    @Test
    public void test49() throws Exception {
        String input = "blockScope{if(true){ int x; }; int x; x := 5; show x;}";
        typeCheck(input);
    }
}
