CREATE TABLE IF NOT EXISTS users (
  id          TEXT UNIQUE NOT NULL PRIMARY KEY,
  displayName TEXT UNIQUE NOT NULL,
  email       TEXT UNIQUE NOT NULL,
  pin         TEXT
  pinExpire   INTEGER
);