package component;

/**
 *  Corresponding to int basic data type in designed language (i.e. 1)
 */
public class NumericIntAST implements AST {
    private int val;

    public NumericIntAST(int val) {
        this.val = val;
    }

    public int getVal() {
        return val;
    }

    public void setVal(int val) {
        this.val = val;
    }
}
