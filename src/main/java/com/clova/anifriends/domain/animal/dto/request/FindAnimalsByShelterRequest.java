package com.clova.anifriends.domain.animal.dto.request;

import com.clova.anifriends.domain.animal.AnimalAge;
import com.clova.anifriends.domain.animal.AnimalSize;
import com.clova.anifriends.domain.animal.vo.AnimalActive;
import com.clova.anifriends.domain.animal.vo.AnimalGender;
import com.clova.anifriends.domain.animal.vo.AnimalType;

public record FindAnimalsByShelterRequest(
    String keyword,
    AnimalType type,
    AnimalGender gender,
    Boolean isNeutered,
    AnimalActive active,
    AnimalSize size,
    AnimalAge age
) {

}
