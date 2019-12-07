package component;

/**
 *  Corresponding to boolean basic data type in designed language (i.e. True/False)
 */
public class BooleanType implements AST {
    private boolean val;

    public BooleanType(boolean val) {
        this.val = val;
    }

    public boolean getVal() {
        return val;
    }

    public void setVal(boolean val) {
        this.val = val;
    }
}
