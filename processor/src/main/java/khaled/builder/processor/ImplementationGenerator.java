package khaled.builder.processor;

import io.helidon.common.types.TypeName;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;
import java.util.stream.Collectors;

import static khaled.builder.processor.GenerationInfo.INDENTATION;

/**
 *
 * @author khaled
 */
public final class ImplementationGenerator implements Generator {

    private final Writer writer;
    private final String prototypeName;
    private final String implName;
    private final String builderName;
    private final Set<TypeHandler> typeHandlers;

    public ImplementationGenerator(GenerationInfo generationInfo, Writer writer) {
        this.writer = writer;
        this.implName = generationInfo.implName();
        this.prototypeName = generationInfo.prototypeName();
        this.builderName = generationInfo.builderName();
        this.typeHandlers = generationInfo.typeHandlers();

    }

    @Override
    public void generate() throws IOException {
        final String implDeclaration = INDENTATION + "class " + this.implName + " implements "
                + prototypeName + "{\n";
        writer.write(implDeclaration);

        generateProperties();

        generateAccessors();

        generateConstructor();

        generateEquals();

        generateHashCode();

        generateToString();

        writer.write("    }\n\n");
    }

    private void generateProperties() throws IOException {
        writer.write("\n\n" + INDENTATION.repeat(2) + "//properties\n");
        for (var property : typeHandlers) {
            property.generateImplementationProperty(writer, 2);
        }
    }

    private void generateAccessors() throws IOException {
        writer.write("\n\n" + INDENTATION.repeat(2) + "//accessor\n");
        String override = INDENTATION.repeat(2) + "@Override\n";
        for (var typeHandler : typeHandlers) {
            writer.write(override);
            typeHandler.generateAccessors(writer, 2, this);
        }
    }

    private void generateConstructor() throws IOException {
        String constructorDeclarationPrefix = INDENTATION.repeat(2) + implName + "(final "
                + builderName + " builder){\n";
        writer.write(constructorDeclarationPrefix);
        for (var typeHandler : typeHandlers) {
            TypeName type = typeHandler.type();
            String name = typeHandler.name();
            String assigement;
            String assignementPrefix = INDENTATION.repeat(3) + "this." + name + " = ";
            if (typeHandler.collectionBased()) {
                String copyOfAssignement = type.className() + ".copyOf(builder." + name + "());\n";
                assigement = assignementPrefix + copyOfAssignement;
            } else {
                assigement = assignementPrefix + "builder." + name + "();\n";
            }
            writer.write(assigement);

        }

        writer.write(INDENTATION.repeat(2) + "}\n\n");
    }

    private void generateEquals() throws IOException {
        final String equalsDeclarationPrefix = INDENTATION.repeat(2) + "@Override\n" + INDENTATION.repeat(2) + "public boolean equals(final Object obj){\n";
        writer.write(equalsDeclarationPrefix);
        String identityCheckFormat = """
                                   
                                   %1$sif(this == obj){
                                   %2$sreturn true;
                                   %1$s}
                                   
                                   """.formatted(INDENTATION.repeat(3),
                INDENTATION.repeat(4));
        writer.write(identityCheckFormat);

        String typeCheckFormat = """
                                %1$sif (!(obj instanceof %2$s other)) {
                                %3$sreturn false;
                                %1$s}
                                 
                                 """.formatted(INDENTATION.repeat(3),
                prototypeName,
                INDENTATION.repeat(4));
        writer.write(typeCheckFormat);

        String returnStatement;
        if (typeHandlers.isEmpty()) {
            returnStatement = INDENTATION.repeat(3) + "return true;\n";
            writer.write(returnStatement);
        } else {
            var joinExpression = "\n" + INDENTATION.repeat(4) + "&& ";
            var booleanExpression = typeHandlers.stream()
                    .map(it -> toEqualityExpression(it, "other"))
                    .collect(Collectors.joining(joinExpression));
            returnStatement = INDENTATION.repeat(3) + "return " + booleanExpression + ";\n";
            writer.write(returnStatement);
        }
        writer.write(INDENTATION.repeat(2) + "}\n\n");

    }

    private String toEqualityExpression(final TypeHandler typeHandler, String other) {
        var type = typeHandler.type();
        var name = typeHandler.name();
        String exp;
        if (type.primitive()) {
            exp = name + " == " + other + "." + name + "()";
        } else {
            exp = "Objects.equals(" + name + ", " + other + "." + name + "()" + ")";
        }
        return exp;
    }

    private void generateHashCode() throws IOException {
        String hashCodeDeclarationFormat = """
                                     %1$s@Override
                                     %1$spublic int hashCode(){
                                     %2$sreturn Objects.hash(%3$s);
                                     %1$s}
                                     
                                     """;

        var params = typeHandlers.stream()
                .map(TypeHandler::name)
                .collect(Collectors.joining(", "));
        String hashCodeDeclaration = hashCodeDeclarationFormat.formatted(
                INDENTATION.repeat(2),
                INDENTATION.repeat(3),
                params);
        writer.write(hashCodeDeclaration);

    }

    private void generateToString() throws IOException {
        String toStringDeclarationFormat = """
                                     %1$s@Override
                                     %1$spublic String toString(){
                                     %2$sreturn \"%3$s{\"
                                     %4$s
                                     %5$s+ \"}\";
                                     %1$s}
                                     
                                     """;

        String propertiesValues = typeHandlers.stream()
                .map(TypeHandler::name)
                .map(it -> INDENTATION.repeat(5) + "+ " + "\"" + it + "=\" + " + it)
                .collect(Collectors.joining(" + \", \"\n"));
        String toStringDeclaration = toStringDeclarationFormat
                .formatted(INDENTATION.repeat(2),
                        INDENTATION.repeat(3),
                        implName,
                        propertiesValues,
                        INDENTATION.repeat(5));
        writer.write(toStringDeclaration);
    }
}
