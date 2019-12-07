package component;

import entity.Token;

/**
 *  Corresponding to entire program structure (e.g. PROGRAM demo { ... })
 */
public class ProgramAST implements AST {
    private AST varNode;
    private AST blockNode;
    private Token token;

    public AST getVarNode() {
        return varNode;
    }

    public void setVarNode(AST varNode) {
        this.varNode = varNode;
    }

    public AST getBlockNode() {
        return blockNode;
    }

    public void setBlockNode(AST blockNode) {
        this.blockNode = blockNode;
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }
}
