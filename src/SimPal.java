import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class SimPal {

    static boolean hadError = false;

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: SimPal [script]");
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    private static void runFile(String filePath) throws IOException {
        byte[] fileBytes = Files.readAllBytes(Paths.get(filePath));
    }

    private static void runPrompt() throws IOException {
        InputStreamReader inputStreamReader = new InputStreamReader(System.in);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        for (; ; ) {
            System.out.print("> ");
            String line = bufferedReader.readLine();
            if (line == null) break;
            run(line);
        }
    }

    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = Collections.emptyList(); // ToDo: add scanner.ScanTokens here

        for (Token token : tokens) {
            System.out.println(token);
        }
    }

    static void error(int lineNumber, String errorMessage) {
        report(lineNumber, "", errorMessage);
    }

    private static void report(int lineNumber, String where, String errorMessage){
        System.err.println(
                "[line " + lineNumber + "] Error" + where + ": " + errorMessage);
        hadError = true;
    }
}