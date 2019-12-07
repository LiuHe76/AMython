package exception;


/**
 *  self-define ZeroDivisionError class
 */
public class ZeroDivisionError extends Exception {

    public ZeroDivisionError() {}

    public ZeroDivisionError(String info) {
        super(info);
    }
}
