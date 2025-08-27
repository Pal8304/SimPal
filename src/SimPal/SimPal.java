package SimPal;

import SimPal.Errors.SimPalRuntimeError;

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

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: SimPal.SimPal [script]");
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    private static void runFile(String filePath) throws IOException {
        byte[] fileBytes = Files.readAllBytes(Paths.get(filePath));
        run(new String(fileBytes, Charset.defaultCharset()));

        if (hadError) System.exit(65);
        if (hadRuntimeError) System.exit(70);
    }

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

    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        Parser parser = new Parser(tokens);
        Expression expression = parser.parse();

        if (hadError) return;

        interpreter.interpret(expression);
    }

    // ToDo: Add an abstraction like errorHandler or errorReporter
    static void error(int lineNumber, String errorMessage) {
        report(lineNumber, "", errorMessage);
    }

    private static void report(int lineNumber, String where, String errorMessage) {
        System.err.println(
                "[line " + lineNumber + "] Error" + where + ": " + errorMessage);
        hadError = true;
    }

    static void error(Token token, String message) {
        if (token.tokenType == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }

    static void runtimeError(SimPalRuntimeError simPalRuntimeError) {
        System.err.println(simPalRuntimeError.getMessage() + "\n[line " + simPalRuntimeError.token.line + "]");
        hadRuntimeError = true;
    }
}