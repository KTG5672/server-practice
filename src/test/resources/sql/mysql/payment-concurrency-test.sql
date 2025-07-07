INSERT INTO users (id, email, password, point)
VALUES ('payment-test-user-1', 'payment-test1@email.com', '1234', 10000);

INSERT INTO concerts (id, name) VALUES (9001, 'test-concert');

INSERT INTO concert_schedules (id, concert_id, place, start_date_time, ticket_open_date_time)
VALUES (9001, 9001, 'test-place', ADDDATE(NOW(), INTERVAL 10 DAY), ADDDATE(NOW(), INTERVAL -1 DAY));

INSERT INTO concert_seats (id, schedule_id, zone, no, price)
VALUES (9001, 9001, 'A', 1, 10000);

INSERT INTO users (id, email, password, point)
VALUES ('payment-test-user-2', 'payment-test2@email.com', '1234', 10000);

INSERT INTO concert_schedules (id, concert_id, place, start_date_time, ticket_open_date_time)
VALUES (9002, 9001, 'test-place', ADDDATE(NOW(), INTERVAL 10 DAY), ADDDATE(NOW(), INTERVAL -1 DAY));

INSERT INTO concert_seats (id, schedule_id, zone, no, price)
VALUES (9002, 9002, 'A', 1, 10000);

INSERT INTO users (id, email, password, point)
VALUES ('payment-test-user-3', 'payment-test3@email.com', '1234', 5000);

INSERT INTO concert_schedules (id, concert_id, place, start_date_time, ticket_open_date_time)
VALUES (9003, 9001, 'test-place', ADDDATE(NOW(), INTERVAL 10 DAY), ADDDATE(NOW(), INTERVAL -1 DAY));

INSERT INTO concert_seats (id, schedule_id, zone, no, price)
VALUES (9003, 9003, 'A', 1, 10000);