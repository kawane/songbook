CREATE TABLE songbook.Songs
(
  id int PRIMARY KEY NOT NULL,
  name text NOT NULL,
  author text,
  creator int NOT NULL,
  contents text
);
ALTER TABLE songbook.Songs ADD CONSTRAINT unique_id UNIQUE (id);
