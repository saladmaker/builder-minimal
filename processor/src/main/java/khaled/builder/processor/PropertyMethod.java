package khaled.builder.processor;

import io.helidon.common.types.Annotation;
import io.helidon.common.types.TypeName;
import io.helidon.common.types.TypedElementInfo;
import java.util.List;

public record PropertyMethod(String name, TypeName type, String defaultValue) {

    static final String OPTION_DEFAULT = "io.helidon.builder.api.Option.Default";
    static final TypeName OPTION_DEFAULT_TYPE = TypeName.create(OPTION_DEFAULT);

    public static PropertyMethod create(TypedElementInfo tei) {

        TypeName returnType = tei.typeName();

        String name = tei.elementName();

        String defaultValue = tei.findAnnotation(OPTION_DEFAULT_TYPE)
                .map(PropertyMethod::extractValue)
                .orElse(null);

        return new PropertyMethod(name, returnType, defaultValue);
    }

    private static String extractValue(final Annotation annotation) {
        return annotation.stringValues()
                .stream()
                .flatMap(List::stream)
                .findFirst()
                .orElse(null);
    }
}