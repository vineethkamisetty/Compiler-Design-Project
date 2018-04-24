/**
 * Starter code for CodeGenerator.java used n the class project in COP5556 Programming Language Principles
 * at the University of Florida, Spring 2018.
 * <p>
 * This software is solely for the educational benefit of students
 * enrolled in the course during the Spring 2018 semester.
 * <p>
 * This software, and any software derived from it,  may not be shared with others or posted to public web sites,
 * either during the course or afterwards.
 *
 * @Beverly A. Sanders, 2018
 */


package cop5556sp18;

import cop5556sp18.AST.*;
import cop5556sp18.Types.Type;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.HashMap;

import static cop5556sp18.RuntimeImageSupport.*;
import static cop5556sp18.RuntimePixelOps.*;
import static cop5556sp18.Scanner.Kind.*;
import static cop5556sp18.Types.Type.IMAGE;

public class CodeGenerator implements ASTVisitor, Opcodes {

    /**
     * All methods and variable static.
     */

    private static final int Z = 255;
    /**
     * Indicates whether genPrint and genPrintTOS should generate code.
     */
    private final boolean DEVEL;
    private final boolean GRADE;
    private final int defaultWidth;
    private final int defaultHeight;
    private ClassWriter cw;
    private String className;
    private String sourceFileName;
    private MethodVisitor mv; // visitor of method currently under construction
    // final boolean itf = false;
    private int slot = 1;
    private HashMap<Type, String> hm = new HashMap<>();

    /**
     * @param DEVEL          used as parameter to genPrint and genPrintTOS
     * @param GRADE          used as parameter to genPrint and genPrintTOS
     * @param sourceFileName name of source file, may be null.
     * @param defaultWidth   default width of images
     * @param defaultHeight  default height of images
     */
    CodeGenerator(boolean DEVEL, boolean GRADE, String sourceFileName,
                  int defaultWidth, int defaultHeight) {
        super();
        this.DEVEL = DEVEL;
        this.GRADE = GRADE;
        this.sourceFileName = sourceFileName;
        this.defaultWidth = defaultWidth;
        this.defaultHeight = defaultHeight;

        hm.put(Type.INTEGER, "I");
        hm.put(Type.FLOAT, "F");
        hm.put(Type.BOOLEAN, "Z");
        hm.put(Type.IMAGE, "Ljava/awt/image/BufferedImage;");
        hm.put(Type.FILE, "Ljava/lang/String;");
    }

    @Override
    public Object visitBlock(Block block, Object arg) throws Exception {
        for (ASTNode node : block.decsOrStatements) {
            node.visit(this, null);
        }
        return null;
    }

    @Override
    public Object visitBooleanLiteral(
            ExpressionBooleanLiteral expressionBooleanLiteral, Object arg) {
        mv.visitLdcInsn(expressionBooleanLiteral.value);
        return null;
    }

    @Override
    public Object visitDeclaration(Declaration declaration, Object arg)
            throws Exception {
        declaration.setSlot(this.slot++);
        if (declaration.getTypeName() == IMAGE) {
            if (declaration.width != null && declaration.height != null) {
                declaration.width.visit(this, arg);
                declaration.height.visit(this, arg);
                mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "makeImage", makeImageSig, false);
            } else if (declaration.width == null && declaration.height == null) {
                mv.visitLdcInsn(this.defaultWidth);
                mv.visitLdcInsn(this.defaultHeight);
                mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "makeImage", makeImageSig, false);
            }
            mv.visitVarInsn(ASTORE, declaration.getSlot());
        }
        return null;
    }

    @Override
    public Object visitExpressionBinary(ExpressionBinary expressionBinary,
                                        Object arg) throws Exception {
        Expression e0 = expressionBinary.leftExpression;
        Expression e1 = expressionBinary.rightExpression;

        if (expressionBinary.op == OP_PLUS) {
            e0.visit(this, arg);
            if (e0.getTypeName() == Type.INTEGER) mv.visitInsn(I2F);
            e1.visit(this, arg);
            if (e1.getTypeName() == Type.INTEGER) mv.visitInsn(I2F);
            mv.visitInsn(FADD);
            if (e0.getTypeName() == Type.INTEGER && e1.getTypeName() == Type.INTEGER)
                mv.visitInsn(F2I);
        } else if (expressionBinary.op == OP_MINUS) {
            e0.visit(this, arg);
            if (e0.getTypeName() == Type.INTEGER) mv.visitInsn(I2F);
            e1.visit(this, arg);
            if (e1.getTypeName() == Type.INTEGER) mv.visitInsn(I2F);
            mv.visitInsn(FSUB);
            if (e0.getTypeName() == Type.INTEGER && e1.getTypeName() == Type.INTEGER)
                mv.visitInsn(F2I);
        } else if (expressionBinary.op == OP_TIMES) {
            e0.visit(this, arg);
            if (e0.getTypeName() == Type.INTEGER) mv.visitInsn(I2F);
            e1.visit(this, arg);
            if (e1.getTypeName() == Type.INTEGER) mv.visitInsn(I2F);
            mv.visitInsn(FMUL);
            if (e0.getTypeName() == Type.INTEGER && e1.getTypeName() == Type.INTEGER)
                mv.visitInsn(F2I);
        } else if (expressionBinary.op == OP_DIV) {
            e0.visit(this, arg);
            if (e0.getTypeName() == Type.INTEGER) mv.visitInsn(I2F);
            e1.visit(this, arg);
            if (e1.getTypeName() == Type.INTEGER) mv.visitInsn(I2F);
            mv.visitInsn(FDIV);
            if (e0.getTypeName() == Type.INTEGER && e1.getTypeName() == Type.INTEGER)
                mv.visitInsn(F2I);
        } else if (expressionBinary.op == OP_POWER) {
            e0.visit(this, arg);
            if (e0.getTypeName() == Type.INTEGER) mv.visitInsn(I2D);
            if (e0.getTypeName() == Type.FLOAT) mv.visitInsn(F2D);
            e1.visit(this, arg);
            if (e1.getTypeName() == Type.INTEGER) mv.visitInsn(I2D);
            if (e1.getTypeName() == Type.FLOAT) mv.visitInsn(F2D);
            mv.visitMethodInsn(INVOKESTATIC,
                    "java/lang/Math", "pow",
                    "(DD)D", false);
            if (e0.getTypeName() == Type.INTEGER && e1.getTypeName() == Type.INTEGER)
                mv.visitInsn(D2I);
            else
                mv.visitInsn(D2F);
        } else if (expressionBinary.op == OP_MOD) {
            e0.visit(this, arg);
            if (e0.getTypeName() == Type.INTEGER) mv.visitInsn(I2F);
            e1.visit(this, arg);
            if (e1.getTypeName() == Type.INTEGER) mv.visitInsn(I2F);
            mv.visitInsn(FREM);
            if (e0.getTypeName() == Type.INTEGER && e1.getTypeName() == Type.INTEGER)
                mv.visitInsn(F2I);
        } else if (expressionBinary.op == OP_AND) {
            e0.visit(this, arg);
            e1.visit(this, arg);
            mv.visitInsn(IAND);
        } else if (expressionBinary.op == OP_OR) {
            e0.visit(this, arg);
            e1.visit(this, arg);
            mv.visitInsn(IOR);
        } else if (expressionBinary.op == OP_EQ) {
            e0.visit(this, arg);
            e1.visit(this, arg);
            if (expressionBinary.leftExpression.getTypeName() == Type.INTEGER || expressionBinary.leftExpression.getTypeName() == Type.BOOLEAN)
                compare(IF_ICMPEQ);
            else if (e0.getTypeName() == Type.FLOAT)
                compareFloat(FCMPL, IFNE);
            else
                compare(IF_ACMPEQ);
        } else if (expressionBinary.op == OP_NEQ) {
            e0.visit(this, arg);
            e1.visit(this, arg);
            if (expressionBinary.leftExpression.getTypeName() == Type.INTEGER || expressionBinary.leftExpression.getTypeName() == Type.BOOLEAN)
                compare(IF_ICMPNE);
            else if (e0.getTypeName() == Type.FLOAT)
                compareFloat(FCMPL, IFEQ);
            else
                compare(IF_ACMPNE);
        } else if (expressionBinary.op == OP_GE) {
            e0.visit(this, arg);
            e1.visit(this, arg);
            if (e0.getTypeName() == Type.FLOAT)
                compareFloat(FCMPL, IFLT);
            if (e0.getTypeName() == Type.INTEGER || e0.getTypeName() == Type.BOOLEAN)
                compare(IF_ICMPGE);
        } else if (expressionBinary.op == OP_GT) {
            e0.visit(this, arg);
            e1.visit(this, arg);
            if (e0.getTypeName() == Type.FLOAT)
                compareFloat(FCMPL, IFLE);
            if (e0.getTypeName() == Type.INTEGER || e0.getTypeName() == Type.BOOLEAN)
                compare(IF_ICMPGT);
        } else if (expressionBinary.op == OP_LE) {
            e0.visit(this, arg);
            e1.visit(this, arg);
            if (e0.getTypeName() == Type.FLOAT)
                compareFloat(FCMPG, IFGT);
            if (e0.getTypeName() == Type.INTEGER || e0.getTypeName() == Type.BOOLEAN)
                compare(IF_ICMPLE);
        } else if (expressionBinary.op == OP_LT) {
            e0.visit(this, arg);
            e1.visit(this, arg);
            if (e0.getTypeName() == Type.FLOAT)
                compareFloat(FCMPG, IFGE);
            if (e0.getTypeName() == Type.INTEGER || e0.getTypeName() == Type.BOOLEAN)
                compare(IF_ICMPLT);
        }
        return null;
    }

    private void compare(int opCode) {
        System.out.println("opcode : " + opCode);
        Label trueLabel = new Label();
        Label endLabel = new Label();

        mv.visitJumpInsn(opCode, trueLabel);
        mv.visitInsn(ICONST_0);
        mv.visitJumpInsn(GOTO, endLabel);
        mv.visitLabel(trueLabel);
        mv.visitInsn(ICONST_1);
        mv.visitLabel(endLabel);
    }

    private void compareFloat(int opCode1, int opCode2) {
        Label trueLabel = new Label();
        Label endLabel = new Label();

        mv.visitInsn(opCode1);
        mv.visitJumpInsn(opCode2, trueLabel);
        mv.visitInsn(ICONST_1);
        mv.visitJumpInsn(GOTO, endLabel);
        mv.visitLabel(trueLabel);
        mv.visitInsn(ICONST_0);
        mv.visitLabel(endLabel);
    }

    @Override
    public Object visitExpressionConditional(
            ExpressionConditional expressionConditional, Object arg)
            throws Exception {
        Expression guard = expressionConditional.guard;
        Expression e0 = expressionConditional.trueExpression;
        Expression e1 = expressionConditional.falseExpression;
        guard.visit(this, arg);
        Label trueLabel = new Label();
        Label endLabel = new Label();
        mv.visitJumpInsn(IFEQ, trueLabel);
        e0.visit(this, arg);
        mv.visitJumpInsn(GOTO, endLabel);
        mv.visitLabel(trueLabel);
        e1.visit(this, arg);
        mv.visitLabel(endLabel);
        return null;
    }

    @Override
    public Object visitExpressionFloatLiteral(
            ExpressionFloatLiteral expressionFloatLiteral, Object arg) {
        mv.visitLdcInsn(expressionFloatLiteral.value);
        return null;
    }

    @Override
    public Object visitExpressionFunctionAppWithExpressionArg(
            ExpressionFunctionAppWithExpressionArg expressionFunctionAppWithExpressionArg,
            Object arg) throws Exception {
        Scanner.Kind function = expressionFunctionAppWithExpressionArg.function;
        Expression e = expressionFunctionAppWithExpressionArg.e;
        e.visit(this, arg);
        if (function == KW_abs) {
            mv.visitMethodInsn(INVOKESTATIC,
                    "java/lang/Math", "abs", "(" + hm.get(e.getTypeName()) + ")" + hm.get(e.getTypeName()), false);
        } else if (function == KW_sin) {
            mv.visitInsn(F2D);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "sin", "(D)D", false);
            mv.visitInsn(D2F);
        } else if (function == KW_cos) {
            mv.visitInsn(F2D);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "cos", "(D)D", false);
            mv.visitInsn(D2F);
        } else if (function == KW_atan) {
            mv.visitInsn(F2D);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "atan", "(D)D", false);
            mv.visitInsn(D2F);
        } else if (function == KW_log) {
            mv.visitInsn(F2D);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "log", "(D)D", false);
            mv.visitInsn(D2F);
        } else if (function == KW_red) {
            mv.visitMethodInsn(INVOKESTATIC, RuntimePixelOps.className, "getRed", getRedSig, false);
        } else if (function == KW_green) {
            mv.visitMethodInsn(INVOKESTATIC, RuntimePixelOps.className, "getGreen", getGreenSig, false);
        } else if (function == KW_blue) {
            mv.visitMethodInsn(INVOKESTATIC, RuntimePixelOps.className, "getBlue", getBlueSig, false);
        } else if (function == KW_alpha) {
            mv.visitMethodInsn(INVOKESTATIC, RuntimePixelOps.className, "getAlpha", getAlphaSig, false);
        } else if (function == KW_width) {
            mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "getWidth", getWidthSig, false);
        } else if (function == KW_height) {
            mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "getHeight", getHeightSig, false);
        } else if (function == KW_float) {
            if (e.getTypeName() == Type.INTEGER)
                mv.visitInsn(I2F);
        } else if (function == KW_int) {
            if (e.getTypeName() == Type.FLOAT)
                mv.visitInsn(F2I);
        }
        return null;
    }

    @Override
    public Object visitExpressionFunctionAppWithPixel(
            ExpressionFunctionAppWithPixel expressionFunctionAppWithPixel,
            Object arg) throws Exception {
        Expression e0 = expressionFunctionAppWithPixel.e0;
        Expression e1 = expressionFunctionAppWithPixel.e1;
        if (expressionFunctionAppWithPixel.name == KW_cart_x || expressionFunctionAppWithPixel.name == KW_cart_y) {
            if (e0.getTypeName() == Type.FLOAT && e1.getTypeName() == Type.FLOAT) {
                e1.visit(this, arg);
                mv.visitInsn(F2D);
                if (expressionFunctionAppWithPixel.name == KW_cart_x)
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "cos", "(D)D", false);
                else
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "sin", "(D)D", false);
                mv.visitInsn(D2F);
                e0.visit(this, arg);
                mv.visitInsn(FMUL);
                mv.visitInsn(F2I);
            }
        } else if (expressionFunctionAppWithPixel.name == KW_polar_a || expressionFunctionAppWithPixel.name == KW_polar_r) {
            if (e0.getTypeName() == Type.INTEGER && e1.getTypeName() == Type.INTEGER) {
                if (expressionFunctionAppWithPixel.name == KW_polar_a) {
                    e1.visit(this, arg);
                    mv.visitInsn(I2D);
                    e0.visit(this, arg);
                    mv.visitInsn(I2D);
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "atan2", "(DD)D", false);
                } else {
                    e0.visit(this, arg);
                    mv.visitInsn(I2D);
                    e1.visit(this, arg);
                    mv.visitInsn(I2D);
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "hypot", "(DD)D", false);
                }
                mv.visitInsn(D2F);
            }
        }
        return null;
    }

    @Override
    public Object visitExpressionIdent(ExpressionIdent expressionIdent,
                                       Object arg) {
        Declaration dec = expressionIdent.getDec();
        if (dec.getTypeName() == Type.FLOAT) {
            mv.visitVarInsn(FLOAD, dec.getSlot());

        } else if (dec.getTypeName() == Type.INTEGER) {
            mv.visitVarInsn(ILOAD, dec.getSlot());

        } else if (dec.getTypeName() == Type.BOOLEAN) {
            mv.visitVarInsn(ILOAD, dec.getSlot());

        } else if (dec.getTypeName() == Type.FILE) {
            mv.visitVarInsn(ALOAD, dec.getSlot());

        } else if (dec.getTypeName() == Type.IMAGE) {
            mv.visitVarInsn(ALOAD, dec.getSlot());

        }
        return null;
    }

    @Override
    public Object visitExpressionIntegerLiteral(
            ExpressionIntegerLiteral expressionIntegerLiteral, Object arg) {
        mv.visitLdcInsn(expressionIntegerLiteral.value);
        return null;
    }

    @Override
    public Object visitExpressionPixel(ExpressionPixel expressionPixel,
                                       Object arg) throws Exception {
        Declaration dec = expressionPixel.getDec();
        mv.visitVarInsn(ALOAD, dec.getSlot());
        expressionPixel.pixelSelector.visit(this, arg);
        mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "getPixel", getPixelSig, false);
        return null;
    }

    @Override
    public Object visitExpressionPixelConstructor(
            ExpressionPixelConstructor expressionPixelConstructor, Object arg) throws Exception {
        expressionPixelConstructor.alpha.visit(this, arg);
        expressionPixelConstructor.red.visit(this, arg);
        expressionPixelConstructor.green.visit(this, arg);
        expressionPixelConstructor.blue.visit(this, arg);
        mv.visitMethodInsn(INVOKESTATIC, RuntimePixelOps.className, "makePixel", makePixelSig, false);
        return null;
    }

    @Override
    public Object visitExpressionPredefinedName(
            ExpressionPredefinedName expressionPredefinedName, Object arg) {
        if (expressionPredefinedName.name == KW_Z) {
            mv.visitLdcInsn(Z);
        } else if (expressionPredefinedName.name == KW_default_height) {
            mv.visitLdcInsn(this.defaultHeight);
        } else if (expressionPredefinedName.name == KW_default_width) {
            mv.visitLdcInsn(this.defaultWidth);
        }
        return null;
    }

    @Override
    public Object visitExpressionUnary(ExpressionUnary expressionUnary,
                                       Object arg) throws Exception {
        if (expressionUnary.op == OP_PLUS) {
            expressionUnary.expression.visit(this, arg);
        } else if (expressionUnary.op == OP_MINUS) {
            expressionUnary.expression.visit(this, arg);
            if (expressionUnary.expression.getTypeName() == Type.FLOAT)
                mv.visitInsn(FNEG);
            if (expressionUnary.expression.getTypeName() == Type.INTEGER)
                mv.visitInsn(INEG);
        } else if (expressionUnary.op == OP_EXCLAMATION) {
            expressionUnary.expression.visit(this, arg);
            if (expressionUnary.expression.getTypeName() == Type.INTEGER) {
                mv.visitInsn(ICONST_M1);
                mv.visitInsn(IXOR);
            }
            if (expressionUnary.expression.getTypeName() == Type.BOOLEAN)
                compare(IFEQ);
        }
        return null;
    }

    @Override
    public Object visitLHSIdent(LHSIdent lhsIdent, Object arg) {
        Declaration dec = lhsIdent.getDec();
        if (lhsIdent.getTypeName() == Type.FLOAT) {
            mv.visitVarInsn(FSTORE, dec.getSlot());

        } else if (lhsIdent.getTypeName() == Type.INTEGER) {
            mv.visitVarInsn(ISTORE, dec.getSlot());

        } else if (lhsIdent.getTypeName() == Type.BOOLEAN) {
            mv.visitVarInsn(ISTORE, dec.getSlot());

        } else if (lhsIdent.getTypeName() == Type.FILE) {
            mv.visitVarInsn(ASTORE, dec.getSlot());

        } else if (lhsIdent.getTypeName() == Type.IMAGE) {
            mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "deepCopy", deepCopySig, false);
            mv.visitVarInsn(ASTORE, dec.getSlot());

        }
        return null;
    }

    @Override
    public Object visitLHSPixel(LHSPixel lhsPixel, Object arg) throws Exception {
        Declaration dec = lhsPixel.getDec();
        mv.visitVarInsn(ALOAD, dec.getSlot());

        lhsPixel.pixelSelector.visit(this, arg);
        mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "setPixel", setPixelSig, false);
        return null;
    }

    @Override
    public Object visitLHSSample(LHSSample lhsSample, Object arg) throws Exception {
        mv.visitVarInsn(ALOAD, lhsSample.getDec().getSlot());
        lhsSample.pixelSelector.visit(this, arg);
        if (lhsSample.color == KW_alpha) mv.visitLdcInsn(RuntimePixelOps.ALPHA);
        else if (lhsSample.color == KW_red) mv.visitLdcInsn(RuntimePixelOps.RED);
        else if (lhsSample.color == KW_green) mv.visitLdcInsn(RuntimePixelOps.GREEN);
        else if (lhsSample.color == KW_blue) mv.visitLdcInsn(RuntimePixelOps.BLUE);
        mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "updatePixelColor", updatePixelColorSig, false);
        return null;
    }

    @Override
    public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws Exception {
        pixelSelector.ex.visit(this, arg);
        pixelSelector.ey.visit(this, arg);

        if (pixelSelector.ex.getTypeName() == Type.FLOAT) {
            mv.visitInsn(F2D);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "cos", "(D)D", false);
            mv.visitInsn(D2F);
            mv.visitInsn(FMUL);
            mv.visitInsn(F2I);
            pixelSelector.ex.visit(this, arg);
            pixelSelector.ey.visit(this, arg);
            mv.visitInsn(F2D);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "sin", "(D)D", false);
            mv.visitInsn(D2F);
            mv.visitInsn(FMUL);
            mv.visitInsn(F2I);
        }
        return null;
    }

    @Override
    public Object visitProgram(Program program, Object arg) throws Exception {
        cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        // cw = new ClassWriter(0); //If the call to mv.visitMaxs(1, 1) crashes,
        // it is
        // sometime helpful to
        // temporarily run it without COMPUTE_FRAMES. You probably
        // won't get a completely correct classfile, but
        // you will be able to see the code that was
        // generated.
        className = program.progName;
        String sourceFileName = (String) arg;
        cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object", null);
        cw.visitSource(sourceFileName, null);

        // create main method
        mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
        // initialize
        mv.visitCode();

        // add label before first instruction
        Label mainStart = new Label();
        mv.visitLabel(mainStart);

        CodeGenUtils.genLog(DEVEL, mv, "entering main");

        program.block.visit(this, arg);

        // generates code to add string to log
        CodeGenUtils.genLog(DEVEL, mv, "leaving main");

        // adds the required (by the JVM) return statement to main
        mv.visitInsn(RETURN);

        // adds label at end of code
        Label mainEnd = new Label();
        mv.visitLabel(mainEnd);
        mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);
        // Because we use ClassWriter.COMPUTE_FRAMES as a parameter in the
        // constructor,
        // asm will calculate this itself and the parameters are ignored.
        // If you have trouble with failures in this routine, it may be useful
        // to temporarily change the parameter in the ClassWriter constructor
        // from COMPUTE_FRAMES to 0.
        // The generated classfile will not be correct, but you will at least be
        // able to see what is in it.
        mv.visitMaxs(0, 0);

        // terminate construction of main method
        mv.visitEnd();

        // terminate class construction
        cw.visitEnd();

        // generate classfile as byte array and return
        return cw.toByteArray();
    }

    @Override
    public Object visitStatementAssign(StatementAssign statementAssign,
                                       Object arg) throws Exception {
        statementAssign.e.visit(this, arg);
        statementAssign.lhs.visit(this, arg);
        return null;
    }

    @Override
    public Object visitStatementIf(StatementIf statementIf, Object arg)
            throws Exception {
        statementIf.guard.visit(this, arg);
        Label afterBlock = new Label();
        mv.visitJumpInsn(IFEQ, afterBlock);
        statementIf.b.visit(this, arg);
        mv.visitLabel(afterBlock);
        return null;
    }

    @Override
    public Object visitStatementInput(StatementInput statementInput, Object arg)
            throws Exception {
        Declaration dec = statementInput.getDec();

        mv.visitVarInsn(ALOAD, 0);
        statementInput.e.visit(this, arg);
        mv.visitInsn(AALOAD);

        if (dec.getTypeName() == Type.INTEGER) {
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(" + hm.get(Type.FILE) + ")" + hm.get(dec.getTypeName()), false);
            mv.visitVarInsn(ISTORE, dec.getSlot());

        } else if (dec.getTypeName() == Type.FLOAT) {
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "parseFloat", "(" + hm.get(Type.FILE) + ")" + hm.get(dec.getTypeName()), false);
            mv.visitVarInsn(FSTORE, dec.getSlot());

        } else if (dec.getTypeName() == Type.BOOLEAN) {
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(" + hm.get(Type.FILE) + ")" + hm.get(dec.getTypeName()), false);
            mv.visitVarInsn(ISTORE, dec.getSlot());

        } else if (dec.getTypeName() == Type.FILE) {
            mv.visitVarInsn(ASTORE, dec.getSlot());

        } else if (dec.getTypeName() == Type.IMAGE) {
            if (statementInput.getDec().width != null && statementInput.getDec().height != null) {
                mv.visitVarInsn(ALOAD, dec.getSlot());
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/awt/image/BufferedImage", "getWidth", "()I", false);
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
                mv.visitVarInsn(ALOAD, dec.getSlot());
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/awt/image/BufferedImage", "getHeight", "()I", false);
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
            } else {
                mv.visitInsn(ACONST_NULL);
                mv.visitInsn(ACONST_NULL);
            }
            mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "readImage", readImageSig, false);
            mv.visitVarInsn(ASTORE, dec.getSlot());
        }
        return null;
    }

    @Override
    public Object visitStatementShow(StatementShow statementShow, Object arg)
            throws Exception {
        /*
          For integers, booleans, and floats, generate code to print to
          console. For images, generate code to display in a frame.

          In all cases, invoke CodeGenUtils.genLogTOS(GRADE, mv, type); before
          consuming top of stack.
         */
        statementShow.e.visit(this, arg);
        Type type = statementShow.e.getType();
        if (type == Type.INTEGER) {
            CodeGenUtils.genLogTOS(GRADE, mv, type);
            mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            mv.visitInsn(Opcodes.SWAP);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(I)V", false);

        } else if (type == Type.BOOLEAN) {
            CodeGenUtils.genLogTOS(GRADE, mv, type);
            mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            mv.visitInsn(Opcodes.SWAP);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Z)V", false);

        } else if (type == Type.FLOAT) {
            CodeGenUtils.genLogTOS(GRADE, mv, type);
            mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            mv.visitInsn(Opcodes.SWAP);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(F)V", false);

        } else if (type == Type.FILE) {
            CodeGenUtils.genLogTOS(GRADE, mv, type);
            mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            mv.visitInsn(Opcodes.SWAP);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);

        } else if (type == Type.IMAGE) {
            CodeGenUtils.genLogTOS(GRADE, mv, type);
            mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "makeFrame", makeFrameSig, false);
        }
        return null;
    }

    @Override
    public Object visitStatementSleep(StatementSleep statementSleep, Object arg)
            throws Exception {
        statementSleep.duration.visit(this, arg);
        mv.visitInsn(I2L);
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "sleep", "(J)V", false);
        return null;
    }

    @Override
    public Object visitStatementWhile(StatementWhile statementWhile, Object arg)
            throws Exception {
        Label whileBlock = new Label();
        Label whileExp = new Label();
        mv.visitJumpInsn(GOTO, whileExp);

        mv.visitLabel(whileBlock);
        statementWhile.b.visit(this, arg);

        mv.visitLabel(whileExp);
        statementWhile.guard.visit(this, arg);
        mv.visitJumpInsn(IFNE, whileBlock);

        return null;
    }

    @Override
    public Object visitStatementWrite(StatementWrite statementWrite, Object arg) throws Exception {
        Declaration source = statementWrite.getSourceDec();
        Declaration des = statementWrite.getDestDec();
        mv.visitVarInsn(ALOAD, source.getSlot());
        mv.visitVarInsn(ALOAD, des.getSlot());
        mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "write", writeSig, false);
        return null;
    }

}
