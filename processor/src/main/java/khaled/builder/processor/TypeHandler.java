package khaled.builder.processor;

import io.helidon.common.types.Annotation;
import io.helidon.common.types.TypeName;
import io.helidon.common.types.TypedElementInfo;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
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

    public static TypeHandler create(TypedElementInfo tei) {
        TypeName type = tei.typeName();
        String name = tei.elementName();

        List<?> defaultValues;

        if (collectionBased(type)) {
            TypeName target = targetTypeOfCollection(type);
            
            defaultValues = extractValues(target.className(), tei);
            String singular = tei.findAnnotation(OPTION_SINGULAR_TYPE)
                    .map(TypeHandler::extractSingularValue)
                    .orElse(null);
            return type.isList()
                    ? new CollectionTypeHandler(name, type, singular, defaultValues, LIST)
                    : new CollectionTypeHandler(name, type, singular, defaultValues, SET);

        } else if (type.isOptional()) {
            return new OptionalTypeHandler(name, type);

        } else {
            String target = boxedName(type);
            defaultValues = extractValues(target, tei);
            if (defaultValues.size() > 1) {
                throw new IllegalStateException(type + " " + name + "() can not have multiple default values ");
            }
            if (!defaultValues.isEmpty()) {
                return new SimpleTypeHandler(name, type, defaultValues.getFirst());
            } else {
                return new SimpleTypeHandler(name, type, null);
            }

        }
    }

    void generateBuilderMutators(Writer writer, String builderName, int indentationLevel) throws IOException;

    TypeName type();

    String name();

    default void generateBuilderChecker(Writer writer, int indentationLevel) throws IOException {
    }

    default void generateBuilderValidateStatement(Writer writer, int indentationLevel) throws IOException {
    }

    default void generateAccessors(Writer writer, int indentationLevel, Generator generator) throws IOException {
        String accessorDeclarationPrefix = INDENTATION.repeat(indentationLevel) + "public ";

        String accessorDeclaration = accessorDeclarationPrefix + shortHandType() + " " + name() + "(){\n";
        writer.write(accessorDeclaration);
        String propertyName = "this." + name();
        String accessMechanism = accessMechanism(propertyName, generator);
        String accessorBody = INDENTATION.repeat(indentationLevel + 1) + "return " + accessMechanism + ";\n";
        writer.write(accessorBody);
        writer.write(INDENTATION.repeat(indentationLevel) + "}\n\n");
    }

    default boolean hasDefaultValue() {
        return switch (this) {
            case SimpleTypeHandler s ->
                null != s.defaultValue();
            case CollectionTypeHandler c ->
                true;
            case OptionalTypeHandler o ->
                false;
        };
    }

    default void generateBuilderProperty(Writer writer, int indentationLevel) throws IOException {
        String modifier;
        String intialValueLiteral;

        switch (this) {

            case SimpleTypeHandler dd when null != dd.defaultValue() -> {
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
                
                switch (collection.collectionType()) {

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
                name(),
                intialValueLiteral
        );
        writer.write(declaration);

    }

    default boolean collectionBased() {
        return collectionBased(type());
    }



    private String initialValueLiteral() {
        switch (this) {
            case SimpleTypeHandler s -> {
                String literalType = boxedName();
                String defaultValue = s.defaultValue().toString();
                return mapToLiteral(literalType, defaultValue);
            }
            case CollectionTypeHandler c -> {
                final String target = targetTypeOfCollection(type()).className();
                List<?> defaultValues = c.defaultValues();
                return mapListToLiteral(target, defaultValues);
            }
            default -> {
                return "";
            }

        }
    }

    default void generateImplementationProperty(Writer writer, int indentationLevel) throws IOException {
        String declaration = PROPERTY_FORMAT.formatted(
                INDENTATION.repeat(indentationLevel),
                FINAL,
                shortHandType(),
                name(),
                NO_INITIALIZATION
        );
        writer.write(declaration);
    }

    private String shortHandType() {
        return type().classNameWithTypes();
    }

    private String builderPropertyType() {
        return switch (this) {
            case OptionalTypeHandler o ->
                o.mutatorType();
            default ->
                shortHandType();
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
    
    private String boxedName() {
        return boxedName(type());
    }
    
    private static String boxedName(TypeName type) {
        return type.boxed().className();
    }

    private static TypeName targetTypeOfCollection(TypeName type) {
        return type.typeArguments()
                .stream()
                .findFirst()
                .orElseThrow(IllegalStateException::new);
    }
    private static String mapListToLiteral(final String type, List<?> defaultValues){
        return defaultValues.stream()
                .map(Object::toString)
                .map(it -> mapToLiteral(type, it))
                .collect(Collectors.joining(", "));
    }
    private static String mapToLiteral(String target, String defaultValue) {
        switch (target) {
            
            case "Boolean" -> {
                return Boolean.valueOf(defaultValue).toString();
            }
            case "Character" -> {
                return charLiteral(defaultValue);
            }
            case "String" -> {
                return stringLiteral(defaultValue);
            }
       
            case "Integer" -> {
                return Integer.valueOf(defaultValue).toString();
            }
            case "Byte" -> {
                return byteLiteral(defaultValue);
            }
            case "Short" -> {
                return shortLiteral(defaultValue);
            }
            
            case "Long" ->{
                return longLiteral(defaultValue);
            }
            case "Double" -> {
                return Double.valueOf(defaultValue).toString();
            }

            case "Float" -> {
                return floatLiteral(defaultValue);
            }
            default ->
                throw new IllegalStateException("unkown type: " + target);

        }

    }

    private static String stringLiteral(String value) {
        return "\"" + value + "\"";
    }

    private static String charLiteral(String value) {
        if (value.length() == 1) {
            return "\'" + value + "\'";
        }
        throw new IllegalStateException("char defaul value must be of length 1");
    }

    private static String byteLiteral(String value) {
        if (value.isEmpty() || value.isBlank()) {
            return "";
        }
        return "(byte)" + Byte.valueOf(value);
    }

    private static String shortLiteral(String value) {
        return "(short)" + Short.valueOf(value);
    }

    private static String floatLiteral(String value) {
        return "(float)" + Float.valueOf(value);
    }
    private static String longLiteral(String value) {
        return Long.valueOf(value) + "L";
    }

    private static String extractSingularValue(final Annotation annotation) {
        return annotation.stringValue()
                .orElse(null);
    }

    private static boolean collectionBased(TypeName type) {
        Objects.requireNonNull(type);
        return type.isList() || type.isSet();
    }
    
    private static List<?> extractValues(String type, TypedElementInfo tei) {
        
        return switch (type) {
            case "String", "Boolean", "Character" -> extractStrings(tei);
            case "Byte", "Short", "Integer" -> extractInts(tei);

            case "Long" -> extractLongs(tei);

            case "Float", "Double" -> extractDoubles(tei);
            default ->
                throw new IllegalStateException("unkown type" + type);

        };

    }
    
    private static List<?> extractStrings(TypedElementInfo tei){
        
        Optional<Annotation> defOpt = tei.findAnnotation(OPTION_DEFAULT_TYPE);
        if(defOpt.isPresent()){
            Annotation defaultAnno = defOpt.get();
            return defaultAnno.stringValues().stream()
                    .flatMap(List::stream)
                    .toList();
        }
        return List.of();
    }
    private static List<?> extractInts(TypedElementInfo tei){
        
        Optional<Annotation> defOpt = tei.findAnnotation(OPTION_DEFAULT_INT_TYPE);
        if(defOpt.isPresent()){
            Annotation defaultAnno = defOpt.get();
            return defaultAnno.intValues().stream()
                    .flatMap(List::stream)
                    .toList();
        }
        return List.of();
    }
    private static List<?> extractLongs(TypedElementInfo tei){
        
        Optional<Annotation> defOpt = tei.findAnnotation(OPTION_DEFAULT_LONG_TYPE);
        if(defOpt.isPresent()){
            Annotation defaultAnno = defOpt.get();
            return defaultAnno.longValues().stream()
                    .flatMap(List::stream)
                    .toList();
        }
        return List.of();
    }
    private static List<?> extractDoubles(TypedElementInfo tei){
        
        Optional<Annotation> defOpt = tei.findAnnotation(OPTION_DEFAULT_DOUBLE_TYPE);
        if(defOpt.isPresent()){
            Annotation defaultAnno = defOpt.get();
            return defaultAnno.doubleValues().stream()
                    .flatMap(List::stream)
                    .toList();
        }
        return List.of();
    }

    
}
