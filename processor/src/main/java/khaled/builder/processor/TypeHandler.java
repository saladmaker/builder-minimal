package khaled.builder.processor;

import java.io.IOException;
import java.io.Writer;
import static khaled.builder.processor.GenerationInfo.INDENTATION;
import static khaled.builder.processor.CollectionTypeHandler.Type.LIST;
import static khaled.builder.processor.CollectionTypeHandler.Type.SET;

/**
 *
 * @author khaled
 */
public sealed interface TypeHandler permits TypeHandler.DefaultTypeHandler, CollectionTypeHandler, OptionalTypeHandler {

    static final String PROPERTY_FORMAT = "%1$sprivate %2$s%3$s %4$s%5$s";
    static final String NO_INITIALIZATION = ";\n";
    static final String INITIALIZATION_FORMAT = " = %1$s;\n";

    static final String NONE = "";
    static final String FINAL = "final ";

    PropertyMethod property();

    default void generateAccessors(Writer writer, int indentationLevel, Generator generator) throws IOException {
        String accessorDeclarationPrefix = INDENTATION.repeat(indentationLevel) + "public ";

        String name = property().name();
        String accessorDeclaration = accessorDeclarationPrefix + type() + " " + name + "(){\n";
        writer.write(accessorDeclaration);
        String propertyName = "this." + name;
        String accessMechanism = accessMechanism(propertyName, generator);
        String accessorBody = INDENTATION.repeat(indentationLevel + 1) + "return " + accessMechanism + ";\n";
        writer.write(accessorBody);
        writer.write(INDENTATION.repeat(indentationLevel) + "}\n\n");
    }

    void generateBuilderMutators(Writer writer, String builderName, int indentationLevel) throws IOException;

    default void generateBuilderChecker(Writer writer, int indentationLevel) throws IOException {
    }

    default void generateBuilderValidateStatement(Writer writer, int indentationLevel) throws IOException {
    }

    default String initialValueLiteral() {
        String name = property().type().boxed().className();
        String defaultValue = property().defaultValue();
        /*
        using the compiler and the javac jvm process as means of validation
            ValueOf will validate the String
            single quotation with validate the char
         */
        return switch (name) {
            case null ->
                throw new IllegalStateException("null default value");

            case "String" ->
                "\"" + defaultValue + "\"";

            case "Boolean" ->
                Boolean.valueOf(defaultValue).toString();

            case "Byte" ->
                Byte.valueOf(defaultValue).toString();

            case "Character" ->
                "\'" + defaultValue + "\'";

            case "Double" ->
                Double.valueOf(defaultValue).toString();

            case "Float" ->
                Float.valueOf(defaultValue).toString();

            case "Integer" ->
                Integer.valueOf(defaultValue).toString();

            case "Long" ->
                Long.valueOf(defaultValue).toString();

            case "Short" ->
                Short.valueOf(defaultValue).toString();

            default ->
                throw new IllegalStateException("unkown type defaulted" + name);
        };

    }

    default void generateBuilderProperty(Writer writer, int indentationLevel) throws IOException {
        String modifier;
        String intialValueLiteral;

        switch (this) {

            case DefaultTypeHandler dd when null != dd.property().defaultValue() -> {
                modifier = NONE;
                intialValueLiteral = INITIALIZATION_FORMAT.formatted(initialValueLiteral());
            }
            case DefaultTypeHandler d -> {
                modifier = NONE;
                intialValueLiteral = NO_INITIALIZATION;
            }

            case CollectionTypeHandler collection -> {

                modifier = FINAL;
                intialValueLiteral = NO_INITIALIZATION;
                var collectionType = collection.collectionType();

                switch (collectionType) {
                    
                    case LIST -> {
                        intialValueLiteral = INITIALIZATION_FORMAT.formatted(LIST.initialization());
                    }
                    case SET ->{
                        intialValueLiteral = INITIALIZATION_FORMAT.formatted(SET.initialization());
                    }
                }
            }
            case OptionalTypeHandler o -> {
                modifier = NONE;
                intialValueLiteral = NO_INITIALIZATION;
            }

        }
        String declaration = PROPERTY_FORMAT.formatted(
                INDENTATION.repeat(indentationLevel),
                modifier,
                builderPropertyType(),
                property().name(),
                intialValueLiteral
        );
        writer.write(declaration);

    }

    default void generateImplementationProperty(Writer writer, int indentationLevel) throws IOException {
        String declaration = PROPERTY_FORMAT.formatted(
                INDENTATION.repeat(indentationLevel),
                FINAL,
                type(),
                property().name(),
                NO_INITIALIZATION
        );
        writer.write(declaration);
    }

    private String type() {
        return property().type().classNameWithTypes();
    }

    private String builderPropertyType() {
        return switch (this) {
            case OptionalTypeHandler o ->
                o.mutatorType();
            default ->
                type();
        };
    }

    private String accessMechanism(String propertyName, Generator generator) {
        if (this instanceof OptionalTypeHandler) {
            return switch (generator) {
                case ImplementationGenerator i -> propertyName;
                case BuilderGenerator b -> "Optional.ofNullable(%1$s)".formatted(propertyName);
                case PrototypeGenerator p -> throw new IllegalStateException("accessor not supported");
            };
        }
        
        return propertyName;

    }

    record DefaultTypeHandler(PropertyMethod property) implements TypeHandler {

        private static final String CHECKER_FORMAT = """
                                                    %1$sprivate boolean %2$sChecker = false;\n
                                                    """;

        private static final String VALIDATION_STATEMENT = """
                                                           %1$sif(!%2$s){
                                                           %3$s throw new IllegalStateException(\"%4$s\");\n
                                                           %1$s}
                                                           
                                                           """;

        @Override
        public void generateBuilderChecker(Writer writer, int indentationLevel) throws IOException {
            if (null == property.defaultValue()) {
                String declaration = CHECKER_FORMAT.formatted(
                        INDENTATION.repeat(indentationLevel),
                        property.name());
                writer.write(declaration);
            }
        }

        @Override
        public void generateBuilderValidateStatement(Writer writer, int indentationLevel) throws IOException {
            if (null == property.defaultValue()) {
                String checkerName = property.name() + "Checker";
                String message = "property " + property.name() + " must be initialized before building";
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
            String name = property.name();
            String builderType = builderName;
            String paramType = property.type().className();

            String mutatorDeclaration = mutatorDeclarationPrefix + builderType + " " + name + "(final "
                    + paramType + " " + name + "){\n";

            writer.write(mutatorDeclaration);

            String mutatorBody;
            if (property.type().primitive()) {

                mutatorBody = INDENTATION.repeat(indentationLevel + 1) + "this." + name + " = " + name + ";\n";

            } else {

                String requireNonNull = "Objects.requireNonNull(" + name + ");";
                mutatorBody = INDENTATION.repeat(indentationLevel + 1) + "this." + name + " = " + requireNonNull + "\n";

            }
            writer.write(mutatorBody);

            // set checker
            String checkerName = property.name() + "Checker";
            if (null == property.defaultValue()) {

                String setChecker = INDENTATION.repeat(3) + "this." + checkerName + " = true;\n";
                writer.write(setChecker);

            }

            String returSelf = INDENTATION.repeat(indentationLevel + 1) + "return self();\n";
            writer.write(returSelf);
            writer.write(INDENTATION.repeat(indentationLevel) + "}\n\n");

        }

    }
}
