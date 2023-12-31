package com.clova.anifriends.domain.applicant.service;

import static com.clova.anifriends.domain.applicant.support.ApplicantFixture.applicant;
import static com.clova.anifriends.domain.applicant.vo.ApplicantStatus.ATTENDANCE;
import static com.clova.anifriends.domain.applicant.vo.ApplicantStatus.NOSHOW;
import static com.clova.anifriends.domain.applicant.vo.ApplicantStatus.PENDING;
import static com.clova.anifriends.domain.applicant.vo.ApplicantStatus.REFUSED;
import static com.clova.anifriends.domain.recruitment.support.fixture.RecruitmentFixture.recruitment;
import static com.clova.anifriends.domain.shelter.support.ShelterFixture.shelter;
import static com.clova.anifriends.domain.volunteer.support.VolunteerFixture.volunteer;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.BDDAssertions.catchException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import com.clova.anifriends.domain.applicant.Applicant;
import com.clova.anifriends.domain.applicant.dto.FindApplicantsResponse;
import com.clova.anifriends.domain.applicant.dto.response.FindApplyingVolunteersResponse;
import com.clova.anifriends.domain.applicant.dto.response.FindApplyingVolunteersResponse.FindApplyingVolunteerResponse;
import com.clova.anifriends.domain.applicant.dto.response.FindApprovedApplicantsResponse;
import com.clova.anifriends.domain.applicant.repository.ApplicantRepository;
import com.clova.anifriends.domain.applicant.repository.response.FindApplicantResult;
import com.clova.anifriends.domain.applicant.repository.response.FindApplyingVolunteerResult;
import com.clova.anifriends.domain.applicant.service.dto.UpdateApplicantAttendanceCommand;
import com.clova.anifriends.domain.applicant.support.ApplicantDtoFixture;
import com.clova.anifriends.domain.applicant.support.ApplicantFixture;
import com.clova.anifriends.domain.notification.repository.ShelterNotificationRepository;
import com.clova.anifriends.domain.notification.repository.VolunteerNotificationRepository;
import com.clova.anifriends.domain.recruitment.Recruitment;
import com.clova.anifriends.domain.recruitment.exception.RecruitmentNotFoundException;
import com.clova.anifriends.domain.recruitment.repository.RecruitmentRepository;
import com.clova.anifriends.domain.recruitment.dto.response.IsAppliedRecruitmentResponse;
import com.clova.anifriends.domain.recruitment.support.fixture.RecruitmentFixture;
import com.clova.anifriends.domain.recruitment.vo.RecruitmentApplicantCount;
import com.clova.anifriends.domain.recruitment.vo.RecruitmentInfo;
import com.clova.anifriends.domain.review.exception.ApplicantNotFoundException;
import com.clova.anifriends.domain.shelter.Shelter;
import com.clova.anifriends.domain.shelter.repository.ShelterRepository;
import com.clova.anifriends.domain.shelter.support.ShelterFixture;
import com.clova.anifriends.domain.volunteer.Volunteer;
import com.clova.anifriends.domain.volunteer.exception.VolunteerNotFoundException;
import com.clova.anifriends.domain.volunteer.repository.VolunteerRepository;
import com.clova.anifriends.domain.volunteer.support.VolunteerFixture;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ApplicantServiceTest {

    @InjectMocks
    ApplicantService applicantService;

    @Mock
    ApplicantRepository applicantRepository;

    @Mock
    RecruitmentRepository recruitmentRepository;

    @Mock
    VolunteerRepository volunteerRepository;

    @Mock
    ShelterNotificationRepository shelterNotificationRepository;

    @Mock
    VolunteerNotificationRepository volunteerNotificationRepository;

    @Mock
    ShelterRepository shelterRepository;

    @Nested
    @DisplayName("registerApplicant 메서드 실행 시")
    class RegisterApplicantTest {

        Shelter shelter = shelter();
        Volunteer volunteer = volunteer();
        Recruitment recruitment = recruitment(shelter);
        RecruitmentInfo recruitmentInfo = new RecruitmentInfo(
            LocalDateTime.now().plusDays(5),
            LocalDateTime.now().plusDays(10),
            LocalDateTime.now().plusDays(3),
            false,
            30
        );


        @Test
        @DisplayName("성공")
        void registerApplicant() {
            // given
            setField(volunteer, "volunteerId", 1L);
            setField(recruitment, "recruitmentId", 1L);
            setField(recruitment, "info", recruitmentInfo);
            given(recruitmentRepository.findByIdPessimistic(anyLong())).willReturn(
                Optional.ofNullable(recruitment));
            given(volunteerRepository.findById(anyLong())).willReturn(
                Optional.ofNullable(volunteer));

            // when
            applicantService.registerApplicant(recruitment.getRecruitmentId(),
                volunteer.getVolunteerId());

            // then
            then(applicantRepository).should().save(any());
            then(shelterNotificationRepository).should().save(any());
        }

        @Test
        @DisplayName("성공: 마지막 모집 인원이 신청한 경우")
        void registerLastApplicant() {
            // given
            setField(volunteer, "volunteerId", 1L);
            setField(recruitment, "recruitmentId", 1L);
            setField(recruitment, "info", recruitmentInfo);
            RecruitmentApplicantCount recruitmentApplicantCount = new RecruitmentApplicantCount(29);
            setField(recruitment, "applicantCount", recruitmentApplicantCount);
            given(recruitmentRepository.findByIdPessimistic(anyLong())).willReturn(
                Optional.ofNullable(recruitment));
            given(volunteerRepository.findById(anyLong())).willReturn(
                Optional.ofNullable(volunteer));

            // when
            applicantService.registerApplicant(recruitment.getRecruitmentId(),
                volunteer.getVolunteerId());

            // then
            then(applicantRepository).should().save(any());
            verify(shelterNotificationRepository, times(2)).save(any());
        }

        @Test
        @DisplayName("예외(ApplicantConflictException): 이미 신청한 경우")
        void throwExceptionWhenAlreadyApplied() {
            // given
            setField(volunteer, "volunteerId", 1L);
            setField(recruitment, "recruitmentId", 1L);
            setField(recruitment, "info", recruitmentInfo);
            given(recruitmentRepository.findByIdPessimistic(anyLong())).willReturn(
                Optional.ofNullable(recruitment));
            given(volunteerRepository.findById(anyLong())).willReturn(
                Optional.ofNullable(volunteer));
            when(applicantRepository.save(any(Applicant.class))).thenThrow(
                DataIntegrityViolationException.class);

            // when
            Exception exception = catchException(() -> applicantService.registerApplicant(
                recruitment.getRecruitmentId(), volunteer.getVolunteerId()));

            // then
            assertThat(exception).isInstanceOf(DataIntegrityViolationException.class);
        }
    }

    @Nested
    @DisplayName("findApplicantsApproved 메서드 실행 시")
    class FindApplicantsApprovedTest {

        @Test
        @DisplayName("성공: 봉사 승인자가 1명 이상인 경우")
        void findApplicantsApproved1() {
            // given
            Recruitment recruitment = recruitment(shelter());
            Volunteer volunteer = volunteer();
            Applicant applicantApproved = applicant(recruitment, volunteer, ATTENDANCE);

            FindApprovedApplicantsResponse response = FindApprovedApplicantsResponse.from(
                List.of(applicantApproved));

            when(applicantRepository.findApprovedApplicants(anyLong(), anyLong()))
                .thenReturn(List.of(applicantApproved));

            // when
            FindApprovedApplicantsResponse result = applicantService.findApprovedApplicants(
                anyLong(), anyLong());

            // then
            assertThat(result).isEqualTo(response);
        }

        @Test
        @DisplayName("성공: 봉사 승인자가 0 명인 경우")
        void findApplicantsApproved2() {
            // given
            FindApprovedApplicantsResponse response = FindApprovedApplicantsResponse.from(
                List.of());

            when(applicantRepository.findApprovedApplicants(anyLong(), anyLong()))
                .thenReturn(List.of());

            // when
            FindApprovedApplicantsResponse result = applicantService.findApprovedApplicants(
                anyLong(), anyLong());

            // then
            assertThat(result).isEqualTo(response);
        }
    }

    @Nested
    @DisplayName("findApplyingVolunteers 실행 시")
    class FindApplyingVolunteersTest {

        @Test
        @DisplayName("성공")
        void findApplyingVolunteers1() {
            // given
            long volunteerId = 1L;
            Volunteer volunteer = VolunteerFixture.volunteer();
            PageRequest pageRequest = PageRequest.of(0, 10);
            List<FindApplyingVolunteerResult> applyingVolunteerResults
                = ApplicantDtoFixture.findApplyingVolunteerResults(3);
            PageImpl<FindApplyingVolunteerResult> findApplyingVolunteerResultPage = new PageImpl<>(
                applyingVolunteerResults);

            given(volunteerRepository.findById(anyLong())).willReturn(Optional.of(volunteer));
            given(applicantRepository.findApplyingVolunteers(any(), any()))
                .willReturn(findApplyingVolunteerResultPage);

            // when
            FindApplyingVolunteersResponse foundApplyingVolunteers
                = applicantService.findApplyingVolunteers(volunteerId, pageRequest);

            // then
            List<FindApplyingVolunteerResponse> applicantsResponse
                = foundApplyingVolunteers.applicants();
            assertThat(applicantsResponse.size()).isEqualTo(3);
            FindApplyingVolunteerResponse response = applicantsResponse.get(0);
            FindApplyingVolunteerResult result = applyingVolunteerResults.get(
                0);
            assertThat(response.shelterId()).isEqualTo(result.getShelterId());
            assertThat(response.recruitmentId()).isEqualTo(result.getRecruitmentId());
            assertThat(response.applicantId()).isEqualTo(result.getApplicantId());
            assertThat(response.recruitmentTitle()).isEqualTo(result.getRecruitmentTitle());
            assertThat(response.shelterName()).isEqualTo(result.getShelterName());
            assertThat(response.applicantStatus()).isEqualTo(result.getApplicantStatus());
            assertThat(response.applicantIsWritedReview())
                .isEqualTo(result.getApplicantIsWritedReview());
            assertThat(response.recruitmentStartTime()).isEqualTo(result.getRecruitmentStartTime());
        }
    }

    @Nested
    @DisplayName("findApplicants 메서드 실행 시")
    class FindApplicantsTest {

        @Test
        @DisplayName("성공")
        void findApplicants() {
            // given
            Shelter shelter = shelter();
            Recruitment recruitment = recruitment(shelter);
            List<FindApplicantResult> applicantResults
                = ApplicantDtoFixture.findApplicantResults(3);

            given(shelterRepository.findById(anyLong())).willReturn(Optional.of(shelter));
            given(recruitmentRepository.findById(anyLong())).willReturn(Optional.of(recruitment));
            given(applicantRepository.findApplicants(recruitment, shelter))
                .willReturn(applicantResults);

            // when
            FindApplicantsResponse findApplicants = applicantService.findApplicants(1L, 1L);

            // then
            assertThat(findApplicants.applicants().size()).isEqualTo(3);
            assertThat(findApplicants.recruitmentCapacity()).isEqualTo(recruitment.getCapacity());

        }
    }

    @Nested
    @DisplayName("updateApplicantAttendance 실행 시")
    class UpdateApplicantAttendance {

        @Test
        @DisplayName("성공")
        void updateApplicantAttendance() {
            // given
            Volunteer volunteer1 = VolunteerFixture.volunteer();
            Volunteer volunteer2 = VolunteerFixture.volunteer();
            Volunteer volunteer3 = VolunteerFixture.volunteer();
            Volunteer volunteer4 = VolunteerFixture.volunteer();

            Shelter shelter = ShelterFixture.shelter();
            Recruitment recruitment = RecruitmentFixture.recruitment(shelter);

            Applicant applicantAttendance = ApplicantFixture.applicant(recruitment, volunteer1,
                ATTENDANCE, 1L);
            Applicant applicantAttendanceToNoShow = ApplicantFixture.applicant(recruitment,
                volunteer2, ATTENDANCE, 2L);
            Applicant applicantNoShowToAttendance = ApplicantFixture.applicant(recruitment,
                volunteer3, NOSHOW, 3L);
            Applicant applicantNoShow = ApplicantFixture.applicant(recruitment, volunteer4,
                NOSHOW, 4L);

            UpdateApplicantAttendanceCommand command1 = new UpdateApplicantAttendanceCommand(
                applicantAttendance.getApplicantId(), true);
            UpdateApplicantAttendanceCommand command2 = new UpdateApplicantAttendanceCommand(
                applicantAttendanceToNoShow.getApplicantId(), false);
            UpdateApplicantAttendanceCommand command3 = new UpdateApplicantAttendanceCommand(
                applicantNoShowToAttendance.getApplicantId(), true);
            UpdateApplicantAttendanceCommand command4 = new UpdateApplicantAttendanceCommand(
                applicantNoShow.getApplicantId(), false);

            List<UpdateApplicantAttendanceCommand> commands = List.of(command1, command2, command3,
                command4);

            // when
            applicantService.updateApplicantAttendance(shelter.getShelterId(),
                recruitment.getRecruitmentId(), commands);

            // then
            verify(applicantRepository, times(1))
                .updateBulkAttendance(shelter.getShelterId(), recruitment.getRecruitmentId(),
                    List.of(applicantAttendance.getApplicantId(),
                        applicantNoShowToAttendance.getApplicantId()), ATTENDANCE);

            verify(applicantRepository, times(1))
                .updateBulkAttendance(shelter.getShelterId(), recruitment.getRecruitmentId(),
                    List.of(applicantAttendanceToNoShow.getApplicantId(),
                        applicantNoShow.getApplicantId()), NOSHOW);

            verify(volunteerNotificationRepository, times(1))
                .saveAll(any(List.class));

        }
    }

    @Nested
    @DisplayName("updateApplicantStatus 실행 시")
    class UpdateApplicantStatus {

        @Test
        @DisplayName("성공")
        void updateApplicantStatus() {
            // given
            Volunteer volunteer = VolunteerFixture.volunteer();
            ReflectionTestUtils.setField(volunteer, "volunteerId", 1L);
            Shelter shelter = ShelterFixture.shelter();
            ReflectionTestUtils.setField(shelter, "shelterId", 1L);
            Recruitment recruitment = RecruitmentFixture.recruitment(shelter);
            ReflectionTestUtils.setField(recruitment, "recruitmentId", 1L);
            Applicant applicant = ApplicantFixture.applicant(recruitment, volunteer, PENDING);
            ReflectionTestUtils.setField(applicant, "applicantId", 1L);
            when(
                applicantRepository.findByApplicantIdAndRecruitment_RecruitmentIdAndRecruitment_Shelter_ShelterId(
                    anyLong(), anyLong(), anyLong())).thenReturn(Optional.of(applicant));

            // when
            applicantService.updateApplicantStatus(applicant.getApplicantId(),
                recruitment.getRecruitmentId(), shelter.getShelterId(), false);

            // then
            assertThat(applicant.getStatus()).isEqualTo(REFUSED);
            then(volunteerNotificationRepository).should().save(any());
        }

        @Test
        @DisplayName("예외(ApplicantNotFoundException): 존재하지 않는 신청인 경우")
        void throwExceptionWhenApplicantNotFound() {
            // given
            Volunteer volunteer = VolunteerFixture.volunteer();
            ReflectionTestUtils.setField(volunteer, "volunteerId", 1L);
            Shelter shelter = ShelterFixture.shelter();
            ReflectionTestUtils.setField(shelter, "shelterId", 1L);
            Recruitment recruitment = RecruitmentFixture.recruitment(shelter);
            ReflectionTestUtils.setField(recruitment, "recruitmentId", 1L);
            Applicant applicant = ApplicantFixture.applicant(recruitment, volunteer, PENDING);
            ReflectionTestUtils.setField(applicant, "applicantId", 1L);
            when(
                applicantRepository.findByApplicantIdAndRecruitment_RecruitmentIdAndRecruitment_Shelter_ShelterId(
                    anyLong(), anyLong(), anyLong())).thenReturn(Optional.empty());

            // when
            Exception exception = catchException(() -> applicantService.updateApplicantStatus(
                applicant.getApplicantId(), recruitment.getRecruitmentId(),
                shelter.getShelterId(), false));

            // then
            assertThat(exception).isInstanceOf(ApplicantNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("isAppliedRecruitment 메서드 호출 시")
    class IsAppliedRecruitmentTest {

        Shelter shelter;
        Volunteer volunteer;
        Recruitment recruitment;

        @BeforeEach
        void setUp() {
            shelter = ShelterFixture.shelter();
            volunteer = VolunteerFixture.volunteer();
            recruitment = RecruitmentFixture.recruitment(shelter);
        }

        @Test
        @DisplayName("성공")
        void isAppliedRecruitment() {
            //given
            given(volunteerRepository.findById(anyLong())).willReturn(Optional.of(volunteer));
            given(recruitmentRepository.findById(anyLong())).willReturn(Optional.of(recruitment));
            given(applicantRepository.existsByVolunteerAndRecruitment(any(), any()))
                .willReturn(true);

            //when
            IsAppliedRecruitmentResponse appliedRecruitment = applicantService.isAppliedRecruitment(
                1L, 1L);

            //then
            Assertions.assertThat(appliedRecruitment.isAppliedRecruitment()).isTrue();
        }

        @Test
        @DisplayName("예외(VolunteerNotFoundException): 봉사자가 존재하지 않음")
        void exceptionWhenVolunteerNotFound() {
            //given
            given(volunteerRepository.findById(anyLong())).willReturn(Optional.empty());

            //when
            Exception exception = Assertions.catchException(
                () -> applicantService.isAppliedRecruitment(1L, 1L));

            //then
            Assertions.assertThat(exception).isInstanceOf(VolunteerNotFoundException.class);
        }

        @Test
        @DisplayName("예외(RecruitmentNotFoundException): 봉사 모집글이 존재하지 않음")
        void exceptionWhenRecruitmentNotFound() {
            //given
            given(volunteerRepository.findById(anyLong())).willReturn(Optional.of(volunteer));
            given(recruitmentRepository.findById(anyLong())).willReturn(Optional.empty());

            //when
            Exception exception = Assertions.catchException(
                () -> applicantService.isAppliedRecruitment(1L, 1L));

            //then
            Assertions.assertThat(exception).isInstanceOf(RecruitmentNotFoundException.class);
        }
    }
}
