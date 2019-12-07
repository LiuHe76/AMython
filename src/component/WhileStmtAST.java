package component;

/**
 *  Corresponding to while statement (i.e. while ( ... ) { ... })
 */
public class WhileStmtAST implements AST {
    private AST predicate;
    private AST body;

    public AST getPredicate() {
        return predicate;
    }

    public void setPredicate(AST predicate) {
        this.predicate = predicate;
    }

    public AST getBody() {
        return body;
    }

    public void setBody(AST body) {
        this.body = body;
    }
}
