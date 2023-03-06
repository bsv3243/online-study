package seong.onlinestudy.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seong.onlinestudy.domain.Study;
import seong.onlinestudy.dto.StudyDto;
import seong.onlinestudy.exception.DuplicateStudyException;
import seong.onlinestudy.repository.StudyRepository;
import seong.onlinestudy.request.StudyCreateRequest;
import seong.onlinestudy.request.StudySearchCond;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StudyService {

    private final StudyRepository studyRepository;

    @Transactional
    public Long createStudy(StudyCreateRequest createStudyRequest) {
        studyRepository.findByName(createStudyRequest.getName())
                .ifPresent(study -> {
                    throw new DuplicateStudyException("이미 존재하는 스터디입니다.");
                });

        Study study = Study.createStudy(createStudyRequest);
        studyRepository.save(study);

        return study.getId();
    }

    public Page<StudyDto> getStudies(StudySearchCond searchCond) {
        PageRequest request = PageRequest.of(searchCond.getPage(), searchCond.getSize());
        Page<Study> studies = studyRepository.findAllByNameContains(searchCond.getName(), request);

        Page<StudyDto> studyDtos = studies.map(study -> StudyDto.from(study));

        return studyDtos;
    }
}
