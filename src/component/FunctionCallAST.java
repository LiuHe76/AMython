package component;

import java.util.List;

/**
 *  Corresponding to function call statement/expression (e.g. func(2, x))
 */
public class FunctionCallAST implements AST {
    private AST varNode;
    private List<AST> exprNode;

    public AST getVarNode() {
        return varNode;
    }

    public void setVarNode(AST varNode) {
        this.varNode = varNode;
    }

    public List<AST> getExprNode() {
        return exprNode;
    }

    public void setExprNode(List<AST> exprNode) {
        this.exprNode = exprNode;
    }
}
