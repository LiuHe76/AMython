package exception;

/**
 *  self-define SyntaxError class
 */
public class SyntaxError extends Exception {

    public SyntaxError() {}

    public SyntaxError(String info) {
        super(info);
    }
}
