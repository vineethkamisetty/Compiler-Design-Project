package cop5556sp18.AST;

/**
 * This code is for the class project in COP5556 Programming Language Principles
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

import cop5556sp18.Scanner.Token;
import cop5556sp18.Types;

public abstract class ASTNode {

    final public Token firstToken;

    private Types.Type typeName;
    private Declaration dec;

    public ASTNode(Token firstToken) {
        super();
        this.firstToken = firstToken;
    }

    public Declaration getDec() {
        return dec;
    }

    public void setDec(Declaration dec) {
        this.dec = dec;
    }

    public Types.Type getTypeName() {
        return typeName;
    }

    public void setTypeName(Types.Type typeName) {
        this.typeName = typeName;
    }

    public abstract Object visit(ASTVisitor v, Object arg) throws Exception;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((firstToken == null) ? 0 : firstToken.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ASTNode other = (ASTNode) obj;
        if (firstToken == null) {
            return other.firstToken == null;
        } else return firstToken.equals(other.firstToken);
    }

    @Override
    public String toString() {
        return "";
    }

}
