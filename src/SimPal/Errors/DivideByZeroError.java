package SimPal.Errors;

public class DivideByZeroError extends RuntimeException {
    public DivideByZeroError(){
        super("Unexpected Division By Zero");
    }
    public DivideByZeroError(String message){
        super(message);
    }
}
