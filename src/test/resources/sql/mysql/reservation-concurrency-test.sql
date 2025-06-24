INSERT INTO concert_schedules (id, concert_id, place, start_date_time, ticket_open_date_time)
VALUES (2001, 1, 'test-place', ADDDATE(NOW(), INTERVAL 10 DAY), ADDDATE(NOW(), INTERVAL -1 DAY));

INSERT INTO concert_seats (id, schedule_id, zone, no, price)
VALUES (2001, 2001, 'A', 1, 10000);
INSERT INTO concert_seats (id, schedule_id, zone, no, price)
VALUES (2002, 2001, 'A', 2, 10000);
INSERT INTO concert_seats (id, schedule_id, zone, no, price)
VALUES (2003, 2001, 'A', 3, 10000);
INSERT INTO concert_seats (id, schedule_id, zone, no, price)
VALUES (2004, 2001, 'A', 3, 10000);