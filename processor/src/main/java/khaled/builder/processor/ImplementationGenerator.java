package khaled.builder.processor;

import java.io.IOException;
import java.io.Writer;
import java.util.Set;
import java.util.stream.Collectors;

import static khaled.builder.processor.GenerationInfo.INDENTATION;

/**
 *
 * @author khaled
 */
public class ImplementationGenerator {

    private final Writer writer;
    private final String prototypeName;
    private final String implName;
    private final String builderName;
    private final Set<PropertyMethod> properties;

    public ImplementationGenerator(GenerationInfo generationInfo, Writer writer) {
        this.writer = writer;
        this.implName = generationInfo.implName();
        this.prototypeName = generationInfo.prototypeName();
        this.builderName = generationInfo.builderName();
        this.properties = generationInfo.properties();

    }

    void generateImplementation() throws IOException {
        final String implDeclaration = INDENTATION + "class " + this.implName + " implements "
                + prototypeName + "{\n";
        writer.write(implDeclaration);

        writer.write("\n\n" + INDENTATION.repeat(2) + "//properties\n");
        String fieldDeclarationPrefix = INDENTATION.repeat(2) + "private final ";
        for (var property : properties) {
            String type = property.type().className();

            String name = property.name();

            String fieldDeclaration = fieldDeclarationPrefix + " " + type + " " + name + ";\n";

            writer.write(fieldDeclaration);
        }

        writer.write("\n\n" + INDENTATION.repeat(2) + "//accessor\n");
        String accessorDeclarationPrefix = INDENTATION.repeat(2) + "@Override\n" + INDENTATION.repeat(2) + "public ";
        for (PropertyMethod property : this.properties) {
            String type = property.type().className();

            String name = property.name();

            String accessorDeclaration = accessorDeclarationPrefix + type + " " + name + "(){\n";

            writer.write(accessorDeclaration);

            String accessorBody = INDENTATION.repeat(3) + "return this." + name + ";\n";

            writer.write(accessorBody);

            writer.write(INDENTATION.repeat(2) + "}\n\n");
        }

        String constructorDeclarationPrefix = INDENTATION.repeat(2) + implName + "(final "
                + builderName + " builder){\n";
        writer.write(constructorDeclarationPrefix);
        for (PropertyMethod property : this.properties) {
            String name = property.name();

            String assignement = INDENTATION.repeat(3) + "this." + name + " = builder." + name + "();\n";

            writer.write(assignement);
        }

        writer.write(INDENTATION.repeat(2) + "}\n\n");

        generateEquals();

        generateHashCode();

        generateToString();

        writer.write("    }\n\n");
    }

    private void generateEquals() throws IOException {
        final String equalsDeclarationPrefix = INDENTATION.repeat(2) + "@Override\n" + INDENTATION.repeat(2) + " public boolean equals(final Object obj){\n";
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
        if (properties.isEmpty()) {
            returnStatement = INDENTATION.repeat(3) + "return true;\n";
            writer.write(returnStatement);
        } else {
            var joinExpression = "\n" + INDENTATION.repeat(4) + "&& ";
            var booleanExpression = properties.stream()
                    .map(it -> toEqualityExpression(it, "other"))
                    .collect(Collectors.joining(joinExpression));
            returnStatement = INDENTATION.repeat(3) + "return " + booleanExpression + ";\n";
            writer.write(returnStatement);
        }
        writer.write(INDENTATION.repeat(2) + "}\n\n");

    }

    private String toEqualityExpression(final PropertyMethod property, String other) {
        var type = property.type();
        var name = property.name();
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

        var params = properties.stream()
                .map(PropertyMethod::name)
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

        String propertiesValues = properties.stream()
                .map(PropertyMethod::name)
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
