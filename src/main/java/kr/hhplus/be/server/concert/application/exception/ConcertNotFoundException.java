package kr.hhplus.be.server.concert.application.exception;

public class ConcertNotFoundException extends RuntimeException {

    public ConcertNotFoundException(Long id) {
        super("Concert not found for id: " + id);
    }
}
