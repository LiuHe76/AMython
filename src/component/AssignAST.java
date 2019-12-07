package component;

/**
 *   Corresponds to Assign statement (e.g. x = 5)
 */
public class AssignAST implements AST {
    private AST varNode;
    private AST exprNode;

    public AST getVarNode() {
        return varNode;
    }

    public void setVarNode(AST varNode) {
        this.varNode = varNode;
    }

    public AST getExprNode() {
        return exprNode;
    }

    public void setExprNode(AST exprNode) {
        this.exprNode = exprNode;
    }
}
