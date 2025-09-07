package simpal.parser;

import simpal.lang.Expression;
import simpal.SimPal;
import simpal.lang.Statement;
import simpal.token.Token;
import simpal.token.TokenType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static simpal.token.TokenType.*;

public class Parser {

    private static class ParseError extends RuntimeException {
    }

    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    /**
     * Parses program following the grammar, Program → Declaration* EOF
     *
     * @return List of statements
     */
    public List<Statement> parse() {
        List<Statement> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(declaration());
        }
        return statements;
    }

    /**
     * Following the grammar declaration → funcDeclaration | varDeclaration | Statement
     * Uses synchronization in case of any parsing errors
     *
     * @return Statement can be function, variable declaration or just a statement
     */
    private Statement declaration() {
        try {
            if (matchAnyTokenType(FUN)) {
                return function("function");
            }
            if (matchAnyTokenType(VAR)) {
                return varDeclaration();
            }
            return statement();
        } catch (ParseError parseError) {
            synchronize();
            return null;
        }
    }

    /**
     * Parses the entire function, following the format: fun <function_name> ( comma_separated parameters ) { function body }
     *
     * @param kind The type of function
     * @return Statement of function
     */
    private Statement.Function function(String kind) {
        Token functionName = consume(IDENTIFIER, "Expect " + kind + " name.");
        consume(LEFT_PAREN, "Expect '(' after " + kind + " name.");

        List<Token> parameters = new ArrayList<>();

        if (!checkTokenType(RIGHT_PAREN)) {
            do {
                if (parameters.size() >= 255) {
                    error(peekCurrentToken(), "Can't have more than 255 parameters.");
                }

                parameters.add(
                        consume(IDENTIFIER, "Expect parameter name."));
            } while (matchAnyTokenType(COMMA));
        }
        consume(RIGHT_PAREN, "Expect ')' after parameters.");

        consume(LEFT_BRACE, "Expect '{' before " + kind + " body.");
        List<Statement> body = block();
        return new Statement.Function(functionName, parameters, body);
    }

    /**
     * Parses variable declaration, following the format var <variable_name> ? ( "=" variable value ;
     *
     * @return Statement parsed variable declaration
     */
    private Statement varDeclaration() {
        Token name = consume(IDENTIFIER, "Expect variable name.");
        Expression initializer = null;
        if (matchAnyTokenType(EQUAL)) {
            initializer = expression();
        }

        consume(SEMICOLON, "Expect ';' after variable declaration.");
        return new Statement.Var(name, initializer);
    }


    /**
     * Follows the grammar statement → completeExpression | forStatement | ifStatement | printStatement | returnStatement | whileStatement | block
     * By dividing then based on tokenType
     *
     * @return Specific statement based on match
     */
    private Statement statement() {
        if (matchAnyTokenType(FOR)) return forStatement();
        if (matchAnyTokenType(IF)) return ifStatement();
        if (matchAnyTokenType(PRINT)) return printStatement();
        if (matchAnyTokenType(RETURN)) return returnStatement();
        if (matchAnyTokenType(WHILE)) return whileStatement();
        if (matchAnyTokenType(LEFT_BRACE)) return new Statement.Block(block());
        return expressionStatement();
    }

    /*
    ToDO: Add Support for break and continue statements
     */

    /**
     * Statement that represents "for-loop", it follows template of "for-loop" i.e. for("some initializer" ; "condition"; "increment of some kind ") { body of loop }
     * It uses while loop implementation in order to loop, thus combines while loop with initializer, condition, increment and body
     *
     * @return Statement representing "for-loop"
     */
    private Statement forStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'for'.");

        Statement initializer;
        if (matchAnyTokenType(SEMICOLON)) {
            initializer = null;
        } else if (matchAnyTokenType(VAR)) {
            initializer = varDeclaration();
        } else {
            initializer = expressionStatement();
        }

        Expression condition = null;
        if (!checkTokenType(SEMICOLON)) {
            condition = expression();
        }
        consume(SEMICOLON, "Expect ';' after loop condition.");

        Expression increment = null;
        if (!checkTokenType(RIGHT_PAREN)) {
            increment = expression();
        }
        consume(RIGHT_PAREN, "Expect ')' after for clauses.");

        Statement body = statement();

        if (increment != null) {
            body = new Statement.Block(Arrays.asList(
                    body, new Statement.CompleteExpression(increment)
            ));
        }

        if (condition == null) condition = new Expression.Literal(true);
        body = new Statement.While(condition, body);

        if (initializer != null) {
            body = new Statement.Block(Arrays.asList(initializer, body));
        }

        return body;
    }


    private Statement ifStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'if'.");
        Expression condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after if condition.");

        Statement thenBranch = statement();
        Statement elseBranch = null;

        if (matchAnyTokenType(ELSE)) {
            elseBranch = statement();
        }

        return new Statement.If(condition, thenBranch, elseBranch);
    }

    private Statement printStatement() {
        Expression value = expression();
        consume(SEMICOLON, "Expect ';' after value.");
        return new Statement.Print(value);
    }

    private Statement returnStatement() {
        Token keyword = getPreviousToken();
        Expression expression = null;
        if (!checkTokenType(SEMICOLON)) {
            expression = expression();
        }

        consume(SEMICOLON, "Expect ';' after return value.");
        return new Statement.Return(keyword, expression);
    }

    private Statement whileStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'while'.");
        Expression condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after condition.");
        Statement body = statement();

        return new Statement.While(condition, body);
    }

    private List<Statement> block() {
        List<Statement> statements = new ArrayList<>();
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
        Expression expression = orExpression();

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

    private Expression orExpression() {
        Expression expression = andExpression();

        while (matchAnyTokenType(OR)) {
            Token operator = getPreviousToken();
            Expression rightExpression = andExpression();
            expression = new Expression.Logical(expression, operator, rightExpression);
        }

        return expression;
    }

    private Expression andExpression() {
        Expression expression = equality();

        while (matchAnyTokenType(AND)) {
            Token operator = getPreviousToken();
            Expression rightExpression = equality();
            expression = new Expression.Logical(expression, operator, rightExpression);
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
        return call();
    }

    private Expression call() {
        Expression expression = primary();

        while (true) {
            if (matchAnyTokenType(LEFT_PAREN)) {
                expression = finishCall(expression);
            } else {
                break;
            }
        }
        return expression;
    }

    private Expression finishCall(Expression callee) {
        List<Expression> arguments = new ArrayList<>();
        if (!checkTokenType(RIGHT_PAREN)) {
            do {
                if (arguments.size() >= 255) {
                    error(peekCurrentToken(), "Can't have more than 255 arguments.");
                }
                arguments.add(expression());
            } while (matchAnyTokenType(COMMA));
        }

        Token paren = consume(RIGHT_PAREN, "Expect ')' after arguments.");

        return new Expression.Call(callee, paren, arguments);
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

    /**
     * Provides the current token that is being pointed to
     *
     * @return Token current token
     */
    private Token peekCurrentToken() {
        return tokens.get(current);
    }

    /**
     * Checks if the End of File (EOF) has been reached
     *
     * @return boolean if EOF is reached
     */
    private boolean isAtEnd() {
        return peekCurrentToken().tokenType == EOF;
    }
}