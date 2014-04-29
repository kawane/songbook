DROP TABLE  songbook.Songs;
CREATE TABLE songbook.Songs
(
  id int PRIMARY KEY NOT NULL AUTO_INCREMENT,
  name varchar(256) NOT NULL,
  author varchar(256),
  creator int NOT NULL,
  contents text
);
ALTER TABLE songbook.Songs ADD CONSTRAINT unique_id UNIQUE (id);

DROP TABLE  songbook.Users;
CREATE TABLE songbook.Users
(
  id int PRIMARY KEY NOT NULL AUTO_INCREMENT ,
  login varchar(256)  NOT NULL,
  password varchar(256) NOT NULL,
  salt varchar(10) NOT NULL
);
ALTER TABLE songbook.Users ADD CONSTRAINT unique_id UNIQUE (id);
