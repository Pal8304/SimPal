package simpal.lang;

import simpal.token.Token;

import java.util.List;

public abstract class Expression {
    public interface Visitor<R> {
        R visitAssignExpression(Assign expression);

        R visitBinaryExpression(Binary expression);

        R visitCallExpression(Call expression);

        R visitGroupingExpression(Grouping expression);

        R visitLiteralExpression(Literal expression);

        R visitLogicalExpression(Logical expression);

        R visitUnaryExpression(Unary expression);

        R visitVariableExpression(Variable expression);
    }

    public static class Assign extends Expression {
        public Assign(Token name, Expression value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitAssignExpression(this);
        }

        public final Token name;
        public final Expression value;
    }

    public static class Binary extends Expression {
        public Binary(Expression leftExpression, Token operator, Expression rightExpression) {
            this.leftExpression = leftExpression;
            this.operator = operator;
            this.rightExpression = rightExpression;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBinaryExpression(this);
        }

        public final Expression leftExpression;
        public final Token operator;
        public final Expression rightExpression;
    }

    public static class Call extends Expression {
        public Call(Expression callee, Token paren, List<Expression> arguments) {
            this.callee = callee;
            this.paren = paren;
            this.arguments = arguments;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitCallExpression(this);
        }

        public final Expression callee;
        public final Token paren;
        public final List<Expression> arguments;
    }

    public static class Grouping extends Expression {
        public Grouping(Expression expression) {
            this.expression = expression;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitGroupingExpression(this);
        }

        public final Expression expression;
    }

    public static class Literal extends Expression {
        public Literal(Object value) {
            this.value = value;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLiteralExpression(this);
        }

        public final Object value;
    }

    public static class Logical extends Expression {
        public Logical(Expression leftExpression, Token operator, Expression rightExpression) {
            this.leftExpression = leftExpression;
            this.operator = operator;
            this.rightExpression = rightExpression;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLogicalExpression(this);
        }

        public final Expression leftExpression;
        public final Token operator;
        public final Expression rightExpression;
    }

    public static class Unary extends Expression {
        public Unary(Token operator, Expression rightExpression) {
            this.operator = operator;
            this.rightExpression = rightExpression;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitUnaryExpression(this);
        }

        public final Token operator;
        public final Expression rightExpression;
    }

    public static class Variable extends Expression {
        public Variable(Token name) {
            this.name = name;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitVariableExpression(this);
        }

        public final Token name;
    }

    public abstract <R> R accept(Visitor<R> visitor);
}
