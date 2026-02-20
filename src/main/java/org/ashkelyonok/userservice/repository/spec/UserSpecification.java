package org.ashkelyonok.userservice.repository.spec;

import lombok.experimental.UtilityClass;
import org.ashkelyonok.userservice.model.dto.UserFilterDto;
import org.ashkelyonok.userservice.model.entity.User;
import org.springframework.data.jpa.domain.Specification;

@UtilityClass
public class UserSpecification {

    public static Specification<User> filterBy(UserFilterDto filter) {
        return Specification.<User>where(null)
                .and(SpecificationBuilder.attributeIn("id", filter.getIds()))
                .and(SpecificationBuilder.attributeEquals("email", filter.getEmail()))
                .and(SpecificationBuilder.likeIgnoreCase("name", filter.getName()))
                .and(SpecificationBuilder.likeIgnoreCase("surname", filter.getSurname()));
    }
}
