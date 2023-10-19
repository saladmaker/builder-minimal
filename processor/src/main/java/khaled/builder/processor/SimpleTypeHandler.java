package khaled.builder.processor;

import io.helidon.common.types.TypeName;
import io.helidon.common.types.TypedElementInfo;
import java.io.IOException;
import java.io.Writer;
import static khaled.builder.processor.GenerationInfo.INDENTATION;
import static khaled.builder.processor.GenerationInfo.CHECKER_SUFFIX;

/**
 *
 * @author khaled
 */
public record SimpleTypeHandler(String name, TypeName type, TypedElementInfo tei) implements TypeHandler {

    private static final String CHECKER_FORMAT = """
                                                    %1$sprivate boolean %2$s%3$s = false;\n
                                                    """;

    private static final String VALIDATION_STATEMENT = """
                                                           %1$sif(!%2$s){
                                                           %3$s throw new IllegalStateException(\"%4$s\");\n
                                                           %1$s}
                                                           
                                                           """;

    @Override
    public void generateBuilderChecker(Writer writer, int indentationLevel) throws IOException {
        if (!hasDefaultValue()) {
            String declaration = CHECKER_FORMAT.formatted(
                    INDENTATION.repeat(indentationLevel),
                    name,
                    CHECKER_SUFFIX);
            writer.write(declaration);
        }
    }

    @Override
    public void generateBuilderValidateStatement(Writer writer, int indentationLevel) throws IOException {
        if (!hasDefaultValue()) {
            String checkerName = name + "Checker";
            String message = "property " + name + " must be initialized before building";
            String statement = VALIDATION_STATEMENT.formatted(INDENTATION.repeat(indentationLevel),
                    checkerName,
                    INDENTATION.repeat(indentationLevel + 1),
                    message
            );
            writer.write(statement);
        }
        writer.write("");
    }

    @Override
    public void generateBuilderMutators(Writer writer, String builderName, int indentationLevel) throws IOException {
        String mutatorDeclarationPrefix = INDENTATION.repeat(indentationLevel) + "public ";
        String name = name();
        String builderType = builderName;
        String paramType = type().className();

        String mutatorDeclaration = mutatorDeclarationPrefix + builderType + " " + name + "(final "
                + paramType + " " + name + "){\n";

        writer.write(mutatorDeclaration);

        String mutatorBody;
        if (type().primitive()) {

            mutatorBody = INDENTATION.repeat(indentationLevel + 1) + "this." + name + " = " + name + ";\n";

        } else {

            String requireNonNull = "Objects.requireNonNull(" + name + ");";
            mutatorBody = INDENTATION.repeat(indentationLevel + 1) + "this." + name + " = " + requireNonNull + "\n";

        }
        writer.write(mutatorBody);

        // set checker
        String checkerName = name + "Checker";
        if (!hasDefaultValue()) {

            String setChecker = INDENTATION.repeat(3) + "this." + checkerName + " = true;\n";
            writer.write(setChecker);

        }

        String returSelf = INDENTATION.repeat(indentationLevel + 1) + "return self();\n";
        writer.write(returSelf);
        writer.write(INDENTATION.repeat(indentationLevel) + "}\n\n");

    }
}
