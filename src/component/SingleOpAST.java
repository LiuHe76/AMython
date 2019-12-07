package component;

import entity.Token;

/**
 *  Corresponding to operation with one operand such as "-2", "+3"
 */
public class SingleOpAST implements AST {
    private Token operator;
    private AST operand;

    public Token getOperator() {
        return operator;
    }

    public void setOperator(Token operator) {
        this.operator = operator;
    }

    public AST getOperand() {
        return operand;
    }

    public void setOperand(AST operand) {
        this.operand = operand;
    }
}
