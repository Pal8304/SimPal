package simpal.interpreter;

import simpal.SimPal;
import simpal.errors.DivideByZeroError;
import simpal.errors.IOError;
import simpal.errors.SimPalRuntimeError;
import simpal.functions.SimPalCallable;
import simpal.functions.SimPalFunction;
import simpal.functions.SimPalReturn;
import simpal.lang.Expression;
import simpal.lang.Statement;
import simpal.token.Token;
import simpal.token.TokenType;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Interpreter implements Expression.Visitor<Object>, Statement.Visitor<Void> {

    final Environment globals = new Environment();
    private Environment environment = globals;
    private final Map<Expression, Integer> locals = new HashMap<>();
    public String outputFilePth;

    public Interpreter() {
        globals.define("clock", new SimPalCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return (double) System.currentTimeMillis() / 1000;
            }

            @Override
            public String toString() {
                return "<native fn>";
            }
        });
    }

    @Override
    public Object visitAssignExpression(Expression.Assign expression) {
        Object value = evaluateExpression(expression.value);

        Integer distance = locals.get(expression);
        if (distance != null) {
            environment.assignAt(distance, expression.name, value);
        } else {
            globals.assign(expression.name, value);
        }

        return value;
    }

    @Override
    public Object visitBinaryExpression(Expression.Binary expression) throws SimPalRuntimeError {
        Object leftExpression = evaluateExpression(expression.leftExpression);
        Object rightExpression = evaluateExpression(expression.rightExpression);
        Token operator = expression.operator;
        switch (operator.tokenType) {
            case GREATER:
                checkNumberOperands(operator, leftExpression, rightExpression);
                return (double) leftExpression > (double) rightExpression;
            case GREATER_EQUAL:
                checkNumberOperands(operator, leftExpression, rightExpression);
                return (double) leftExpression >= (double) rightExpression;
            case LESS:
                checkNumberOperands(operator, leftExpression, rightExpression);
                return (double) leftExpression < (double) rightExpression;
            case LESS_EQUAL:
                checkNumberOperands(operator, leftExpression, rightExpression);
                return (double) leftExpression <= (double) rightExpression;
            case MINUS:
                checkNumberOperands(operator, leftExpression, rightExpression);
                return (double) leftExpression - (double) rightExpression;
            case SLASH:
                checkNumberOperands(operator, leftExpression, rightExpression);
                if ((double) rightExpression == 0) {
                    throw new DivideByZeroError("Division by zero is not possible.");
                }
                return (double) leftExpression / (double) rightExpression;
            case MODULO:
                checkNumberOperands(operator, leftExpression, rightExpression);
                if ((double) rightExpression == 0) {
                    throw new DivideByZeroError("Remainder when any number is divided by zero is not possible.");
                }
                return (double)leftExpression % (double)rightExpression;
            case STAR:
                checkNumberOperands(operator, leftExpression, rightExpression);
                return (double) leftExpression * (double) rightExpression;
            case BANG_EQUAL:
                checkNumberOperands(operator, leftExpression, rightExpression);
                return !isEqual(leftExpression, rightExpression);
            case EQUAL_EQUAL:
                checkNumberOperands(operator, leftExpression, rightExpression);
                return isEqual(leftExpression, rightExpression);
            case PLUS:
                if (leftExpression instanceof Double && rightExpression instanceof Double) {
                    return (double) leftExpression + (double) rightExpression;
                }

                if (leftExpression instanceof String && rightExpression instanceof String) {
                    return (String) leftExpression + (String) rightExpression;
                }
                throw new SimPalRuntimeError(operator,
                        "Operands must be two numbers or two strings.");
        }

        return null;
    }

    @Override
    public Object visitLiteralExpression(Expression.Literal expression) {
        return expression.value;
    }

    @Override
    public Object visitLogicalExpression(Expression.Logical expression) {
        Object leftExpression = evaluateExpression(expression.leftExpression);
        Token operator = expression.operator;
        if (operator.tokenType == TokenType.OR) {
            if (isTruthy(leftExpression)) return leftExpression;
        }

        if (operator.tokenType == TokenType.AND) {
            if (!isTruthy(leftExpression)) return leftExpression;
        }
        return evaluateExpression(expression.rightExpression);
    }

    @Override
    public Object visitGroupingExpression(Expression.Grouping expression) {
        return evaluateExpression(expression.expression);
    }

    @Override
    public Object visitUnaryExpression(Expression.Unary expression) throws SimPalRuntimeError {
        Object rightExpression = evaluateExpression(expression.rightExpression);
        Token operator = expression.operator;
        switch (operator.tokenType) {
            case BANG:
                return !isTruthy(rightExpression);
            case MINUS:
                checkNumberOperand(operator, rightExpression);
                return -(double) rightExpression;
        }

        return null;
    }

    @Override
    public Object visitCallExpression(Expression.Call expression) {
        Object callee = evaluateExpression(expression.callee);

        List<Object> arguments = new ArrayList<>();
        for (Expression argument : expression.arguments) {
            arguments.add(evaluateExpression(argument));
        }

        if (!(callee instanceof SimPalCallable)) {
            throw new SimPalRuntimeError(expression.paren,
                    "Can only call functions and classes.");
        }

        SimPalCallable function = (SimPalCallable) callee;

        if (arguments.size() != function.arity()) {
            throw new SimPalRuntimeError(expression.paren, "Expected " +
                    function.arity() + " arguments but got " +
                    arguments.size() + ".");
        }

        return function.call(this, arguments);
    }

    @Override
    public Object visitVariableExpression(Expression.Variable expression) {
        return lookUpVariable(expression.name, expression);
    }

    @Override
    public Void visitCompleteExpressionStatement(Statement.CompleteExpression statement) {
        evaluateExpression(statement.expression);
        return null;
    }

    @Override
    public Void visitFunctionStatement(Statement.Function statement) {
        SimPalFunction function = new SimPalFunction(statement, environment);
        environment.define(statement.name.lexeme, function);
        return null;
    }

    @Override
    public Void visitIfStatement(Statement.If statement) {
        if (isTruthy(evaluateExpression(statement.condition))) {
            execute(statement.thenBranch);
        } else if (statement.elseBranch != null) {
            execute(statement.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitPrintStatement(Statement.Print statement) {
        Object value = evaluateExpression(statement.expression);
        if(outputFilePth == null || outputFilePth.isBlank()) {
            System.out.println(stringify(value));
        }
        else {
            try {
                PrintWriter printWriter = new PrintWriter(new FileWriter(outputFilePth, true));
                printWriter.println(stringify(value));
                printWriter.close();
            } catch (Exception e ) {
                System.err.println("Exception: " + e.getMessage());
                throw new IOError("Error while print in output file: " + outputFilePth, e);
            }
        }
        return null;
    }

    @Override
    public Void visitReturnStatement(Statement.Return statement) {
        Object value = null;
        if (statement.value != null) value = evaluateExpression(statement.value);

        throw new SimPalReturn(value);
    }

    @Override
    public Void visitVarStatement(Statement.Var statement) {
        Object value = null;
        if (statement.initializer != null) {
            value = evaluateExpression(statement.initializer);
        }
        environment.define(statement.name.lexeme, value);
        return null;
    }

    @Override
    public Void visitWhileStatement(Statement.While statement) {
        while (isTruthy(evaluateExpression(statement.condition))) {
            execute(statement.body);
        }
        return null;
    }

    @Override
    public Void visitBlockStatement(Statement.Block statement) {
        executeBlock(statement.statements, new Environment(environment));
        return null;
    }

    public void executeBlock(List<Statement> statements, Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;

            for (Statement statement : statements) {
                execute(statement);
            }
        } finally {
            this.environment = previous;
        }
    }

    public void interpret(List<Statement> statements) {
        try {
            for (Statement statement : statements) {
                execute(statement);
            }
        } catch (SimPalRuntimeError simPalRuntimeError) {
            SimPal.runtimeError(simPalRuntimeError);
        }
    }

    void resolve(Expression expression, int depth) {
        locals.put(expression, depth);
    }

    private Object lookUpVariable(Token name, Expression expression) {
        Integer distance = locals.get(expression);
        if (distance != null) {
            return environment.getAt(distance, name.lexeme);
        } else {
            return globals.get(name);
        }
    }

    private void execute(Statement statement) {
        statement.accept(this);
    }

    private String stringify(Object object) {
        if (object == null) return "nil";
        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }
        return object.toString();
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new SimPalRuntimeError(operator, "Operand must be a number.");
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) return;
        throw new SimPalRuntimeError(operator, "Operands must be numbers.");
    }

    private Object evaluateExpression(Expression expression) {
        return expression.accept(this);
    }

    private boolean isTruthy(Object object) {
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean) object;
        return true;
    }

    private boolean isEqual(Object leftExpression, Object rightExpression) {
        if (leftExpression == null && rightExpression == null) return true;
        if (leftExpression == null) return false;
        return leftExpression.equals(rightExpression);
    }

}
