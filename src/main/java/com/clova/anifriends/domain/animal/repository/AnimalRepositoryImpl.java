package com.clova.anifriends.domain.animal.repository;

import static com.clova.anifriends.domain.animal.QAnimal.animal;

import com.clova.anifriends.domain.animal.Animal;
import com.clova.anifriends.domain.animal.AnimalAge;
import com.clova.anifriends.domain.animal.AnimalSize;
import com.clova.anifriends.domain.animal.wrapper.AnimalActive;
import com.clova.anifriends.domain.animal.wrapper.AnimalGender;
import com.clova.anifriends.domain.animal.wrapper.AnimalType;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AnimalRepositoryImpl implements AnimalRepositoryCustom {

    private final JPAQueryFactory query;

    @Override
    public Page<Animal> findAnimalsByShelter(
        Long shelterId,
        String keyword,
        AnimalType type,
        AnimalGender gender,
        Boolean isNeutered,
        AnimalActive active,
        AnimalSize size,
        AnimalAge age,
        Pageable pageable
    ) {
        List<Animal> animals = query.select(animal)
            .from(animal)
            .where(
                animal.shelter.shelterId.eq(shelterId),
                animalNameContains(keyword),
                animalTypeContains(type),
                animalGenderContains(gender),
                animalIsNeutered(isNeutered),
                animalActiveContains(active),
                animalSizeContains(size),
                animalAgeContains(age)
            )
            .limit(pageable.getPageSize())
            .offset(pageable.getOffset())
            .fetch();

        Long count = query.select(animal.count())
            .from(animal)
            .where(
                animal.shelter.shelterId.eq(shelterId),
                animalNameContains(keyword),
                animalTypeContains(type),
                animalGenderContains(gender),
                animalIsNeutered(isNeutered),
                animalActiveContains(active),
                animalSizeContains(size),
                animalAgeContains(age)
            )
            .fetchOne();

        return new PageImpl<>(animals, pageable, count);
    }

    private BooleanExpression animalNameContains(String keyword) {
        return keyword != null ? animal.name.name.contains(keyword) : null;
    }

    private BooleanExpression animalTypeContains(
        AnimalType type
    ) {
        return type != null ? animal.type.stringValue().eq(type.getName()) : null;
    }

    private BooleanExpression animalGenderContains(
        AnimalGender gender
    ) {
        return gender != null ? animal.gender.stringValue().eq(gender.getName()) : null;
    }

    private BooleanExpression animalIsNeutered(
        Boolean isNeuteredFilter
    ) {
        return isNeuteredFilter != null ? animal.neutered.isNeutered.eq(isNeuteredFilter) : null;
    }

    private BooleanExpression animalActiveContains(
        AnimalActive active
    ) {
        return active != null ? animal.active.stringValue().contains(active.getName()) : null;
    }

    private BooleanExpression animalSizeContains(
        AnimalSize size
    ) {
        if (Objects.isNull(size)) {
            return null;
        }

        int minWeight = size.getMinWeight();
        int maxWeight = size.getMaxWeight();

        return animal.weight.weight.between(minWeight, maxWeight);
    }

    private BooleanExpression animalAgeContains(
        AnimalAge age
    ) {
        if (Objects.isNull(age)) {
            return null;
        }

        int minMonth = age.getMinMonth();
        int maxMonth = age.getMaxMonth();

        LocalDate currentDate = LocalDate.now();
        LocalDate minDate = currentDate.minusMonths(minMonth);
        LocalDate maxDate = currentDate.minusMonths(maxMonth);

        return animal.birthDate.between(maxDate, minDate);
    }
}