package khaled.builder.processor;

import io.helidon.common.types.TypeInfo;
import io.helidon.common.types.TypeName;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author khaled
 */
public record GenerationInfo(
        String prototypeName,
        String builderName,
        String implName,
        String superTypeName,
        String packageName,
        Set<TypeHandler> typeHandlers) {

    static final String BLUEPRINT_SUFFIX = "Blueprint";
    static final String INDENTATION = "    ";

    static final String PROTOTYPE = "io.helidon.builder.api.Prototype";

    static final String PROTOTYPE_BUILDER = "Prototype.Builder";

    static final String CHECKER_SUFFIX = "Checker";

    static final String IMPL_SUFFIX = "Impl";

    static final String COMMON_BUILDER = "io.helidon.common.Builder";

    private static final String BUILDER_NAME = "Builder";

    public static GenerationInfo create(TypeInfo blueprint) {
        TypeName type = blueprint.typeName();

        String blueprintName = type.className();

        String prototypeName = blueprintName.substring(0, blueprintName.length() - BLUEPRINT_SUFFIX.length());

        String implName = prototypeName + IMPL_SUFFIX;

        String packageName = type.packageName();

        String builderName = BUILDER_NAME;

        Set<TypeHandler> properties = blueprint.elementInfo()
                .stream()
                .map(PropertyMethod::create)
                .map(TypeHandler::create)
                .collect(Collectors.toSet());

        return new GenerationInfo(prototypeName, builderName, implName, blueprintName, packageName, properties);

    }
    
}
