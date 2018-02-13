
CREATE TABLE films (
    code        char(5) CONSTRAINT firstkey PRIMARY KEY,
    title       varchar(40) NOT NULL,
    len         interval hour to minute
);

INSERT INTO films VALUES ('MOV01', 'Some movie 1', '1:59');
INSERT INTO films VALUES ('MOV02', 'Some movie 2', '2:59');
INSERT INTO films VALUES ('MOV03', 'Some movie 3', '3:59');
INSERT INTO films VALUES ('MOV04', 'Some movie 4', '4:59');
INSERT INTO films VALUES ('MOV05', 'Some movie 5', '5:59');
INSERT INTO films VALUES ('MOV06', 'Some movie 6', '6:59');

