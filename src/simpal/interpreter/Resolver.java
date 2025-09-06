package simpal.interpreter;

import simpal.SimPal;
import simpal.lang.Expression;
import simpal.lang.Statement;
import simpal.token.Token;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * This class visits very node of the syntax tree and kind of performs semantic analysis
 */
public class Resolver implements Expression.Visitor<Void>, Statement.Visitor<Void> {
    private final Interpreter interpreter;
    private final Stack<Map<String, Boolean>> scopes = new Stack<>();
    private FunctionType currentFunction = FunctionType.NONE;

    public Resolver(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    private enum FunctionType {
        NONE,
        FUNCTION
    }

    @Override
    public Void visitVariableExpression(Expression.Variable expression) {
        if (!scopes.isEmpty() && scopes.peek().get(expression.name.lexeme) == Boolean.FALSE) {
            SimPal.error(expression.name,
                    "Can't read local variable in its own initializer.");
        }
        resolveLocal(expression, expression.name);
        return null;
    }

    @Override
    public Void visitBinaryExpression(Expression.Binary expression) {
        resolve(expression.leftExpression);
        resolve(expression.rightExpression);

        return null;
    }

    @Override
    public Void visitCallExpression(Expression.Call expression) {
        resolve(expression.callee);

        for (Expression argument : expression.arguments) {
            resolve(argument);
        }

        return null;
    }

    @Override
    public Void visitGroupingExpression(Expression.Grouping expression) {
        resolve(expression.expression);

        return null;
    }

    @Override
    public Void visitLiteralExpression(Expression.Literal expression) {
        return null;
    }

    @Override
    public Void visitLogicalExpression(Expression.Logical expression) {
        resolve(expression.leftExpression);
        resolve(expression.rightExpression);

        return null;
    }

    @Override
    public Void visitUnaryExpression(Expression.Unary expression) {
        resolve(expression.rightExpression);
        return null;
    }

    @Override
    public Void visitIfStatement(Statement.If statement) {
        resolve(statement.condition);
        resolve(statement.thenBranch);

        if (statement.elseBranch != null) resolve(statement.elseBranch);

        return null;
    }

    @Override
    public Void visitPrintStatement(Statement.Print statement) {
        resolve(statement.expression);
        return null;
    }

    @Override
    public Void visitWhileStatement(Statement.While statement) {
        resolve(statement.condition);
        resolve(statement.body);

        return null;
    }

    @Override
    public Void visitReturnStatement(Statement.Return statement) {
        if (currentFunction == FunctionType.NONE) {
            SimPal.error(statement.keyword, "Can't return from top-level code.");
        }

        if (statement.value != null) {
            resolve(statement.value);
        }
        return null;
    }

    @Override
    public Void visitAssignExpression(Expression.Assign expression) {
        resolve(expression.value);
        resolveLocal(expression, expression.name);
        return null;
    }

    @Override
    public Void visitCompleteExpressionStatement(Statement.CompleteExpression statement) {
        resolve(statement.expression);
        return null;
    }

    @Override
    public Void visitBlockStatement(Statement.Block statement) {
        beginScope();
        resolve(statement.statements);
        endScope();
        return null;
    }

    @Override
    public Void visitVarStatement(Statement.Var statement) {
        declare(statement.name);
        if (statement.initializer != null) {
            resolve(statement.initializer);
        }
        define(statement.name);
        return null;
    }

    @Override
    public Void visitFunctionStatement(Statement.Function statement) {
        declare(statement.name);
        define(statement.name);

        resolveFunction(statement, FunctionType.FUNCTION);
        return null;
    }

    public void resolve(List<Statement> statements) {
        for (Statement statement : statements) {
            resolve(statement);
        }
    }

    /**
     * Adds variable name to the innermost scope, false in the map means that variable initializer is resolved or not
     *
     * @param name Token that is being declared
     */
    private void declare(Token name) {
        if (scopes.isEmpty()) return;
        Map<String, Boolean> scope = scopes.peek();

        if (scope.containsKey(name.lexeme)) {
            SimPal.error(name,
                    "Already a variable with this name in this scope.");
        }

        scope.put(name.lexeme, false);
    }

    /**
     * Marks the variable in the innermost scope as true i.e. "resolved"
     *
     * @param name Token name that is to be defined
     */
    private void define(Token name) {
        if (scopes.isEmpty()) return;
        scopes.peek().put(name.lexeme, true);
    }

    private void resolveLocal(Expression expression, Token name) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).containsKey(name.lexeme)) {
                interpreter.resolve(expression, scopes.size() - 1 - i);
                return;
            }
        }
    }

    private void beginScope() {
        scopes.push(new HashMap<>());
    }

    private void resolve(Statement statement) {
        statement.accept(this);
    }

    private void resolve(Expression expression) {
        expression.accept(this);
    }

    private void resolveFunction(Statement.Function function, FunctionType type) {
        FunctionType enclosingFunction = currentFunction;
        currentFunction = type;

        beginScope();
        for (Token param : function.params) {
            declare(param);
            define(param);
        }
        resolve(function.body);
        endScope();
        currentFunction = enclosingFunction;
    }

    private void endScope() {
        scopes.pop();
    }
}
