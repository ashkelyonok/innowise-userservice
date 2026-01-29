package org.ashkelyonok.userservice.mapper;

import org.ashkelyonok.userservice.model.dto.CardCreateDto;
import org.ashkelyonok.userservice.model.dto.CardResponseDto;
import org.ashkelyonok.userservice.model.dto.CardUpdateDto;
import org.ashkelyonok.userservice.model.entity.Card;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.time.LocalDate;

@Mapper(componentModel = "spring")
public interface CardMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "expirationDate", qualifiedByName = "dateToString")
    CardResponseDto toDto(Card card);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "holder", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "expirationDate", qualifiedByName = "stringToDate")
    Card toEntity(CardCreateDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "number", ignore = true)
    @Mapping(target = "holder", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "expirationDate", qualifiedByName = "stringToDate")
    void updateCardFromDto(CardUpdateDto dto, @MappingTarget Card card);

    @Named("stringToDate")
    default LocalDate stringToDate(String mmYY) {
        if (mmYY == null || mmYY.isBlank()) {
            return null;
        }
        String[] parts = mmYY.split("/");
        int month = Integer.parseInt(parts[0]);
        int year = 2000 + Integer.parseInt(parts[1]);

        return LocalDate.of(year, month, 1);
    }

    @Named("dateToString")
    default String dateToString(LocalDate date) {
        if (date == null) {
            return null;
        }
        return String.format("%02d/%02d", date.getMonthValue(), date.getYear() % 100);
    }
}
