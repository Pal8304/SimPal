package simpal;

import simpal.errors.SimPalRuntimeError;
import simpal.interpreter.Interpreter;
import simpal.lang.Statement;
import simpal.parser.Parser;
import simpal.scanner.Scanner;
import simpal.token.Token;
import simpal.token.TokenType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class SimPal {

    private static final Interpreter interpreter = new Interpreter();

    static boolean hadError = false;
    static boolean hadRuntimeError = false;

    /**
     * Main function that executes code for file ( if provided in params ) or from terminal input
     *
     * @param args (optional) file name to executed
     * @throws IOException if any input error occurs
     */
    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: SimPal.SimPal [script]");
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    /**
     * Runs the code using the given file path from file bytes attempts to execute  if file argument is given
     *
     * @param filePath relative path of the file which has code to be executed
     * @throws IOException if file is invalid or readAllBytes fails
     */
    private static void runFile(String filePath) throws IOException {
        byte[] fileBytes = Files.readAllBytes(Paths.get(filePath));
        run(new String(fileBytes, Charset.defaultCharset()));

        if (hadError) System.exit(65);
        if (hadRuntimeError) System.exit(70);
    }

    /**
     * Runs the code that is inputted in the terminal, executed when no file path is provided in the argument
     *
     * @throws IOException when input error occurs
     */
    private static void runPrompt() throws IOException {
        InputStreamReader inputStreamReader = new InputStreamReader(System.in);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        for (; ; ) {
            System.out.print("> ");
            String line = bufferedReader.readLine();
            if (line == null) break;
            run(line);
            hadError = false;
        }
    }

    /**
     * Runs the input code, first tokenizes it using {@link Scanner}, parses it using {@link Parser} and then interprets it using {@link Interpreter}
     *
     * @param source code that is to be executed, basically the source code
     */
    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        Parser parser = new Parser(tokens);
        List<Statement> statements = parser.parse();

        if (hadError) return;

        interpreter.interpret(statements);
    }

    // ToDo: Add an abstraction like errorHandler or errorReporter

    /**
     * Displays error details with line number
     *
     * @param lineNumber   line number on which error has occurred
     * @param errorMessage message that needs to be displayed
     */

    public static void error(int lineNumber, String errorMessage) {
        report(lineNumber, "", errorMessage);
    }

    /**
     * Displays error using token line and error message
     *
     * @param token   token at which error has occurred, provides line number
     * @param message error message to be displayed
     */
    public static void error(Token token, String message) {
        if (token.tokenType == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }

    /**
     * Prints on terminal error details
     *
     * @param lineNumber   line number of source code where error has occurred
     * @param where        location of error in the line ( like in the end or lexeme location )
     * @param errorMessage detailed message of error
     */
    private static void report(int lineNumber, String where, String errorMessage) {
        System.err.println(
                "[line " + lineNumber + "] Error" + where + ": " + errorMessage);
        hadError = true;
    }

    /**
     * Prints any runtime error that might have occurred during execution
     *
     * @param simPalRuntimeError any runtime error that might have occurred
     */
    public static void runtimeError(SimPalRuntimeError simPalRuntimeError) {
        System.err.println(simPalRuntimeError.getMessage() + "\n[line " + simPalRuntimeError.token.line + "]");
        hadRuntimeError = true;
    }
}