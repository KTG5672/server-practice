package kr.hhplus.be.server.concert.application.exception;

public class NotValidConcertException extends RuntimeException {

    public NotValidConcertException(Long concertId) {
        super("The concert id " + concertId + " is not valid");
    }

}
