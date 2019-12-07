package component;

/**
 *  Corresponding to return statement (e.g. return 1)
 */
public class ReturnAST implements AST {
    private AST expr;

    public AST getExpr() {
        return expr;
    }

    public void setExpr(AST expr) {
        this.expr = expr;
    }
}
