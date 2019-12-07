package component;

/**
 *  Corresponding to double basic data type in designed language (i.e. 3.14)
 */
public class NumericDoubleAST implements AST {
    private double val;

    public NumericDoubleAST(double val) {
        this.val = val;
    }

    public double getVal() {
        return val;
    }

    public void setVal(double val) {
        this.val = val;
    }
}
