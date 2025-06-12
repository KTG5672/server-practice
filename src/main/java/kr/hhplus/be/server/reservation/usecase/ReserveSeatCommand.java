package kr.hhplus.be.server.reservation.usecase;

public record ReserveSeatCommand(String userId, Long seatId) {}
