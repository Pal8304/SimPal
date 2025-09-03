package SimPal;

import java.util.ArrayList;
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

/* New Grammar

expression     → assignment ;

assignment     → IDENTIFIER "=" assignment
               | equality ;

equality       → comparison ( ( "!=" | "==" ) comparison )* ;

comparison     → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;

term           → factor ( ( "-" | "+" ) factor )* ;

factor         → unary ( ( "/" | "*" ) unary )* ;

unary          → ( "!" | "-" ) unary
               | primary ;

primary        → "true" | "false" | "nil"
               | NUMBER | STRING
               | "(" expression ")"
               | IDENTIFIER ;
 */

/* Added Newer Grammar ( for handling statements )

program        → declaration* EOF ;

declaration    → varDeclaration
               | statement ;

varDecl        → "var" IDENTIFIER ( "=" expression )? ";" ;

statement      → completeExpression
               | printStatement
               | block;

block          → "{" declaration* "}" ;

completeExpression       → expression ";" ;

printStatement      → "print" expression ";" ;

 */


public class Parser {

    private static class ParseError extends RuntimeException {
    }

    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    List<Statement> parse() {
        List<Statement> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(declaration());
        }
        return statements;
    }

    private Statement declaration() {
        try {
            if (matchAnyTokenType(VAR)) {
                return varDeclaration();
            }
            return statement();
        } catch (ParseError parseError) {
            synchronize();
            return null;
        }
    }

    private Statement varDeclaration() {
        Token name = consume(IDENTIFIER, "Expect variable name.");
        Expression initializer = null;
        if (matchAnyTokenType(EQUAL)) {
            initializer = expression();
        }

        consume(SEMICOLON, "Expect ';' after variable declaration.");
        return new Statement.Var(name, initializer);
    }

    private Statement statement() {
        if (matchAnyTokenType(PRINT)) return printStatement();
        if (matchAnyTokenType(LEFT_BRACE)) return new Statement.Block(block());
        return expressionStatement();
    }

    private Statement printStatement() {
        Expression value = expression();
        consume(SEMICOLON, "Expect ';' after value.");
        return new Statement.Print(value);
    }

    private List<Statement> block() {
        List<Statement> statements = new ArrayList<Statement>();
        while (!isAtEnd() && !checkTokenType(RIGHT_BRACE)) {
            statements.add(declaration());
        }

        consume(RIGHT_BRACE, "Expect '}' after block.");
        return statements;
    }

    private Statement expressionStatement() {
        Expression expression = expression();
        consume(SEMICOLON, "Expect ';' after expression.");
        return new Statement.CompleteExpression(expression);
    }

    private Expression expression() {
        return assignment();
    }

    private Expression assignment() {
        Expression expression = equality();

        if (matchAnyTokenType(EQUAL)) {
            Token equals = getPreviousToken();
            Expression value = assignment();

            if (expression instanceof Expression.Variable) {
                Token name = ((Expression.Variable) expression).name;
                return new Expression.Assign(name, value);
            }
            // We report an error if the left-hand side isn’t a valid assignment target, but we don’t throw it because the parser isn’t in a confused state where we need to go into panic mode and synchronize.
            error(equals, "Invalid assignment target.");
        }

        return expression;
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

        if (matchAnyTokenType(IDENTIFIER)) {
            return new Expression.Variable(getPreviousToken());
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