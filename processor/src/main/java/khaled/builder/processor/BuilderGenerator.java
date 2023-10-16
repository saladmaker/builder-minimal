package khaled.builder.processor;

import io.helidon.common.types.TypeName;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static khaled.builder.processor.GenerationInfo.INDENTATION;
import static khaled.builder.processor.GenerationInfo.PROTOTYPE_BUILDER;
import static khaled.builder.processor.GenerationInfo.COMMON_BUILDER;
import static khaled.builder.processor.GenerationInfo.CHECKER_SUFFIX;

/**
 *
 * @author khaled
 */
public class BuilderGenerator {

    private final Writer writer;
    private final String builderName;
    private final String prototypeName;
    private final String implName;
    private final Set<PropertyMethod> properties;

    private final Map<String, String> propertyCheckers = new LinkedHashMap<>();

    BuilderGenerator(GenerationInfo generationInfo, Writer writer) {
        this.writer = writer;
        this.builderName = generationInfo.builderName();
        this.prototypeName = generationInfo.prototypeName();
        this.implName = generationInfo.implName();
        this.properties = generationInfo.properties();

    }

    void generateBuilder() throws IOException {

        generateDeclaration();
        generateCheckers();

        generateProperties();

        //mutators
        generateMutators();

        // accessor
        generateAccessors();

        generateValidate();

        generateBuilds();

        writer.write(INDENTATION + "}\n\n");
    }

    private void generateDeclaration() throws IOException {
        String builderDeclaration = INDENTATION + "class " + builderName + " implements "
                + PROTOTYPE_BUILDER + "<" + builderName + ", " + prototypeName + ">,\n" + INDENTATION.repeat(4)
                + COMMON_BUILDER + "<" + builderName + ", " + prototypeName + ">{\n";

        writer.write(builderDeclaration);

    }

    private void generateProperties() throws IOException {
        writer.write("\n\n" + INDENTATION.repeat(2) + "//properties\n");
        String propertyDeclarationPrefix = INDENTATION.repeat(2) + "private ";
        for (var property : properties) {
            TypeName type = property.type();

            String name = property.name();

            String className = type.classNameWithTypes();

            String literalValue = "";
            String finalModifier = "";
            final String defaultValue = property.defaultValue();
            if (null != defaultValue || property.collectionBased()) {
                if (property.collectionBased()) {
                    finalModifier = "final ";
                }
                literalValue = generateLiteralDefaultValue(defaultValue, type.boxed());
                literalValue = " = " + literalValue;
            }
            String propertyDeclaration = propertyDeclarationPrefix + finalModifier + className + " " + name;

            propertyDeclaration = propertyDeclaration + literalValue + ";\n";
            writer.write(propertyDeclaration);

        }

    }

    private String generateLiteralDefaultValue(String defaultValue, TypeName type) {
        String name = type.className();

        /*
        using the compiler and the javac jvm process as means of validation
            ValueOf will validate the String
            single quotation with validate the char
         */
        return switch (name) {
            case null -> throw new IllegalStateException("null default value");

            case "String" -> "\"" + defaultValue + "\"";

            case "Boolean" -> Boolean.valueOf(defaultValue).toString();

            case "Byte" -> Byte.valueOf(defaultValue).toString();

            case "Character" -> "\'" + defaultValue + "\'";

            case "Double" -> Double.valueOf(defaultValue).toString();

            case "Float" -> Float.valueOf(defaultValue).toString();

            case "Integer" -> Integer.valueOf(defaultValue).toString();

            case "Long" -> Long.valueOf(defaultValue).toString();

            case "Short" -> Short.valueOf(defaultValue).toString();

            case "List" -> "new ArrayList<>()";

            case "Set" -> "new LinkedHashSet<>()";    

            default -> throw new IllegalStateException("unkown type defaulted");
        };
    }

    private void generateCheckers() throws IOException {
        writer.write("\n\n" + INDENTATION.repeat(2) + "//checkers\n");
        String propertyCheckerPrefix = INDENTATION.repeat(2) + "private boolean ";
        for (var property : properties) {
            if (null == property.defaultValue() && !property.collectionBased()) {
                String name = property.name();

                String checker = property.name() + CHECKER_SUFFIX;

                if (null != propertyCheckers.put(name, checker)) {
                    throw new IllegalStateException("duplicate property" + name);

                }

                String propertyCheckerDeclaration = propertyCheckerPrefix + checker + " = false;\n";
                writer.write(propertyCheckerDeclaration);

            }
        }
    }

    private void generateMutators() throws IOException {
        writer.write("\n\n" + INDENTATION.repeat(2) + "//mutators\n");
        for (var property : properties) {
            if (property.collectionBased()) {
                generateCollectionMutator(property);
            } else {
                generateSimpleMutator(property);
            }
        }
    }

    private void generateSimpleMutator(PropertyMethod property) throws IOException {
        String mutatorDeclarationPrefix = INDENTATION.repeat(2) + "public ";
        String name = property.name();
        String builderType = builderName;
        String paramType = property.type().className();

        String mutatorDeclaration = mutatorDeclarationPrefix + builderType + " " + name + "(final "
                + paramType + " " + name + "){\n";

        writer.write(mutatorDeclaration);

        String mutatorBody;
        if (property.type().primitive()) {

            mutatorBody = INDENTATION.repeat(3) + "this." + name + " = " + name + ";\n";

        } else {

            String requireNonNull = "Objects.requireNonNull(" + name + ");";
            mutatorBody = INDENTATION.repeat(3) + "this." + name + " = " + requireNonNull + "\n";

        }
        writer.write(mutatorBody);

        // set checker
        String checker = propertyCheckers.get(name);
        if (null != checker) {

            String setChecker = INDENTATION.repeat(3) + "this." + checker + " = true;\n";
            writer.write(setChecker);

        }

        String returSelf = INDENTATION.repeat(3) + "return self();\n";
        writer.write(returSelf);
        writer.write(INDENTATION.repeat(2) + "}\n\n");

    }

    private void generateCollectionMutator(PropertyMethod property) throws IOException {
        String name = property.name();
        generateAddCollection(property, name, true, false);
        generateAddCollection(property, "add" + capitalize(name), false, false);
        String sigular = property.singlar();
        if (null != sigular) {
            if (sigular.isBlank()) {
                generateAddCollection(property, "add" + capitalize(singular(name)), false, true);
            } else {
                generateAddCollection(property, sigular, false, true);
            }
        }

    }

    private void generateAddCollection(PropertyMethod property, String methodName, boolean clear, boolean singular) throws IOException {
        String mutatorDeclarationPrefix = INDENTATION.repeat(2) + "public ";
        String builderType = builderName;
        String propertyName = property.name();
        String paramName;
        String paramType;
        if (singular) {
            paramType = property.type().typeArguments().getFirst().className();
            paramName = singular(propertyName);

        } else {
            paramType = covary(property.type());
            paramName = propertyName;
        }
        if (null == paramType) {
            String message = """
                          paramType is null method name:%1$s, clear:%2$s, singular:%3$s
                          property method:%4$s
                          """.formatted(methodName, clear, singular, property);
            throw new IllegalStateException(message);
        }
        String mutatorDeclaration = mutatorDeclarationPrefix + builderType + " " + methodName + "(final "
                + paramType + " " + paramName + "){\n";

        writer.write(mutatorDeclaration);

        String requireNonNull = INDENTATION.repeat(3) + "Objects.requireNonNull(" + paramName + ");\n";
        writer.write(requireNonNull);

        if (clear) {
            String clearCollection = INDENTATION.repeat(3) + "this." + propertyName + ".clear();\n";
            writer.write(clearCollection);
        }
        if (singular) {
            String add = INDENTATION.repeat(3) + "this." + propertyName + ".add(" + paramName + ");\n";
            writer.write(add);

        } else {
            String addAll = INDENTATION.repeat(3) + "this." + propertyName + ".addAll(" + paramName + ");\n";
            writer.write(addAll);
        }
        String returSelf = INDENTATION.repeat(3) + "return self();\n";

        writer.write(returSelf);
        writer.write(INDENTATION.repeat(2) + "}\n\n");
    }

    private String covary(TypeName type) {
        List<TypeName> arguments = type.typeArguments()
                .stream()
                .map(it -> TypeName.builder(it).wildcard(true).build())
                .collect(Collectors.toList());
        String name = type.className();
        if (!arguments.isEmpty()) {
            String paramFormat = "<%1$s>";
            String tokens = arguments.stream()
                    .map(it -> (it.wildcard() ? "? extends " : "") + it.className())
                    .collect(Collectors.joining(", "));
            String params = paramFormat.formatted(tokens);
            name = name + params;

        }
        return name;
    }

    private void generateAccessors() throws IOException {
        String accessorDeclarationPrefix = INDENTATION.repeat(2) + "public ";
        for (var property : properties) {
            String type = property.type().classNameWithTypes();
            String name = property.name();

            String accessorDeclaration = accessorDeclarationPrefix + type + " " + name + "(){\n";
            writer.write(accessorDeclaration);

            String accessorBody = INDENTATION.repeat(3) + "return this." + name + ";\n";
            writer.write(accessorBody);
            writer.write(INDENTATION.repeat(2) + "}\n\n");
        }
    }

    private void generateValidate() throws IOException {
        String validatorDeclarationPrefix = INDENTATION.repeat(2) + "private void validate(){\n\n";
        writer.write(validatorDeclarationPrefix);
        for (var propertyName : propertyCheckers.keySet()) {

            String message = propertyName + " must be set before building";
            String checker = propertyCheckers.get(propertyName);
            String validationFormat = """
          %1$sif(!%2$s){%n
          %3$sthrow new IllegalStateException(\"%4$s\");%n
          %1$s}%n"""
                    .formatted(INDENTATION.repeat(3),
                            checker,
                            INDENTATION.repeat(4),
                            message);
            writer.write(validationFormat);
        }
        writer.write(INDENTATION.repeat(2) + "}\n\n");
    }

    private void generateBuilds() throws IOException {
        String buildPrototypeDeclarationPrefix = INDENTATION.repeat(2) + "@Override\n" + INDENTATION.repeat(2) + "public "
                + prototypeName + " buildPrototype(){\n";
        writer.write(buildPrototypeDeclarationPrefix);
        writer.write(INDENTATION.repeat(3) + "validate();\n");
        writer.write(INDENTATION.repeat(3) + "return new " + implName + "(this);\n");
        writer.write(INDENTATION.repeat(2) + "}\n");
        String buildDeclarationPrefix = INDENTATION.repeat(2) + "@Override\n" + INDENTATION.repeat(2) + "public " + prototypeName
                + " build(){\n";
        writer.write(buildDeclarationPrefix);
        writer.write(INDENTATION.repeat(3) + "return buildPrototype();\n");
        writer.write(INDENTATION.repeat(2) + "}\n");
    }

    private static String capitalize(String name) {
        if (!name.isBlank()) {
            return Character.toUpperCase(name.charAt(0)) + name.substring(1);
        }
        return name;

    }

    private static String singular(String name) {
        if (name.length() > 1 && name.endsWith("s")) {
            return name.substring(0, name.length() - 1);
        }
        return name;
    }
}
