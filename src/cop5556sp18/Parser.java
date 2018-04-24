package cop5556sp18;
/* *
 * Initial code for SimpleParser for the class project in COP5556 Programming Language Principles
 * at the University of Florida, Spring 2018.
 *
 * This software is solely for the educational benefit of students
 * enrolled in the course during the Spring 2018 semester.
 *
 * This software, and any software derived from it,  may not be shared with others or posted to public web sites,
 * either during the course or afterwards.
 *
 *  @Beverly A. Sanders, 2018
 */

import cop5556sp18.AST.*;
import cop5556sp18.Scanner.Kind;
import cop5556sp18.Scanner.Token;

import java.util.ArrayList;

import static cop5556sp18.Scanner.Kind.*;

public class Parser {

    private Scanner scanner;
    private Token t;
    private Kind[] firstDec = {KW_int, KW_boolean, KW_image, KW_float, KW_filename};
    private Kind[] firstStatement = {KW_input, KW_write, KW_while, KW_if, KW_show, KW_sleep, KW_red, KW_green, KW_blue,
            KW_alpha, IDENTIFIER};
    private Kind[] lhs = {IDENTIFIER, KW_alpha, KW_blue, KW_green, KW_red};
    private Kind[] Color = {KW_red, KW_green, KW_blue, KW_alpha};

    /*
     * Block ::= { ( (Declaration | Statement) ; )* }
     */
    private Kind[] FunctionName = {KW_sin, KW_cos, KW_atan, KW_abs, KW_log, KW_cart_x, KW_cart_y, KW_polar_a, KW_polar_r,
            KW_int, KW_float, KW_width, KW_height, KW_red, KW_green, KW_blue, KW_alpha};
    private Kind[] PredefinedName = {KW_Z, KW_default_height, KW_default_width};


    Parser(Scanner scanner) {
        this.scanner = scanner;
        t = scanner.nextToken();
    }

    Program parse() throws SyntaxException {
        Program p = program();
        matchEOF();
        return p;
    }

    /*
     * Program ::= Identifier Block
     */
    private Program program() throws SyntaxException {
        Token first = t;
        Token prog_name = match(IDENTIFIER);
        Block bk = block();
        return new Program(first, prog_name, bk);
    }

    private Block block() throws SyntaxException {
        Token first = t;
        match(LBRACE);
        ArrayList<ASTNode> decOrst = new ArrayList<>();

        while (isKind(firstDec) | isKind(firstStatement)) {
            if (isKind(firstDec)) {
                decOrst.add(declaration());
            } else if (isKind(firstStatement)) {
                decOrst.add(statement());
            }
            match(SEMI);
        }
        match(RBRACE);
        return new Block(first, decOrst);
    }

    private Declaration declaration() throws SyntaxException {
        Token first = t;
        Token tmp;
        Token name;
        Expression width = null;
        Expression height = null;

        if (!isKind(KW_image)) {
            tmp = consume();
            name = match(IDENTIFIER);
        } else if (isKind(firstDec)) {
            tmp = consume();
            name = match(IDENTIFIER);
            if (isKind(LSQUARE)) {
                consume();
                width = expression();
                match(COMMA);
                height = expression();
                match(RSQUARE);
            }
        } else {
            throw new SyntaxException(t, "Syntax Error :: declaration error :: Token : " + t);
        }
        return new Declaration(first, tmp, name, width, height);
    }

    public Statement statement() throws SyntaxException {
        Statement st;
        switch (t.kind) {
            case KW_input:
                st = statementInput();
                break;
            case KW_write:
                st = statementWrite();
                break;
            case KW_while:
                st = statementWhile();
                break;
            case KW_if:
                st = statementIf();
                break;
            case KW_show:
                st = statementShow();
                break;
            case KW_sleep:
                st = statementSleep();
                break;
            default:
                if (isKind(lhs))
                    st = statementAssignment();
                else
                    throw new SyntaxException(t, "Syntax Error :: statement error :: Token : " + t);
                break;
        }
        return st;
    }

    public Expression expression() throws SyntaxException {
        Token first = t;
        Expression or;
        Expression trueExp;
        Expression falseExp;
        or = orexpression();
        if (isKind(OP_QUESTION)) {
            consume();
            trueExp = expression();
            match(OP_COLON);
            falseExp = expression();
            return new ExpressionConditional(first, or, trueExp, falseExp);
        }
        return or;
    }

    private StatementInput statementInput() throws SyntaxException {
        Token first = t;
        consume(); // consumes input
        Token destName = match(IDENTIFIER);
        match(KW_from);
        match(OP_AT);
        Expression exp = expression();
        return new StatementInput(first, destName, exp);
    }

    private StatementWrite statementWrite() throws SyntaxException {
        Token first = t;
        consume();
        Token scrName = match(IDENTIFIER);
        match(KW_to);
        Token destName = match(IDENTIFIER);
        return new StatementWrite(first, scrName, destName);
    }

    private StatementWhile statementWhile() throws SyntaxException {
        Token first = t;
        consume();
        match(LPAREN);
        Expression exp = expression();
        match(RPAREN);
        Block bk = block();
        return new StatementWhile(first, exp, bk);
    }

    private StatementIf statementIf() throws SyntaxException {
        Token first = t;
        consume();
        match(LPAREN);
        Expression exp = expression();
        match(RPAREN);
        Block bk = block();
        return new StatementIf(first, exp, bk);
    }

    private StatementShow statementShow() throws SyntaxException {
        Token first = t;
        consume();
        Expression exp = expression();
        return new StatementShow(first, exp);
    }

    private StatementSleep statementSleep() throws SyntaxException {
        Token first = t;
        consume();
        Expression exp = expression();
        return new StatementSleep(first, exp);
    }

    private StatementAssign statementAssignment() throws SyntaxException {
        Token first = t;
        LHS lhs = lhs();
        match(OP_ASSIGN);
        Expression exp = expression();
        return new StatementAssign(first, lhs, exp);
    }

    private LHS lhs() throws SyntaxException {
        Token first = t;
        Token tmp;
        Token color;
        PixelSelector px;

        if (isKind(IDENTIFIER)) {
            tmp = consume();
            px = pixelselector2();
            if (px == null) return new LHSIdent(first, tmp);
            else return new LHSPixel(first, tmp, px);
        } else if (isKind(Color)) {
            color = consume();
            match(LPAREN);
            tmp = match(IDENTIFIER);
            px = pixelselector1();
            match(RPAREN);
            return new LHSSample(first, tmp, px, color);
        } else {
            throw new SyntaxException(t, "Syntax Error :: lhs error :: Token : " + t);
        }
    }

    private PixelSelector pixelselector1() throws SyntaxException { //pixelselector true
        Token first = t;
        if (isKind(LSQUARE)) {
            consume();
            Expression ex = expression();
            match(COMMA);
            Expression ey = expression();
            match(RSQUARE);
            return new PixelSelector(first, ex, ey);
        } else {
            throw new SyntaxException(t, "Syntax Error :: lhs error :: Token : " + t);
        }
    }

    private PixelSelector pixelselector2() throws SyntaxException {
        Token first = t;
        if (isKind(LSQUARE)) {
            consume();
            Expression ex = expression();
            match(COMMA);
            Expression ey = expression();
            match(RSQUARE);
            return new PixelSelector(first, ex, ey);
        }
        return null;
    }


    private Expression orexpression() throws SyntaxException {
        Token first = t;
        Expression e0 = andexpression();

        while (isKind(OP_OR)) {
            Token op = consume();
            Expression e1 = andexpression();
            e0 = new ExpressionBinary(first, e0, op, e1);
        }
        return e0;
    }

    private Expression andexpression() throws SyntaxException {
        Token first = t;
        Expression e0 = eqexpression();
        while (isKind(OP_AND)) {
            Token op = consume();
            Expression e1 = eqexpression();
            e0 = new ExpressionBinary(first, e0, op, e1);
        }
        return e0;
    }

    private Expression eqexpression() throws SyntaxException {
        Token first = t;
        Expression e0 = relexpression();
        while (isKind(OP_EQ) | isKind(OP_NEQ)) {
            Token op = consume();
            Expression e1 = relexpression();
            e0 = new ExpressionBinary(first, e0, op, e1);
        }
        return e0;
    }

    private Expression relexpression() throws SyntaxException {
        Token first = t;
        Expression e0 = addexpression();
        while (isKind(OP_LT) | isKind(OP_GT) | isKind(OP_LE) | isKind(OP_GE)) {
            Token op = consume();
            Expression e1 = addexpression();
            e0 = new ExpressionBinary(first, e0, op, e1);
        }
        return e0;
    }

    private Expression addexpression() throws SyntaxException {
        Token first = t;
        Expression e0 = multexpression();
        while (isKind(OP_PLUS) | isKind(OP_MINUS)) {
            Token op = consume();
            Expression e1 = multexpression();
            e0 = new ExpressionBinary(first, e0, op, e1);
        }
        return e0;
    }

    private Expression multexpression() throws SyntaxException {
        Token first = t;
        Expression e0 = powexpression();
        while (isKind(OP_TIMES) | isKind(OP_DIV) | isKind(OP_MOD)) {
            Token op = consume();
            Expression e1 = powexpression();
            e0 = new ExpressionBinary(first, e0, op, e1);
        }
        return e0;
    }

    private Expression powexpression() throws SyntaxException {
        Token first = t;
        Expression e0 = unaryexpression();
        if (isKind(OP_POWER)) {
            Token op = consume();
            Expression e1 = powexpression();
            e0 = new ExpressionBinary(first, e0, op, e1);
        }
        return e0;
    }

    private Expression unaryexpression() throws SyntaxException {
        Token first = t;
        Token op;
        Expression exp;

        if (isKind(OP_PLUS)) {
            op = consume();
            exp = unaryexpression();
            return new ExpressionUnary(first, op, exp);
        } else if (isKind(OP_MINUS)) {
            op = consume();
            exp = unaryexpression();
            return new ExpressionUnary(first, op, exp);
        } else {
            return unaryExpressionNotPlusMinus();
        }
    }

    private Expression unaryExpressionNotPlusMinus() throws SyntaxException {
        Token first = t;
        Token op;
        Expression exp;
        if (isKind(OP_EXCLAMATION)) {
            op = consume();
            exp = unaryexpression();
            return new ExpressionUnary(first, op, exp);
        } else {
            return primary();
        }
    }

    private Expression primary() throws SyntaxException {
        Token first = t;
        Token val;
        if (isKind(INTEGER_LITERAL)) {
            val = consume();
            return new ExpressionIntegerLiteral(first, val);
        } else if (isKind(BOOLEAN_LITERAL)) {
            val = consume();
            return new ExpressionBooleanLiteral(first, val);
        } else if (isKind(FLOAT_LITERAL)) {
            val = consume();
            return new ExpressionFloatLiteral(first, val);
        } else if (isKind(LPAREN)) {
            consume();
            Expression exp = expression();
            match(RPAREN);
            return exp;
        } else if (isKind(IDENTIFIER)) {
            val = consume();
            PixelSelector px = pixelselector2();
            if (px == null) return new ExpressionIdent(first, val);
            else return new ExpressionPixel(first, val, px);
        } else if (isKind(FunctionName)) {
            return functionApplication();
        } else if (isKind(PredefinedName)) {
            val = consume();
            return new ExpressionPredefinedName(first, val);
        } else if (isKind(LPIXEL)) {
            return pixelconstructor();
        } else {
            throw new SyntaxException(t, "Syntax Error :: primary error :: Token : " + t);
        }
    }

    private Expression pixelconstructor() throws SyntaxException {
        Token first = t;
        consume();
        Expression e0 = expression();
        match(COMMA);
        Expression e1 = expression();
        match(COMMA);
        Expression e2 = expression();
        match(COMMA);
        Expression e3 = expression();
        match(RPIXEL);
        return new ExpressionPixelConstructor(first, e0, e1, e2, e3);
    }

    private Expression functionApplication() throws SyntaxException {
        Token first = t;
        if (isKind(FunctionName)) {
            Token funcName = consume();
            if (isKind(LPAREN)) {
                consume();
                Expression exp = expression();
                match(RPAREN);
                return new ExpressionFunctionAppWithExpressionArg(first, funcName, exp);
            } else if (isKind(LSQUARE)) {
                consume();
                Expression e0 = expression();
                match(COMMA);
                Expression e1 = expression();
                match(RSQUARE);
                return new ExpressionFunctionAppWithPixel(first, funcName, e0, e1);
            } else {
                throw new SyntaxException(t, "Syntax Error :: functionApplication inside loop error :: Token : " + t);
            }
        } else {
            throw new SyntaxException(t, "Syntax Error :: functionApplication error :: Token : " + t);
        }

    }

    private boolean isKind(Kind kind) {
        return t.kind == kind;
    }

    private boolean isKind(Kind... kinds) {
        for (Kind k : kinds) {
            if (k == t.kind)
                return true;
        }
        return false;
    }

    private Token match(Kind kind) throws SyntaxException {
        Token tmp = t;
        if (isKind(kind)) {
            consume();
            return tmp;
        }
        throw new SyntaxException(t, "Syntax Error :: match :: Kind : " + kind + " Token : " + t);
    }

    private Token consume() throws SyntaxException {
        Token tmp = t;
        if (isKind(EOF)) {
            throw new SyntaxException(t, "Syntax Error :: consume :: Token : " + t);
            // Note that EOF should be matched by the matchEOF method which is called only
            // in parse().
            // Anywhere else is an error. */
        }
        t = scanner.nextToken();
        return tmp;
    }

    private Token matchEOF() throws SyntaxException {
        if (isKind(EOF)) {
            return t;
        } else
            throw new SyntaxException(t, "Syntax Error :: matchEOF :: Token : " + t);
    }

    @SuppressWarnings("serial")
    static class SyntaxException extends Exception {
        Token t;

        SyntaxException(Token t, String message) {
            super(message);
            this.t = t;
        }
    }

}
