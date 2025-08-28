package SimPal;

abstract class Statement {
  interface Visitor<R> {
    R visitCompleteExpressionStatement(CompleteExpression statement);
    R visitPrintStatement(Print statement);
  }
  static class CompleteExpression extends Statement {
    CompleteExpression(Expression expression) {
      this.expression = expression;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitCompleteExpressionStatement(this);
    }

    final Expression expression;
  }
  static class Print extends Statement {
    Print(Expression expression) {
      this.expression = expression;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitPrintStatement(this);
    }

    final Expression expression;
  }

  abstract <R> R accept(Visitor<R> visitor);
}
