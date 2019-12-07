package component;

/**
 *  Corresponding to if statement currently strictly following if ( ... ) { ... } else { ... } pattern
 */
public class IfStmtAST implements AST {
    private AST predicate;
    private AST if_body;
    private AST else_body;

    public AST getPredicate() {
        return predicate;
    }

    public void setPredicate(AST predicate) {
        this.predicate = predicate;
    }

    public AST getIf_body() {
        return if_body;
    }

    public void setIf_body(AST if_body) {
        this.if_body = if_body;
    }

    public AST getElse_body() {
        return else_body;
    }

    public void setElse_body(AST else_body) {
        this.else_body = else_body;
    }
}
