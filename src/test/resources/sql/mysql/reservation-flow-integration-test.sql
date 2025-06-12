INSERT INTO users (id, email, password, point)
VALUES ('test-user', 'test@email.com', '1234', 10000);

INSERT INTO concerts (id, name) VALUES (1, 'test-concert');

INSERT INTO concert_schedules (id, concert_id, place, start_date_time, ticket_open_date_time)
VALUES (2, 1, 'test-place', ADDDATE(NOW(), INTERVAL 10 DAY), ADDDATE(NOW(), INTERVAL -1 DAY));

INSERT INTO concert_seats (id, schedule_id, zone, no, price)
VALUES (4, 2, 'A', 1, 10000);

INSERT INTO concert_seats (id, schedule_id, zone, no, price)
VALUES (5, 2, 'A', 2, 10000);

INSERT INTO concert_seats (id, schedule_id, zone, no, price)
VALUES (6, 2, 'A', 3, 10000);