package simpal.scanner;

import simpal.SimPal;
import simpal.token.Token;
import simpal.token.TokenType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static simpal.token.TokenType.*;

/*
ToDo:
1. Handle Multiline comments
2. Refactor the code
 */

public class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and", AND);
        keywords.put("class", CLASS);
        keywords.put("else", ELSE);
        keywords.put("false", FALSE);
        keywords.put("for", FOR);
        keywords.put("fun", FUN);
        keywords.put("if", IF);
        keywords.put("nil", NIL);
        keywords.put("or", OR);
        keywords.put("print", PRINT);
        keywords.put("return", RETURN);
        keywords.put("super", SUPER);
        keywords.put("this", THIS);
        keywords.put("true", TRUE);
        keywords.put("var", VAR);
        keywords.put("while", WHILE);
        keywords.put("int", INT);
        keywords.put("double", DOUBLE);
    }

    public Scanner(String source) {
        this.source = source;
    }

    /**
     * Tokenizes the entire string of source code and finally adds EOF at the end
     *
     * @return list of tokens
     */
    public List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }
        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    /**
     * Checks our current pointer has reached end of the provided source code.
     *
     * @return true if end of source code is reached else false
     */
    private boolean isAtEnd() {
        return current >= source.length();
    }

    /**
     * Scans token by matching each character with various operators and alphanumerics
     */
    private void scanToken() {
        char nextCharacter = moveToNextCharacter();
        switch (nextCharacter) {
            case '(':
                addToken(LEFT_PAREN);
                break;
            case ')':
                addToken(RIGHT_PAREN);
                break;
            case '{':
                addToken(LEFT_BRACE);
                break;
            case '}':
                addToken(RIGHT_BRACE);
                break;
            case ',':
                addToken(COMMA);
                break;
            case '.':
                addToken(DOT);
                break;
            case '-':
                addToken(MINUS);
                break;
            case '+':
                addToken(PLUS);
                break;
            case ';':
                addToken(SEMICOLON);
                break;
            case '*':
                addToken(STAR);
                break;
            case '%':
                addToken(MODULO);
                break;
            case '!':
                addToken(match('=') ? BANG_EQUAL : BANG);
                break;
            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '<':
                addToken(match('=') ? LESS_EQUAL : LESS);
                break;
            case '>':
                addToken(match('=') ? GREATER_EQUAL : GREATER);
                break;
            case '/':
                if (match('/')) {
                    // A comment goes until the end of the line.
                    while (peek() != '\n' && !isAtEnd()) moveToNextCharacter();
                } else {
                    addToken(SLASH);
                }
                break;
            case ' ':
            case '\r':
            case '\t':
                // Ignore whitespace.
                break;

            case '\n':
                line++;
                break;

            case '"':
                string();
                break;
            default:
                if (isDigit(nextCharacter)) {
                    number();
                } else if (isAlpha(nextCharacter)) {
                    identifier();
                } else {
                    SimPal.error(line, "Unexpected character.");
                }
                break;
        }
    }

    /**
     * Gets the current character that is being pointed to and increments the current pointer
     *
     * @return character from the source code which the current pointer points
     */
    private char moveToNextCharacter() {
        return source.charAt(current++);
    }

    /**
     * Adds new token to the token list ( token is considered a string between start and current pointers )
     *
     * @param tokenType Provides info about token type
     * @param literal   The literal value of the token
     */
    private void addToken(TokenType tokenType, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(tokenType, text, literal, line));
    }

    /**
     * Adds new token list with empty literal
     *
     * @param tokenType Provides info about token type
     */
    private void addToken(TokenType tokenType) {
        addToken(tokenType, null);
    }

    /**
     * Checks if the current character in the source matches with expected character
     *
     * @param expected The character that is expected
     * @return True if the current character matches with the expected character
     */
    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;

        current++;
        return true;
    }

    /**
     * Gets the current character that is being pointed to by current pointer, and in case of end of source, returns '\0'
     *
     * @return Character that is being pointed in the source code
     */
    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    /**
     * Gets just next character to current character that is being pointed to
     *
     * @return Next character from the source code
     */
    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    // ToDo: var a = """a""" works in cpp and not in this language, later figure out why, and fix it if it's a mistake

    /**
     * Evaluates strings for the scanner, any number of character between "" and adds it to the token list
     */
    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++;
            moveToNextCharacter();
        }

        if (isAtEnd()) {
            SimPal.error(line, "Unterminated string.");
            return;
        }

        moveToNextCharacter();

        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    /**
     * Evaluates numbers, uses regex like [0-9]*.?[0-9]* and adds it to the token list
     * That is first takes all digits before decimal point and then if decimal point is found takes all numbers after it
     */
    private void number() {
        while (isDigit(peek())) moveToNextCharacter();

        if (peek() == '.' && isDigit(peekNext())) {
            moveToNextCharacter();

            while (isDigit(peek())) moveToNextCharacter();
        }

        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));

    }

    /**
     * Checks if character is a digit
     *
     * @param ch Character to be checked
     * @return True if it is a digit else false
     */
    private boolean isDigit(char ch) {
        return (ch >= '0' && ch <= '9');
    }

    /**
     * Adds identifier in the token list
     */
    private void identifier() {
        while (isAlphaNumeric(peek())) moveToNextCharacter();

        String text = source.substring(start, current);
        TokenType tokenType = keywords.get(text);
        if (tokenType == null) tokenType = IDENTIFIER;

        addToken(tokenType);
    }

    /**
     * Checks if a given character is alphanumeric (alphabet or digit or underscore)
     *
     * @param ch Character to be checked
     * @return True if character is alphanumeric
     */
    private boolean isAlphaNumeric(char ch) {
        return isAlpha(ch) || isDigit(ch);
    }

    /**
     * Checks if a character is an alphabet or underscore
     *
     * @param ch Character to be checked
     * @return true in case of alphabet
     */
    private boolean isAlpha(char ch) {
        return (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || (ch == '_');
    }
}
