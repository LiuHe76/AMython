package component;

import entity.Token;

/**
 *  Corresponding to operation with two operands such as "+", "*", "==", "<"
 */
public class DoubleOpAST implements AST {
    private Token operator;
    private AST leftOperand, rightOperand;

    public Token getOperator() {
        return operator;
    }

    public void setOperator(Token operator) {
        this.operator = operator;
    }

    public AST getLeftOperand() {
        return leftOperand;
    }

    public void setLeftOperand(AST leftOperand) {
        this.leftOperand = leftOperand;
    }

    public AST getRightOperand() {
        return rightOperand;
    }

    public void setRightOperand(AST rightOperand) {
        this.rightOperand = rightOperand;
    }
}
