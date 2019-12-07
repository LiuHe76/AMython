package component;

import java.util.List;

/**
 *  Corresponding to statement block following such structure: { ... }
 */
public class BlockAST implements AST {
    private List<AST> stmtnode;

    public List<AST> getStmtnode() {
        return stmtnode;
    }

    public void setStmtnode(List<AST> stmtnode) {
        this.stmtnode = stmtnode;
    }
}
