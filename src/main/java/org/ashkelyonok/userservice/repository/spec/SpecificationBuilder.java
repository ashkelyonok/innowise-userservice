package org.ashkelyonok.userservice.repository.spec;

import jakarta.persistence.criteria.Path;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

@UtilityClass
public class SpecificationBuilder {

    public static <T> Specification<T> likeIgnoreCase(String field, String value) {
        return (root, query, cb) -> {
            if (value == null || value.trim().isEmpty()) {
                return cb.conjunction();
            }
            Path<String> fieldPath = root.get(field);
            return cb.like(cb.lower(fieldPath), "%" + value.toLowerCase().trim() + "%");
        };
    }

    public static <T> Specification<T> contains(String field, String value) {
        return (root, query, cb) -> {
            if (value == null || value.trim().isEmpty()) {
                return cb.conjunction();
            }
            return cb.like(root.get(field), "%" + value.trim() + "%");
        };
    }

    public static <T> Specification<T> attributeEquals(String field, Object value) {
        return (root, query, cb) -> {
            if (value == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get(field), value);
        };
    }
}
