package exception;


/**
 *  self-define TypeError class
 */
public class TypeError extends Exception {
    public TypeError() {}

    public TypeError(String info) {
        super(info);
    }
}
