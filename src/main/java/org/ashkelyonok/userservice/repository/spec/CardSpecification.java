package org.ashkelyonok.userservice.repository.spec;

import lombok.experimental.UtilityClass;
import org.ashkelyonok.userservice.model.entity.Card;
import org.springframework.data.jpa.domain.Specification;

@UtilityClass
public class CardSpecification {

    public static Specification<Card> filterByNumber(String number) {
        return SpecificationBuilder.contains("number", number);
    }

    public static Specification<Card> filterByActive(Boolean active) {
        return SpecificationBuilder.attributeEquals("active", active);
    }

    public static Specification<Card> filterByHolder(String holder) {
        return SpecificationBuilder.likeIgnoreCase("holder", holder);
    }
}
