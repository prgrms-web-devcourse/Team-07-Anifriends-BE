package com.clova.anifriends.domain.applicant;

import com.clova.anifriends.domain.applicant.exception.ApplicantBadRequestException;
import com.clova.anifriends.domain.applicant.exception.ApplicantCanNotApplyException;
import com.clova.anifriends.domain.applicant.vo.ApplicantStatus;
import com.clova.anifriends.domain.common.BaseTimeEntity;
import com.clova.anifriends.domain.recruitment.Recruitment;
import com.clova.anifriends.domain.review.Review;
import com.clova.anifriends.domain.volunteer.Volunteer;
import com.clova.anifriends.global.exception.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "applicant",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"recruitment_id", "volunteer_id"})
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Applicant extends BaseTimeEntity {

    @Id
    @Column(name = "applicant_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long applicantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruitment_id")
    private Recruitment recruitment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "volunteer_id")
    private Volunteer volunteer;

    // todo: 제거 필요
    @OneToOne(mappedBy = "applicant", fetch = FetchType.LAZY)
    private Review review;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ApplicantStatus status = ApplicantStatus.PENDING;

    public Applicant(
        Recruitment recruitment,
        Volunteer volunteer
    ) {
        validateRecruitment(recruitment);
        validateVolunteer(volunteer);
        validateApplicantCount(recruitment);
        recruitment.increaseApplicantCount();
        this.recruitment = recruitment;
        this.volunteer = volunteer;
        recruitment.addApplicant(this);
        volunteer.addApplicant(this);
    }

    private void validateRecruitment(Recruitment recruitment) {
        if (recruitment == null) {
            throw new ApplicantBadRequestException("봉사는 필수 입력 항목입니다.");
        }
        if (recruitment.isClosed() || recruitment.getDeadline().isBefore(LocalDateTime.now())) {
            throw new ApplicantCanNotApplyException(ErrorCode.CONCURRENCY, "모집이 마감된 봉사입니다.");
        }
    }

    private void validateVolunteer(Volunteer volunteer) {
        if (volunteer == null) {
            throw new ApplicantBadRequestException("봉사자는 필수 입력 항목입니다.");
        }
    }

    private void validateApplicantCount(Recruitment recruitment) {
        if (recruitment.isFullApplicants()) {
            throw new ApplicantCanNotApplyException(ErrorCode.CONCURRENCY, "모집 인원이 초과되었습니다.");
        }
    }

    public boolean isAttendance() {
        return this.status == ApplicantStatus.ATTENDANCE;
    }

    public void registerReview(Review review) {
        this.review = review;
        this.volunteer.increaseReviewCount();
    }

    public void increaseTemperature(int temperature) {
        this.volunteer.increaseTemperature(temperature);
    }

    public void updateApplicantStatus(Boolean isApproved) {
        if (Objects.nonNull(isApproved)) {
            this.status = isApproved ? ApplicantStatus.ATTENDANCE : ApplicantStatus.REFUSED;
        }
    }

    public ApplicantStatus getStatus() {
        return status;
    }

    public Recruitment getRecruitment() {
        return recruitment;
    }

    public Volunteer getVolunteer() {
        return volunteer;
    }

    public Long getApplicantId() {
        return applicantId;
    }

    public Review getReview() {
        return review;
    }
}
