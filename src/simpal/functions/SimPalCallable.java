package simpal.functions;

import simpal.interpreter.Interpreter;

import java.util.List;

public interface SimPalCallable {
    int arity();

    Object call(Interpreter interpreter, List<Object> arguments);
}
