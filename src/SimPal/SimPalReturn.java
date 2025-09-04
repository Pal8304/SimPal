package SimPal;

public class SimPalReturn extends RuntimeException{
    final Object value;

    SimPalReturn(Object value){
        super(null, null, false, false);
        this.value = value;
    }
}
