INSERT INTO users (id, email, password, point)
VALUES ('test-user-1', 'test1@email.com', '1234', 50000);

INSERT INTO users (id, email, password, point)
VALUES ('test-user-2', 'test2@email.com', '1234', 50000);

INSERT INTO users (id, email, password, point)
VALUES ('test-user-3', 'test3@email.com', '1234', 50000);

INSERT INTO users (id, email, password, point)
VALUES ('test-user-4', 'test4@email.com', '1234', 50000);

INSERT INTO users (id, email, password, point)
VALUES ('test-user-5', 'test5@email.com', '1234', 50000);

INSERT INTO users (id, email, password, point)
VALUES ('test-user-6', 'test6@email.com', '1234', 50000);

INSERT INTO concerts (id, name) VALUES (1, 'test-concert');

INSERT INTO concert_schedules (id, concert_id, place, start_date_time, ticket_open_date_time)
VALUES (2, 1, 'test-place', ADDDATE(NOW(), INTERVAL 10 DAY), ADDDATE(NOW(), INTERVAL -1 DAY));

INSERT INTO concert_seats (id, schedule_id, zone, no, price)
VALUES (4, 2, 'A', 1, 10000);

INSERT INTO concert_seats (id, schedule_id, zone, no, price)
VALUES (5, 2, 'A', 2, 10000);

INSERT INTO concert_seats (id, schedule_id, zone, no, price)
VALUES (6, 2, 'A', 3, 10000);

INSERT INTO concert_seats (id, schedule_id, zone, no, price)
VALUES (7, 2, 'B', 4, 5000);

INSERT INTO concert_seats (id, schedule_id, zone, no, price)
VALUES (8, 2, 'B', 5, 5000);

INSERT INTO concert_seats (id, schedule_id,zone, no, price)
VALUES (9, 2, 'B', 6, 5000);