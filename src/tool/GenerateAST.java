package tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class GenerateAST {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: generate_ast <output directory>");
            System.exit(65);
        }
        String outputDirectory = args[0];
        defineAST(outputDirectory, "Expression", Arrays.asList(
                "Assign   : Token name, Expression value",
                "Binary   : Expression leftExpression, Token operator, Expression rightExpression",
                "Call     : Expression callee, Token paren, List<Expression> arguments",
                "Grouping : Expression expression",
                "Literal  : Object value",
                "Logical  : Expression leftExpression, Token operator, Expression rightExpression",
                "Unary    : Token operator, Expression rightExpression",
                "Variable : Token name"
        ));

        defineAST(outputDirectory, "Statement", Arrays.asList(
                "Block      : List<Statement> statements",
                "CompleteExpression : Expression expression",
                "Function   : Token name, List<Token> params," +
                        " List<Statement> body",
                "If         : Expression condition, Statement thenBranch," +
                        " Statement elseBranch",
                "Print      : Expression expression",
                "Return     : Token keyword, Expression value",
                "Var        : Token name, Expression initializer",
                "While      : Expression condition, Statement body"
        ));
    }

    private static void defineAST(String outputDirectory, String baseName, List<String> types) throws IOException {
        String path = outputDirectory + "/" + baseName + ".java";
        PrintWriter writer = new PrintWriter(path, StandardCharsets.UTF_8);

        writer.println("package SimPal;");
        writer.println();
        writer.println("import java.util.List;");
        writer.println();
        writer.println("public abstract class " + baseName + " {");

        defineVisitor(writer, baseName, types);

        for (String type : types) {
            String className = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim();
            defineType(writer, baseName, className, fields);
        }

        writer.println();
        writer.println("  public abstract <R> R accept(Visitor<R> visitor);");

        writer.println("}");
        writer.close();
    }

    private static void defineType(PrintWriter writer, String baseName, String className, String fieldList) {
        writer.println("  public static class " + className + " extends " +
                baseName + " {");

        // Constructor.
        writer.println("    public " + className + "(" + fieldList + ") {");

        // Store parameters in fields.
        String[] fields = fieldList.split(", ");
        for (String field : fields) {
            String name = field.split(" ")[1];
            writer.println("      this." + name + " = " + name + ";");
        }

        writer.println("    }");

        // Visitor pattern.
        writer.println();
        writer.println("    @Override");
        writer.println("    public <R> R accept(Visitor<R> visitor) {");
        writer.println("      return visitor.visit" +
                className + baseName + "(this);");
        writer.println("    }");

        // Fields.
        writer.println();
        for (String field : fields) {
            writer.println("    public final " + field + ";");
        }

        writer.println("  }");
    }

    private static void defineVisitor(PrintWriter writer, String baseName, List<String> types) {
        writer.println("  public interface Visitor<R> {");

        for (String type : types) {
            String typeName = type.split(":")[0].trim();
            writer.println("    R visit" + typeName + baseName + "(" +
                    typeName + " " + baseName.toLowerCase() + ");");
        }

        writer.println("  }");
    }
}
