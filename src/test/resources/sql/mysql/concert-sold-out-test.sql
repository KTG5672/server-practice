INSERT INTO users (id, email, password, point)
VALUES ('concert-sold-out-test-user-1', 'concert-sold-out-test1@email.com', '1234', 1000000);

INSERT INTO concerts (id, name, start_date, last_date) VALUES (10000, 'test-concert-1', ADDDATE(NOW(), INTERVAL 10 DAY), ADDDATE(NOW(), INTERVAL 12 DAY));

INSERT INTO concert_schedules (id, concert_id, place, start_date_time, ticket_open_date_time)
VALUES (10000, 10000, 'test-place-1', ADDDATE(NOW(), INTERVAL 10 DAY), ADDDATE(NOW(), INTERVAL -1 DAY));

INSERT INTO concert_seats (id, schedule_id, zone, no, price)
VALUES (10001, 10000, 'A', 1, 10000);

INSERT INTO concert_seats (id, schedule_id, zone, no, price)
VALUES (10002, 10000, 'B', 1, 10000);

INSERT INTO concert_seats (id, schedule_id, zone, no, price)
VALUES (10003, 10000, 'C', 1, 10000);

-- 미매진 테스트 케이스
INSERT INTO concerts (id, name, start_date, last_date) VALUES (10001, 'test-concert-1', ADDDATE(NOW(), INTERVAL 10 DAY), ADDDATE(NOW(), INTERVAL 12 DAY));

INSERT INTO concert_schedules (id, concert_id, place, start_date_time, ticket_open_date_time)
VALUES (10001, 10001, 'test-place-1', ADDDATE(NOW(), INTERVAL 10 DAY), ADDDATE(NOW(), INTERVAL -1 DAY));

INSERT INTO concert_seats (id, schedule_id, zone, no, price)
VALUES (10004, 10001, 'A', 1, 10000);

INSERT INTO concert_seats (id, schedule_id, zone, no, price)
VALUES (10005, 10001, 'B', 1, 10000);
