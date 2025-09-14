package simpal.errors;

public class IOError extends RuntimeException {
    public IOError(String message) {
        super(message);
    }
    public IOError(String message, Exception e) {
        super(message, e);
    }
}
