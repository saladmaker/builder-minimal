package khaled.builder.processor;

import io.helidon.common.types.TypeName;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static khaled.builder.processor.GenerationInfo.INDENTATION;
import static khaled.builder.processor.CollectionTypeHandler.Type.LIST;
import static khaled.builder.processor.CollectionTypeHandler.Type.SET;

/**
 *
 * @author khaled
 */
public sealed interface TypeHandler permits SimpleTypeHandler,
        CollectionTypeHandler, OptionalTypeHandler {

    static final String PROPERTY_FORMAT = "%1$sprivate %2$s%3$s %4$s%5$s";
    static final String NO_INITIALIZATION = ";\n";
    static final String INITIALIZATION_FORMAT = " = %1$s;\n";

    static final String NONE = "";
    static final String FINAL = "final ";

    static final String OPTION_DEFAULT = "io.helidon.builder.api.Option.Default";
    static final TypeName OPTION_DEFAULT_TYPE = TypeName.create(OPTION_DEFAULT);

    static final String OPTION_DEFAULT_INT = "io.helidon.builder.api.Option.DefaultInt";
    static final TypeName OPTION_DEFAULT_INT_TYPE = TypeName.create(OPTION_DEFAULT_INT);

    static final String OPTION_DEFAULT_LONG = "io.helidon.builder.api.Option.DefaultLong";
    static final TypeName OPTION_DEFAULT_LONG_TYPE = TypeName.create(OPTION_DEFAULT_LONG);

    static final String OPTION_DEFAULT_DOUBLE = "io.helidon.builder.api.Option.DefaultDouble";
    static final TypeName OPTION_DEFAULT_DOUBLE_TYPE = TypeName.create(OPTION_DEFAULT_DOUBLE);

    static final String OPTION_SINGULAR = "io.helidon.builder.api.Option.Singular";
    static final TypeName OPTION_SINGULAR_TYPE = TypeName.create(OPTION_SINGULAR);

    public static TypeHandler create(PropertyMethod property) {
        TypeName type = property.type();

        if (property.collectionBased()) {

            return type.isList() ? new CollectionTypeHandler(property, LIST)
                    : new CollectionTypeHandler(property, SET);

        } else if (type.isOptional()) {
            return new OptionalTypeHandler(property);
        }
        return new SimpleTypeHandler(property);
    }

    void generateBuilderMutators(Writer writer, String builderName, int indentationLevel) throws IOException;

    PropertyMethod property();

    default void generateBuilderChecker(Writer writer, int indentationLevel) throws IOException {
    }

    default void generateBuilderValidateStatement(Writer writer, int indentationLevel) throws IOException {
    }

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

    default boolean hasDefaultValue() {
        return !extractDefaultValue().isEmpty();
    }

    default void generateBuilderProperty(Writer writer, int indentationLevel) throws IOException {
        String modifier;
        String intialValueLiteral;

        switch (this) {

            case SimpleTypeHandler dd when !extractDefaultValue().isEmpty() -> {
                modifier = NONE;
                intialValueLiteral = INITIALIZATION_FORMAT.formatted(initialValueLiteral());
            }
            case SimpleTypeHandler d -> {
                modifier = NONE;
                intialValueLiteral = NO_INITIALIZATION;
            }

            case CollectionTypeHandler collection -> {

                modifier = FINAL;
                intialValueLiteral = NO_INITIALIZATION;
                var collectionType = collection.collectionType();

                switch (collectionType) {

                    case LIST -> {
                        String constructor = LIST.initialization().formatted(initialValueLiteral());
                        intialValueLiteral = INITIALIZATION_FORMAT.formatted(constructor);
                    }
                    case SET -> {
                        String constructor = SET.initialization().formatted(initialValueLiteral());
                        intialValueLiteral = INITIALIZATION_FORMAT.formatted(constructor);
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

    private boolean collectionBased() {
        return property().type().isList() || property().type().isSet();
    }

    private TypeName typeName() {
        return property().type();
    }

    private Optional<TypeName> defaultValuesTargetTypeName() {
        if (typeName().isOptional()) {
            return Optional.empty();
        }
        if (collectionBased()) {
            return typeName().typeArguments().stream()
                    .findFirst();
        } else {
            return Optional.of(typeName().boxed());
        }
    }

    default List<?> extractDefaultValue() {
        Optional<TypeName> targetOpt = defaultValuesTargetTypeName();
        List<?> value = List.of();
        if (targetOpt.isPresent()) {
            TypeName target = targetOpt.get();
            String name = target.className();

            switch (name) {
                case "String", "Character", "Boolean" -> {
                    switch (this) {
                        case SimpleTypeHandler s -> {
                            return extractOne(OPTION_DEFAULT_TYPE);

                        }

                        case CollectionTypeHandler c -> {
                            return extractAll(OPTION_DEFAULT_TYPE);
                        }
                        case OptionalTypeHandler o -> {
                            throw new IllegalStateException("optional cannot have default values");
                        }
                    }

                }
                case "Integer", "Short", "Byte" -> {
                    switch (this) {
                        case SimpleTypeHandler s -> {
                            return extractOne(OPTION_DEFAULT_INT_TYPE);
                        }
                        case CollectionTypeHandler c -> {
                            return extractAll(OPTION_DEFAULT_INT_TYPE);
                        }
                        case OptionalTypeHandler o -> {
                            throw new IllegalStateException("optional cannot have default values");
                        }
                    }
                }

                case "Long" -> {
                    switch (this) {
                        case SimpleTypeHandler s -> {
                            return extractOne(OPTION_DEFAULT_LONG_TYPE);
                        }
                        case CollectionTypeHandler c -> {
                            return extractAll(OPTION_DEFAULT_LONG_TYPE);
                        }
                        case OptionalTypeHandler o -> {
                            throw new IllegalStateException("optional cannot have default values");
                        }
                    }

                }

                case "Double", "Float" -> {
                    switch (this) {
                        case SimpleTypeHandler s -> {
                            return extractOne(OPTION_DEFAULT_DOUBLE_TYPE);

                        }

                        case CollectionTypeHandler c -> {
                            return extractAll(OPTION_DEFAULT_DOUBLE_TYPE);

                        }
                        case OptionalTypeHandler o -> {
                            throw new IllegalStateException("optional cannot have default values");
                        }
                    }
                }

                default -> {
                    throw new IllegalStateException("unkown type: " + name + " handler: " + this);
                }
            }
        }
        return value;
    }

    private List<?> extractOne(TypeName annoType) {
        var tei = property().typedInfo();
        var annoOpt = tei.findAnnotation(annoType);
        if (annoOpt.isPresent()) {
            var anno = annoOpt.get();
            var allValues = anno.values().values().stream()
                    .flatMap(it -> {

                        if (it instanceof Collection<?> c) {
                            return c.stream();
                        }
                        return Stream.empty();

                    })
                    .toList();
            if (allValues.size() > 1 || allValues.isEmpty()) {
                throw new IllegalStateException("default value must of size 1");
            }
            return List.copyOf(allValues);
        }
        return List.of();
    }

    private List<?> extractAll(TypeName annoType) {
        var tei = property().typedInfo();
        var annoOpt = tei.findAnnotation(annoType);
        if (annoOpt.isPresent()) {
            var anno = annoOpt.get();
            var allValues = anno.values().values().stream()
                    .flatMap(it -> {

                        if (it instanceof Collection<?> c) {
                            return c.stream();
                        }
                        return Stream.empty();

                    })
                    .toList();

            return List.copyOf(allValues);
        }
        return List.of();
    }

    default String initialValueLiteral() {
        String literal = "";

        if (defaultValuesTargetTypeName().isPresent()) {
            String name = defaultValuesTargetTypeName().get().className();
            List<?> defaultValue = extractDefaultValue();

            String arrange = defaultValue.stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(", "));
            /*
            using the compiler and the javac jvm process as means of validation
            ValueOf will validate the String
            single quotation with validate the char
             */
            switch (name) {
                case null ->
                    throw new IllegalStateException("null default value");
                case "Byte" ->{
                    literal = defaultValue.stream()
                            .map(Object::toString)
                            .map(TypeHandler::ByteLiteral)
                            .collect(Collectors.joining(", "));
                    
                }
                case "Short" ->{
                    literal = defaultValue.stream()
                            .map(Object::toString)
                            .map(TypeHandler::ShortLiteral)
                            .collect(Collectors.joining(", "));
                }
                
                case "Float" ->{
                    literal = defaultValue.stream()
                            .map(Object::toString)
                            .map(TypeHandler::floatLiteral)
                            .collect(Collectors.joining(", "));
                }
                case "Boolean", "Double", "Integer", "Long" -> {
                    literal = arrange;
                }

                case "String" -> {
                    literal = defaultValue.stream()
                            .map(Object::toString)
                            .map(TypeHandler::StringLiteral)
                            .collect(Collectors.joining(", "));
                }

                case "Character" -> {
                    literal = defaultValue.stream()
                            .map(Object::toString)
                            .map(TypeHandler::CharLiteral)
                            .collect(Collectors.joining(", "));

                }

                default -> {

                    throw new IllegalStateException("unkown type defaulted" + name);
                }
            }
            return literal;
        }
        return literal;

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
                case ImplementationGenerator i ->
                    propertyName;
                case BuilderGenerator b ->
                    "Optional.ofNullable(%1$s)".formatted(propertyName);
                case PrototypeGenerator p ->
                    throw new IllegalStateException("accessor not supported");
            };
        }

        return propertyName;

    }

    private static String StringLiteral(String value) {
        return "\"" + value + "\"";
    }

    private static String CharLiteral(String value) {
        if (value.length() == 1) {
            return "\'" + value + "\'";
        }
        return "";
    }

    private static String ByteLiteral(String value) {
        if (value.isEmpty() || value.isBlank()) {
            return "";
        }
        return "(byte)" + Byte.valueOf(value);
    }

    private static String ShortLiteral(String value) {
        if (value.isEmpty() || value.isBlank()) {
            return "";
        }
        return "(short)" + Short.valueOf(value);
    }
    
    private static String floatLiteral(String value) {
        if (value.isEmpty() || value.isBlank()) {
            return "";
        }
        return "(float)" + Float.valueOf(value);
    }
    

}
