--comment: create the routing schema and lock down default access at the database layer
CREATE SCHEMA IF NOT EXISTS routing;

REVOKE ALL ON SCHEMA routing FROM PUBLIC;