
ALTER ROLE postgres WITH ENCRYPTED PASSWORD '111';

CREATE USER greetgo_secure WITH ENCRYPTED PASSWORD '111';
CREATE DATABASE greetgo_secure WITH OWNER greetgo_secure;
GRANT ALL ON DATABASE greetgo_secure TO greetgo_secure;
