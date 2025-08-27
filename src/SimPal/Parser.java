package SimPal;

import java.util.List;

import static SimPal.TokenType.*;

/* Initial Grammar:

expression     → literal
               | unary
               | binary
               | grouping ;

literal        → NUMBER | STRING | "true" | "false" | "nil" ;
grouping       → "(" expression ")" ;
unary          → ( "-" | "!" ) expression ;
binary         → expression operator expression ;
operator       → "==" | "!=" | "<" | "<=" | ">" | ">="
               | "+"  | "-"  | "*" | "/" ;

*/

/* New Complete Grammar

expression     → equality ;
equality       → comparison ( ( "!=" | "==" ) comparison )* ;
comparison     → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
term           → factor ( ( "-" | "+" ) factor )* ;
factor         → unary ( ( "/" | "*" ) unary )* ;
unary          → ( "!" | "-" ) unary
               | primary ;
primary        → NUMBER | STRING | "true" | "false" | "nil"
               | "(" expression ")" ;
 */

public class Parser {

    private static class ParseError extends RuntimeException {
    }

    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    Expression parse() {
        try {
            return expression();
        } catch (ParseError parseError) {
            return null;
        }
    }

    private Expression expression() {
        return equality();
    }

    private Expression equality() {
        Expression expression = comparison();

        while (matchAnyTokenType(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = getPreviousToken();
            Expression rightExpression = comparison();
            expression = new Expression.Binary(expression, operator, rightExpression);
        }

        return expression;
    }

    private Expression comparison() {
        Expression expression = term();

        while (matchAnyTokenType(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = getPreviousToken();
            Expression rightExpression = term();
            expression = new Expression.Binary(expression, operator, rightExpression);
        }

        return expression;
    }

    private Expression term() {
        Expression expression = factor();
        while (matchAnyTokenType(PLUS, MINUS)) {
            Token operator = getPreviousToken();
            Expression rightExpression = factor();
            expression = new Expression.Binary(expression, operator, rightExpression);
        }
        return expression;
    }

    private Expression factor() {
        Expression expression = unary();
        while (matchAnyTokenType(SLASH, STAR)) {
            Token operator = getPreviousToken();
            Expression rightExpression = unary();
            expression = new Expression.Binary(expression, operator, rightExpression);
        }
        return expression;
    }

    private Expression unary() {
        if (matchAnyTokenType(BANG, MINUS)) {
            Token operator = getPreviousToken();
            Expression rightExpression = unary();
            return new Expression.Unary(operator, rightExpression);
        }
        return primary();
    }

    private Expression primary() {
        if (matchAnyTokenType(FALSE)) {
            return new Expression.Literal(false);
        }
        if (matchAnyTokenType(TRUE)) {
            return new Expression.Literal(true);
        }
        if (matchAnyTokenType(NIL)) {
            return new Expression.Literal(null);
        }

        if (matchAnyTokenType(NUMBER, STRING)) {
            return new Expression.Literal(getPreviousToken().literal);
        }

        if (matchAnyTokenType(LEFT_PAREN)) {
            Expression expression = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expression.Grouping(expression);
        }

        throw error(peekCurrentToken(), "Expect expression.");
    }


    private boolean matchAnyTokenType(TokenType... tokenTypes) {
        for (TokenType tokenType : tokenTypes) {
            if (checkTokenType(tokenType)) {
                moveToNextToken();
                return true;
            }
        }
        return false;
    }

    private Token consume(TokenType type, String message) {
        if (checkTokenType(type)) return moveToNextToken();
        throw error(peekCurrentToken(), message);
    }

    private boolean checkTokenType(TokenType tokenType) {
        if (isAtEnd()) return false;
        return peekCurrentToken().tokenType == tokenType;
    }


    private Token moveToNextToken() {
        if (!isAtEnd()) current++;
        return getPreviousToken();
    }

    private Token getPreviousToken() {
        return tokens.get(current - 1);
    }

    private ParseError error(Token token, String message) {
        SimPal.error(token, message);
        return new ParseError();
    }

    private void synchronize() {
        moveToNextToken();

        while (!isAtEnd()) {
            if (getPreviousToken().tokenType == SEMICOLON) return;
            switch (peekCurrentToken().tokenType) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }

            moveToNextToken();
        }

    }

    private Token peekCurrentToken() {
        return tokens.get(current);
    }

    private boolean isAtEnd() {
        return peekCurrentToken().tokenType == EOF;
    }
}