package exception;

/**
 *  self-define NameError class
 */
public class NameError extends Exception {

    public NameError() {}

    public NameError(String info) {
        super(info);
    }
}
