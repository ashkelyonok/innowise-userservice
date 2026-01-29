package org.ashkelyonok.userservice.repository.spec;

import lombok.experimental.UtilityClass;
import org.ashkelyonok.userservice.model.entity.User;
import org.springframework.data.jpa.domain.Specification;

@UtilityClass
public class UserSpecification {

    public static Specification<User> filterByNameAndSurname(String name, String surname) {
        return Specification.<User>where(null)
                .and(SpecificationBuilder.likeIgnoreCase("name", name))
                .and(SpecificationBuilder.likeIgnoreCase("surname", surname));
    }
}
