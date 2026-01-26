package org.ashkelyonok.userservice.repository.spec;

import lombok.experimental.UtilityClass;
import org.ashkelyonok.userservice.model.entity.Card;
import org.springframework.data.jpa.domain.Specification;

@UtilityClass
public class CardSpecification {

    public static Specification<Card> filterByNumber(String number) {
        return SpecificationBuilder.likeIgnoreCase("number", number);
    }

    public static Specification<Card> filterByActive(Boolean active) {
        return SpecificationBuilder.attributeEquals("active", active);
    }

    public static Specification<Card> filterByHolderAndNumber(String holder, String number) {
        return Specification.<Card>where(null)
                .and(SpecificationBuilder.likeIgnoreCase("holder", holder))
                .and(SpecificationBuilder.like("number", number));
    }
}
