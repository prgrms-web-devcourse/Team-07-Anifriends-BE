package com.clova.anifriends.domain.recruitment.wrapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;

import com.clova.anifriends.domain.recruitment.exception.RecruitmentBadRequestException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RecruitmentInfoTest {

    @Nested
    @DisplayName("RecruitmentTime 생성 시")
    class newTimeTest {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = now.plusHours(1);
        LocalDateTime endTime = startTime.plusMinutes(1);
        LocalDateTime deadline = startTime.minusMinutes(1);
        boolean isClosed = false;
        int capacity = 10;


        @Test
        @DisplayName("성공")
        void newRecruitmentInfo() {
            //given
            //when
            RecruitmentInfo recruitmentTime = new RecruitmentInfo(startTime, endTime, deadline,
                isClosed, capacity);

            //then
            assertThat(recruitmentTime.getStartTime()).isEqualTo(startTime);
            assertThat(recruitmentTime.getEndTime()).isEqualTo(endTime);
        }

        @Test
        @DisplayName("예외(RecruitmentBadRequestException): startTime이 null")
        void exceptionWhenStartTimeIsNull() {
            //given
            LocalDateTime nullStartTime = null;

            //when
            Exception exception = catchException(
                () -> new RecruitmentInfo(nullStartTime, endTime, deadline, isClosed, capacity));

            //then
            assertThat(exception).isInstanceOf(RecruitmentBadRequestException.class);
        }

        @Test
        @DisplayName("예외(RecruitmentBadRequestException): endTime이 null")
        void exceptionWhenEndTimeIsNull() {
            //given
            LocalDateTime nullEndTime = null;

            //when
            Exception exception = catchException(
                () -> new RecruitmentInfo(startTime, nullEndTime, deadline, isClosed, capacity));

            //then
            assertThat(exception).isInstanceOf(RecruitmentBadRequestException.class);
        }

        @Test
        @DisplayName("예외(RecruitmentBadRequestException): deadline이 null")
        void exceptionWhenDeadlineIsNull() {
            //given
            LocalDateTime nullDeadline = null;

            //when
            Exception exception = catchException(
                () -> new RecruitmentInfo(startTime, endTime, nullDeadline, isClosed, capacity));

            //then
            assertThat(exception).isInstanceOf(RecruitmentBadRequestException.class);
        }

        @Test
        @DisplayName("예외(RecruitmentBadRequestException): startTime이 현재 시간보다 이전")
        void exceptionWhenStartTimeIsBeforeNow() {
            //given
            LocalDateTime beforeStartTime = now.minusMinutes(1);

            //when
            Exception exception = catchException(
                () -> new RecruitmentInfo(beforeStartTime, endTime, deadline, isClosed, capacity));

            //then
            assertThat(exception).isInstanceOf(RecruitmentBadRequestException.class);
        }

        @Test
        @DisplayName("예외(RecruitmentBadRequestException): endTime이 시작 시간보다 이전")
        void exceptionWhenEndTimeIsBeforeStartTime() {
            //given
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startTime = now.plusHours(1);
            LocalDateTime beforeEndTime = startTime.minusSeconds(1);

            //when
            Exception exception = catchException(
                () -> new RecruitmentInfo(startTime, beforeEndTime, deadline, isClosed, capacity));

            //then
            assertThat(exception).isInstanceOf(RecruitmentBadRequestException.class);
        }

        @Test
        @DisplayName("예외(RecruitmentBadRequestException): deadline이 현재 시간 이전")
        void exceptionWhenDeadlineIsBeforeNow() {
            //given
            LocalDateTime deadlineBeforeNow = LocalDateTime.now().minusMinutes(1);

            //when
            Exception exception = catchException(
                () -> new RecruitmentInfo(startTime, endTime, deadlineBeforeNow, isClosed,
                    capacity));

            //then
            assertThat(exception)
                .isInstanceOf(RecruitmentBadRequestException.class);
        }

        @Test
        @DisplayName("예외(RecruitmentBadRequestException): deadline이 봉사 시작 시간 이후")
        void exceptionWhenDeadlineIsAfterStartTime() {
            //given
            LocalDateTime deadlineAfterStartTime = startTime.plusMinutes(1);

            //when
            Exception exception = catchException(() -> new RecruitmentInfo(
                startTime, endTime, deadlineAfterStartTime, isClosed, capacity));

            //then
            assertThat(exception)
                .isInstanceOf(RecruitmentBadRequestException.class);
        }

        @ParameterizedTest
        @CsvSource({
            "0", "100"
        })
        @DisplayName("예외(RecruitmentBadRequestException): capacity가 범위를 넘음")
        void exceptionWhenCapacityOutOfSize(String size) {
            //given
            int capacityOutOfSize = Integer.parseInt(size);

            //when
            Exception exception = catchException(
                () -> new RecruitmentInfo(startTime, endTime, deadline, isClosed,
                    capacityOutOfSize));

            //then
            assertThat(exception)
                .isInstanceOf(RecruitmentBadRequestException.class);
        }
    }

    @Nested
    @DisplayName("closeRecruitment 메서드 호출 시")
    class CloseRecruitmentTest {

        @Test
        @DisplayName("성공")
        void closeRecruitment() {
            //given
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startTime = now.plusHours(1);
            LocalDateTime endTime = startTime.plusMinutes(1);
            LocalDateTime deadline = startTime.minusMinutes(1);
            boolean isClosed = false;
            int capacity = 10;
            RecruitmentInfo recruitmentInfo
                = new RecruitmentInfo(startTime, endTime, deadline, isClosed, capacity);

            //when
            RecruitmentInfo updatedRecruitmentInfo = recruitmentInfo.closeRecruitment();

            //then
            assertThat(updatedRecruitmentInfo.isClosed()).isTrue();
        }
    }
}
