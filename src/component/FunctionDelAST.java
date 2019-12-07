package component;

import java.util.List;

/**
 *  Corresponding to function declaration statement (e.g. def f() { ... })
 */
public class FunctionDelAST implements AST {
    private AST varNode;
    private List<AST> paramNode;
    private AST blockNode;

    public AST getVarNode() {
        return varNode;
    }

    public void setVarNode(AST varNode) {
        this.varNode = varNode;
    }

    public List<AST> getParamNode() {
        return paramNode;
    }

    public void setParamNode(List<AST> paramNode) {
        this.paramNode = paramNode;
    }

    public AST getBlockNode() {
        return blockNode;
    }

    public void setBlockNode(AST blockNode) {
        this.blockNode = blockNode;
    }
}
