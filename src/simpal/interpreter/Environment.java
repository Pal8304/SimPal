package simpal.interpreter;

import simpal.errors.SimPalRuntimeError;
import simpal.token.Token;

import java.util.HashMap;
import java.util.Map;

public class Environment {

    final Environment enclosingEnvironment;

    public Environment() {
        enclosingEnvironment = null;
    }

    public Environment(Environment environment) {
        enclosingEnvironment = environment;
    }

    private final Map<String, Object> values = new HashMap<>();

    Object get(Token name) {
        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme);
        }

        // Not sure if this should be above
        if (enclosingEnvironment != null) {
            return enclosingEnvironment.get(name);
        }

        throw new SimPalRuntimeError(name,
                "Undefined variable '" + name.lexeme + "'.");
    }

     public void define(String name, Object value) {
        values.put(name, value);
    }

    void assign(Token name, Object value) {
        if (values.containsKey(name.lexeme)) {
            values.put(name.lexeme, value);
            return;
        }

        if (enclosingEnvironment != null) {
            enclosingEnvironment.assign(name, value);
            return;
        }

        throw new SimPalRuntimeError(name,
                "Undefined variable '" + name.lexeme + "'.");
    }
}
