package SimPal;

import SimPal.Errors.DivideByZeroError;
import SimPal.Errors.SimPalRuntimeError;

import java.util.List;

public class Interpreter implements Expression.Visitor<Object>, Statement.Visitor<Void> {

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
                    throw new DivideByZeroError();
                }
                return (double) leftExpression / (double) rightExpression;
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
    public Object visitGroupingExpression(Expression.Grouping expression) {
        return evaluateExpression(expression);
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
    public Void visitCompleteExpressionStatement(Statement.CompleteExpression statement) {
        evaluateExpression(statement.expression);
        return null;
    }

    @Override
    public Void visitPrintStatement(Statement.Print statement) {
        Object value = evaluateExpression(statement.expression);
        System.out.println(stringify(value));
        return null;
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

    private void execute(Statement statement){
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
