package khaled.builder.processor;

import io.helidon.common.types.TypedElementInfo;
import static io.helidon.common.types.TypeValues.MODIFIER_PUBLIC;
import static io.helidon.common.types.TypeValues.MODIFIER_ABSTRACT;
import static io.helidon.common.types.TypeValues.KIND_METHOD;

import java.util.Set;
import java.util.function.Predicate;

public class TypedElementInfoPredicate implements Predicate<TypedElementInfo> {

    private static final Set<String> PROPERTY_MODIFIERS = Set.of(MODIFIER_PUBLIC, MODIFIER_ABSTRACT);

    @Override
    public boolean test(TypedElementInfo tei) {

        return PROPERTY_MODIFIERS.equals(tei.modifiers())
                && KIND_METHOD.equals(tei.elementTypeKind());
    }

}
