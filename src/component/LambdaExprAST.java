package component;

import entity.Token;

import java.util.List;

/**
 *  Corresponding to lambda expression following the format: lambda ( ... ) : { ... }
 */
public class LambdaExprAST implements AST {
    private String name;
    private Token token;
    private List<AST> params;
    private AST body;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public List<AST> getParams() {
        return params;
    }

    public void setParams(List<AST> params) {
        this.params = params;
    }

    public AST getBody() {
        return body;
    }

    public void setBody(AST body) {
        this.body = body;
    }
}
