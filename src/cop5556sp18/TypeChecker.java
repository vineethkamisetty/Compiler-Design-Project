package cop5556sp18;

import cop5556sp18.AST.*;
import cop5556sp18.Scanner.Kind;
import cop5556sp18.Scanner.Token;

import static cop5556sp18.Scanner.Kind.*;
import static cop5556sp18.Types.Type.*;

public class TypeChecker implements ASTVisitor {

    private SymbolTable symbolTable = new SymbolTable();
    private Kind[] Function_1 = {KW_abs, KW_red, KW_green, KW_blue, KW_alpha};
    private Kind[] Function_2 = {KW_sin, KW_cos, KW_atan, KW_abs, KW_log};
    TypeChecker() {
    }

    // Name is only used for naming the output file.
    // Visit the child block to type check program.
    @Override
    public Object visitProgram(Program program, Object arg) throws Exception {
        program.block.visit(this, arg);
        return null;
    }

    @Override
    public Object visitBlock(Block block, Object arg) throws Exception {
        symbolTable.enterScope();
        for (ASTNode node : block.decsOrStatements) {
            node.visit(this, arg);
        }
        symbolTable.leaveScope();
        return null;
    }

    @Override
    public Object visitDeclaration(Declaration declaration, Object arg) throws Exception {
        if (!symbolTable.insert(declaration.name, declaration))
            throw new SemanticException(declaration.firstToken, "ERROR: Unable to declare the variable in Dec at " + declaration);
        Expression e0 = declaration.height;
        Expression e1 = declaration.width;
        if (e0 != null && e1 != null) {
            if ((e0.visit(this, arg)) != INTEGER
                    || (e1.visit(this, arg)) != INTEGER
                    || !declaration.type.equals(Scanner.Kind.KW_image))
                throw new SemanticException(declaration.firstToken, "ERROR: Unable to declare the variable in Dec at " + declaration);
        } else if (e0 != null || e1 != null)
            throw new SemanticException(declaration.firstToken, "ERROR: Unable to declare the variable in Dec at " + declaration);
        declaration.setTypeName(Types.getType(declaration.type));
        return declaration.getTypeName();
    }

    @Override
    public Object visitStatementWrite(StatementWrite statementWrite, Object arg) throws Exception {
        Declaration sourceDec = symbolTable.lookup(statementWrite.sourceName);
        Declaration destDec = symbolTable.lookup(statementWrite.destName);
        statementWrite.setSourceDec(sourceDec);
        statementWrite.setDestDec(destDec);
        if (sourceDec == null || destDec == null)
            throw new SemanticException(statementWrite.firstToken, "ERROR: Unable to write the variable at " + statementWrite);
        if (sourceDec.getTypeName() != IMAGE || destDec.getTypeName() != FILE)
            throw new SemanticException(statementWrite.firstToken, "ERROR: Unable to write the variable at " + statementWrite);
        return statementWrite;
    }

    @Override
    public Object visitStatementInput(StatementInput statementInput, Object arg) throws Exception {
        Declaration dec = symbolTable.lookup(statementInput.destName);
        statementInput.setDec(dec);
        if (dec == null || (statementInput.e.visit(this, arg)) != INTEGER)
            throw new SemanticException(statementInput.firstToken, "ERROR: Unable to input the variable at " + statementInput);
        return statementInput;
    }

    @Override
    public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws Exception {
        if (pixelSelector.ex.visit(this, arg) != pixelSelector.ey.visit(this, arg)
                || (pixelSelector.ex.visit(this, arg) != INTEGER && pixelSelector.ex.visit(this, arg) != FLOAT))
            throw new SemanticException(pixelSelector.firstToken, "ERROR: Unable to select the variable at " + pixelSelector);
        return pixelSelector;
    }

    @Override
    public Object visitExpressionConditional(ExpressionConditional expressionConditional, Object arg) throws Exception {
        if (expressionConditional.guard.visit(this, arg) != BOOLEAN
                || expressionConditional.trueExpression.visit(this, arg) != expressionConditional.falseExpression.visit(this, arg))
            throw new SemanticException(expressionConditional.firstToken, "ERROR: Unable to check the condition at " + expressionConditional);
        expressionConditional.setTypeName((Types.Type) expressionConditional.trueExpression.visit(this, arg));
        return expressionConditional.getTypeName();
    }

    @Override
    public Object visitExpressionBinary(ExpressionBinary expressionBinary, Object arg) throws Exception {
        Expression e0 = expressionBinary.leftExpression;
        Expression e1 = expressionBinary.rightExpression;
        if (expressionBinary.op == OP_AND || expressionBinary.op == OP_OR) {
            if (e0.visit(this, arg) == BOOLEAN && e1.visit(this, arg) == BOOLEAN)
                expressionBinary.setTypeName(BOOLEAN);
            if (e0.visit(this, arg) == INTEGER && e1.visit(this, arg) == INTEGER)
                expressionBinary.setTypeName(INTEGER);
        } else if (expressionBinary.op == OP_PLUS || expressionBinary.op == OP_MINUS || expressionBinary.op == OP_DIV
                || expressionBinary.op == OP_TIMES || expressionBinary.op == OP_POWER) {
            if ((e0.visit(this, arg) == FLOAT) && (e1.visit(this, arg) == FLOAT || e1.visit(this, arg) == INTEGER))
                expressionBinary.setTypeName(FLOAT);
            if (e0.visit(this, arg) == INTEGER && e1.visit(this, arg) == FLOAT)
                expressionBinary.setTypeName(FLOAT);
            if (e0.visit(this, arg) == INTEGER && e1.visit(this, arg) == INTEGER)
                expressionBinary.setTypeName(INTEGER);
        } else if (expressionBinary.op == OP_MOD) {
            if (e0.visit(this, arg) == INTEGER && e1.visit(this, arg) == INTEGER)
                expressionBinary.setTypeName(INTEGER);
        } else if (expressionBinary.op == OP_EQ || expressionBinary.op == OP_NEQ
                || expressionBinary.op == OP_GT || expressionBinary.op == OP_GE
                || expressionBinary.op == OP_LT || expressionBinary.op == OP_LE) {
            if (e0.visit(this, arg) == INTEGER && e1.visit(this, arg) == INTEGER)
                expressionBinary.setTypeName(BOOLEAN);
            if (e0.visit(this, arg) == FLOAT && e1.visit(this, arg) == FLOAT)
                expressionBinary.setTypeName(BOOLEAN);
            if (e0.visit(this, arg) == BOOLEAN && e1.visit(this, arg) == BOOLEAN)
                expressionBinary.setTypeName(BOOLEAN);
        }
        if (expressionBinary.getTypeName() == null) {
            throw new SemanticException(expressionBinary.firstToken, "ERROR: Unable to check the expressionBinary at " + expressionBinary);
        }
        return expressionBinary.getTypeName();
    }

    @Override
    public Object visitExpressionUnary(ExpressionUnary expressionUnary, Object arg) throws Exception {
        Expression e = expressionUnary.expression;
        if ((expressionUnary.op == OP_EXCLAMATION && (e.visit(this, arg) == INTEGER || e.visit(this, arg) == BOOLEAN))
                || ((expressionUnary.op == OP_PLUS || expressionUnary.op == OP_MINUS) && (e.visit(this, arg) == INTEGER || e.visit(this, arg) == FLOAT))) {
            expressionUnary.setTypeName((Types.Type) expressionUnary.expression.visit(this, arg));
            return expressionUnary.getTypeName();
        } else
            throw new SemanticException(expressionUnary.firstToken, "ERROR: Unable to check the expressionBinary at " + expressionUnary);
    }

    @Override
    public Object visitExpressionIntegerLiteral(ExpressionIntegerLiteral expressionIntegerLiteral, Object arg) {
        expressionIntegerLiteral.setTypeName(INTEGER);
        return expressionIntegerLiteral.getTypeName();
    }

    @Override
    public Object visitBooleanLiteral(ExpressionBooleanLiteral expressionBooleanLiteral, Object arg) {
        expressionBooleanLiteral.setTypeName(BOOLEAN);
        return expressionBooleanLiteral.getTypeName();
    }

    @Override
    public Object visitExpressionPredefinedName(ExpressionPredefinedName expressionPredefinedName, Object arg) {
        expressionPredefinedName.setTypeName(INTEGER);
        return expressionPredefinedName.getTypeName();
    }

    @Override
    public Object visitExpressionFloatLiteral(ExpressionFloatLiteral expressionFloatLiteral, Object arg) {
        expressionFloatLiteral.setTypeName(FLOAT);
        return expressionFloatLiteral.getTypeName();
    }

    @Override
    public Object visitExpressionFunctionAppWithExpressionArg(
            ExpressionFunctionAppWithExpressionArg expressionFunctionAppWithExpressionArg, Object arg)
            throws Exception {
        Expression e = expressionFunctionAppWithExpressionArg.e;
        Kind k = expressionFunctionAppWithExpressionArg.function;
        if (isKind(k, Function_1) && e.visit(this, arg) == INTEGER)
            expressionFunctionAppWithExpressionArg.setTypeName(INTEGER);
        else if (isKind(k, Function_2) && e.visit(this, arg) == FLOAT)
            expressionFunctionAppWithExpressionArg.setTypeName(FLOAT);
        else if ((k == KW_width || k == KW_height) && e.visit(this, arg) == IMAGE)
            expressionFunctionAppWithExpressionArg.setTypeName(INTEGER);
        else if (k == KW_float) {
            if (e.visit(this, arg) == INTEGER)
                expressionFunctionAppWithExpressionArg.setTypeName(FLOAT);
            if (e.visit(this, arg) == FLOAT)
                expressionFunctionAppWithExpressionArg.setTypeName(FLOAT);
        } else if (k == KW_int) {
            if (e.visit(this, arg) == INTEGER)
                expressionFunctionAppWithExpressionArg.setTypeName(INTEGER);
            if (e.visit(this, arg) == FLOAT)
                expressionFunctionAppWithExpressionArg.setTypeName(INTEGER);
        }
        if (expressionFunctionAppWithExpressionArg.getTypeName() == null)
            throw new SemanticException(expressionFunctionAppWithExpressionArg.firstToken, "ERROR: Unable to check the expressionfunctionarg at " + expressionFunctionAppWithExpressionArg);
        return expressionFunctionAppWithExpressionArg.getTypeName();
    }

    private boolean isKind(Kind kind, Kind[] kinds) {
        for (Kind k : kinds) {
            if (k == kind)
                return true;
        }
        return false;
    }

    @Override
    public Object visitExpressionFunctionAppWithPixel(ExpressionFunctionAppWithPixel expressionFunctionAppWithPixel,
                                                      Object arg) throws Exception {
        if (expressionFunctionAppWithPixel.name == Kind.KW_cart_x || expressionFunctionAppWithPixel.name == Kind.KW_cart_y) {
            if (expressionFunctionAppWithPixel.e0.visit(this, arg) == FLOAT && expressionFunctionAppWithPixel.e1.visit(this, arg) == FLOAT) {
                expressionFunctionAppWithPixel.setTypeName(INTEGER);
                return expressionFunctionAppWithPixel.getTypeName();
            }
        } else if (expressionFunctionAppWithPixel.name == Kind.KW_polar_a || expressionFunctionAppWithPixel.name == Kind.KW_polar_r) {
            if (expressionFunctionAppWithPixel.e0.visit(this, arg) == INTEGER && expressionFunctionAppWithPixel.e1.visit(this, arg) == INTEGER) {
                expressionFunctionAppWithPixel.setTypeName(FLOAT);
                return expressionFunctionAppWithPixel.getTypeName();
            }
        }
        throw new SemanticException(expressionFunctionAppWithPixel.firstToken, "ERROR: Unable to check the expressionFunctionAppWithPixel at " + expressionFunctionAppWithPixel);
    }

    @Override
    public Object visitExpressionPixelConstructor(ExpressionPixelConstructor expressionPixelConstructor, Object arg)
            throws Exception {
        if (expressionPixelConstructor.alpha.visit(this, arg) != INTEGER
                || expressionPixelConstructor.red.visit(this, arg) != INTEGER
                || expressionPixelConstructor.green.visit(this, arg) != INTEGER
                || expressionPixelConstructor.blue.visit(this, arg) != INTEGER)
            throw new SemanticException(expressionPixelConstructor.firstToken, "ERROR: Unable to check the expressionPixelConstructor at " + expressionPixelConstructor);
        expressionPixelConstructor.setTypeName(INTEGER);
        return expressionPixelConstructor.getTypeName();
    }

    @Override
    public Object visitStatementAssign(StatementAssign statementAssign, Object arg) throws Exception {
        if (statementAssign.lhs.visit(this, arg) != statementAssign.e.visit(this, arg))
            throw new SemanticException(statementAssign.firstToken, "ERROR: Unable to check the statementAssign at " + statementAssign);
        return statementAssign;
    }

    @Override
    public Object visitStatementShow(StatementShow statementShow, Object arg) throws Exception {
        if ((statementShow.e.visit(this, arg) != IMAGE)
                && (statementShow.e.visit(this, arg) != INTEGER)
                && (statementShow.e.visit(this, arg) != BOOLEAN)
                && (statementShow.e.visit(this, arg) != FLOAT))
            throw new SemanticException(statementShow.firstToken, "ERROR: Unable to check the statementShow at " + statementShow);
        return statementShow;
    }

    @Override
    public Object visitExpressionPixel(ExpressionPixel expressionPixel, Object arg) throws Exception {
        Declaration dec = symbolTable.lookup(expressionPixel.name);
        expressionPixel.setDec(dec);
        if (dec == null || dec.getTypeName() != IMAGE)
            throw new SemanticException(expressionPixel.firstToken, "ERROR: Unable to check the expressionPixel at " + expressionPixel);
        expressionPixel.pixelSelector.visit(this,arg);
        expressionPixel.setTypeName(INTEGER);
        return expressionPixel.getTypeName();
    }

    @Override
    public Object visitExpressionIdent(ExpressionIdent expressionIdent, Object arg) throws Exception {
        Declaration dec = symbolTable.lookup(expressionIdent.name);
        expressionIdent.setDec(dec);
        if (dec == null)
            throw new SemanticException(expressionIdent.firstToken, "ERROR: Unable to check the expressionIdent at " + expressionIdent);
        expressionIdent.setTypeName(dec.getTypeName());
        return expressionIdent.getTypeName();
    }

    @Override
    public Object visitLHSSample(LHSSample lhsSample, Object arg) throws Exception {
        Declaration dec = symbolTable.lookup(lhsSample.name);
        lhsSample.setDec(dec);
        if (dec == null || dec.getTypeName() != IMAGE || lhsSample.pixelSelector.ex.visit(this, arg) != lhsSample.pixelSelector.ex.visit(this, arg))
            throw new SemanticException(lhsSample.firstToken, "ERROR: Unable to check the lhsSample at " + lhsSample);
        lhsSample.pixelSelector.visit(this, arg);
        lhsSample.setTypeName(INTEGER);
        return lhsSample.getTypeName();
    }

    @Override
    public Object visitLHSPixel(LHSPixel lhsPixel, Object arg) throws Exception {
        Declaration dec = symbolTable.lookup(lhsPixel.name);
        lhsPixel.setDec(dec);
        if (dec == null || dec.getTypeName() != IMAGE)
            throw new SemanticException(lhsPixel.firstToken, "ERROR: Unable to check the lhsPixel at " + lhsPixel);
        lhsPixel.pixelSelector.visit(this, arg);
        lhsPixel.setTypeName(INTEGER);
        return lhsPixel.getTypeName();
    }

    @Override
    public Object visitLHSIdent(LHSIdent lhsIdent, Object arg) throws Exception {
        Declaration dec = symbolTable.lookup(lhsIdent.name);
        lhsIdent.setDec(dec);
        if (dec == null)
            throw new SemanticException(lhsIdent.firstToken, "ERROR: Unable to check the lhsIdent at " + lhsIdent);
        lhsIdent.setTypeName(dec.getTypeName());
        return lhsIdent.getTypeName();
    }

    @Override
    public Object visitStatementIf(StatementIf statementIf, Object arg) throws Exception {
        if ((statementIf.guard.visit(this, arg)) != BOOLEAN)
            throw new SemanticException(statementIf.firstToken, "ERROR: Unable to check the statementIf at " + statementIf);
        statementIf.b.visit(this, arg);
        return statementIf;
    }

    @Override
    public Object visitStatementWhile(StatementWhile statementWhile, Object arg) throws Exception {
        if ((statementWhile.guard.visit(this, arg)) != BOOLEAN)
            throw new SemanticException(statementWhile.firstToken, "ERROR: Unable to check the statementWhile at " + statementWhile);
        statementWhile.b.visit(this, arg);
        return statementWhile;
    }

    @Override
    public Object visitStatementSleep(StatementSleep statementSleep, Object arg) throws Exception {
        if ((statementSleep.duration.visit(this, arg)) != INTEGER)
            throw new SemanticException(statementSleep.firstToken, "ERROR: Unable to check the statementWhile at " + statementSleep);
        return statementSleep;
    }

    @SuppressWarnings("serial")
    static class SemanticException extends Exception {
        Token t;

        SemanticException(Token t, String message) {
            super(message);
            this.t = t;
        }
    }
}
