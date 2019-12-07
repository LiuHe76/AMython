package component;

import entity.Token;

/**
 *  Corresponding to variable element in language such as "x", "y2"
 */
public class VarAST implements AST {
    private Token token;
    private String name;

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
