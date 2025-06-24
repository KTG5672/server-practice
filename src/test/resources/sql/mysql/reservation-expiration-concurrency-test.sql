INSERT INTO users (id, email, password, point)
VALUES ('reservation-expiration-test-user-1', 'reservation-expiration-test-1@email.com', '1234', 50000);

INSERT INTO users (id, email, password, point)
VALUES ('reservation-expiration-test-user-2', 'reservation-expiration-test-2@email.com', '1234', 50000);

INSERT INTO users (id, email, password, point)
VALUES ('reservation-expiration-test-user-3', 'reservation-expiration-test-3@email.com', '1234', 50000);

INSERT INTO users (id, email, password, point)
VALUES ('reservation-expiration-test-user-4', 'reservation-expiration-test-4@email.com', '1234', 50000);

INSERT INTO users (id, email, password, point)
VALUES ('reservation-expiration-test-user-5', 'reservation-expiration-test-5@email.com', '1234', 50000);

INSERT INTO users (id, email, password, point)
VALUES ('reservation-expiration-test-user-6', 'reservation-expiration-test-6@email.com', '1234', 50000);

INSERT INTO concerts (id, name) VALUES (1001, 'test-concert');

INSERT INTO concert_schedules (id, concert_id, place, start_date_time, ticket_open_date_time)
VALUES (1001, 1001, 'test-place', ADDDATE(NOW(), INTERVAL 10 DAY), ADDDATE(NOW(), INTERVAL -1 DAY));

INSERT INTO concert_seats (id, schedule_id, zone, no, price)
VALUES (1001, 1001, 'A', 1, 10000);
INSERT INTO concert_seats (id, schedule_id, zone, no, price)
VALUES (1002, 1001, 'A', 2, 10000);
INSERT INTO concert_seats (id, schedule_id, zone, no, price)
VALUES (1003, 1001, 'A', 3, 10000);
INSERT INTO concert_seats (id, schedule_id, zone, no, price)
VALUES (1004, 1001, 'B', 4, 5000);
INSERT INTO concert_seats (id, schedule_id, zone, no, price)
VALUES (1005, 1001, 'B', 5, 5000);
INSERT INTO concert_seats (id, schedule_id,zone, no, price)
VALUES (1006, 1001, 'B', 6, 5000);