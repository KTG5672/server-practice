package kr.hhplus.be.server.concert.application;

import kr.hhplus.be.server.concert.application.dto.ConcertCreateCommand;
import kr.hhplus.be.server.concert.entity.Concert;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 콘서트 서비스
 */
@Service
public class ConcertService {

    private final ConcertRepository concertRepository;

    public ConcertService(ConcertRepository concertRepository) {
        this.concertRepository = concertRepository;
    }

    /**
     * 콘서트를 생성하고 저장
     * - 콘서트 생성,저장 시 캐시 이름 'concerts' 데이터 초기화
     * @param createCommand 생성할 콘서트 정보
     * @return Long 콘서트 식별자
     */
    @Transactional
    @CacheEvict(value = "concerts", allEntries = true)
    public Long createConcert(ConcertCreateCommand createCommand) {
        Concert concert = ConcertCreateCommand.toNewEntity(createCommand);
        Concert saved = concertRepository.save(concert);
        return saved.getId();
    }

}
