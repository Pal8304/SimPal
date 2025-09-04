package simpal.errors;

import simpal.token.Token;

public class SimPalRuntimeError extends RuntimeException {
    public final Token token;

    public SimPalRuntimeError(Token token, String message) {
        super(message);
        this.token = token;
    }
}
