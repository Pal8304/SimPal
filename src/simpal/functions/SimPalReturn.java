package simpal.functions;

public class SimPalReturn extends RuntimeException{
    public final Object value;

    public SimPalReturn(Object value){
        super(null, null, false, false);
        this.value = value;
    }
}
