package SimPal;

abstract class Expression {
    interface Visitor<R> {
        R visitBinaryExpression(Binary expression);

        R visitGroupingExpression(Grouping expression);

        R visitLiteralExpression(Literal expression);

        R visitUnaryExpression(Unary expression);
    }

    static class Binary extends Expression {
        Binary(Expression leftExpression, Token operator, Expression rightExpression) {
            this.leftExpression = leftExpression;
            this.operator = operator;
            this.rightExpression = rightExpression;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitBinaryExpression(this);
        }

        final Expression leftExpression;
        final Token operator;
        final Expression rightExpression;
    }

    static class Grouping extends Expression {
        Grouping(Expression expression) {
            this.expression = expression;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitGroupingExpression(this);
        }

        final Expression expression;
    }

    static class Literal extends Expression {
        Literal(Object value) {
            this.value = value;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitLiteralExpression(this);
        }

        final Object value;
    }

    static class Unary extends Expression {
        Unary(Token operator, Expression rightExpression) {
            this.operator = operator;
            this.rightExpression = rightExpression;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitUnaryExpression(this);
        }

        final Token operator;
        final Expression rightExpression;
    }

    abstract <R> R accept(Visitor<R> visitor);
}
