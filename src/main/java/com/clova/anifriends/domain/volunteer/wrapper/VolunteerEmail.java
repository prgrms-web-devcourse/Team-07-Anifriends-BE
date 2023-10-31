package com.clova.anifriends.domain.volunteer.wrapper;

import com.clova.anifriends.domain.volunteer.exception.VolunteerBadRequestException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Getter;

@Getter
@Embeddable
public class VolunteerEmail {

    @Column(name = "email")
    private String email;

    protected VolunteerEmail() {
    }

    public VolunteerEmail(String value) {
        validateVolunteerEmail(value);
        this.email = value;
    }

    private void validateVolunteerEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new VolunteerBadRequestException("이메일은 필수 항목입니다.");
        }

        String pattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

        Pattern emailPattern = Pattern.compile(pattern);
        Matcher matcher = emailPattern.matcher(email);

        if (!matcher.matches()) {
            throw new VolunteerBadRequestException("이메일 형식에 맞지 않습니다.");
        }
    }

}
