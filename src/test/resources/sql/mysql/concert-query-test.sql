INSERT INTO concerts (id, name, start_date, last_date) VALUES (8000, 'test-concert-1', ADDDATE(NOW(), INTERVAL 10 DAY), ADDDATE(NOW(), INTERVAL 12 DAY));

INSERT INTO concert_schedules (id, concert_id, place, start_date_time, ticket_open_date_time)
VALUES (8000, 8000, 'test-place-1', ADDDATE(NOW(), INTERVAL 10 DAY), ADDDATE(NOW(), INTERVAL -1 DAY));

INSERT INTO concerts (id, name, start_date, last_date) VALUES (8001, 'test-concert-2', ADDDATE(NOW(), INTERVAL 10 DAY), ADDDATE(NOW(), INTERVAL 12 DAY));

INSERT INTO concert_schedules (id, concert_id, place, start_date_time, ticket_open_date_time)
VALUES (8001, 8001, 'test-place-2', ADDDATE(NOW(), INTERVAL 10 DAY), ADDDATE(NOW(), INTERVAL -1 DAY));

INSERT INTO concerts (id, name, start_date, last_date) VALUES (8002, 'test-concert-3', ADDDATE(NOW(), INTERVAL 10 DAY), ADDDATE(NOW(), INTERVAL 12 DAY));

INSERT INTO concert_schedules (id, concert_id, place, start_date_time, ticket_open_date_time)
VALUES (8002, 8002, 'test-place-3', ADDDATE(NOW(), INTERVAL 10 DAY), ADDDATE(NOW(), INTERVAL -1 DAY));

INSERT INTO concerts (id, name, start_date, last_date) VALUES (8003, 'test-concert-4', ADDDATE(NOW(), INTERVAL 10 DAY), ADDDATE(NOW(), INTERVAL 12 DAY));

INSERT INTO concert_schedules (id, concert_id, place, start_date_time, ticket_open_date_time)
VALUES (8003, 8003, 'test-place-4', ADDDATE(NOW(), INTERVAL 10 DAY), ADDDATE(NOW(), INTERVAL -1 DAY));

INSERT INTO concerts (id, name, start_date, last_date) VALUES (8004, 'test-concert-5', ADDDATE(NOW(), INTERVAL -5 DAY), ADDDATE(NOW(), INTERVAL -5 DAY));

INSERT INTO concert_schedules (id, concert_id, place, start_date_time, ticket_open_date_time)
VALUES (8004, 8004, 'test-place-5', ADDDATE(NOW(), INTERVAL -5 DAY), ADDDATE(NOW(), INTERVAL -15 DAY));

INSERT INTO concerts (id, name, start_date, last_date) VALUES (8005, 'test-concert-6', ADDDATE(NOW(), INTERVAL 10 DAY), ADDDATE(NOW(), INTERVAL -1 DAY));

INSERT INTO concert_schedules (id, concert_id, place, start_date_time, ticket_open_date_time)
VALUES (8005, 8005, 'test-place-6', ADDDATE(NOW(), INTERVAL -1 DAY), ADDDATE(NOW(), INTERVAL -5 DAY));
