create user hmda_user;
create database hmda;
grant all privileges on database hmda to hmda_user;

CREATE ROLE hmda_user_rd;
ALTER ROLE hmda_user_rd WITH NOSUPERUSER INHERIT NOCREATEROLE NOCREATEDB LOGIN NOREPLICATION NOBYPASSRLS;

\connect hmda;
create schema if not exists hmda_user authorization hmda_user;
create schema if not exists hmda_beta_user authorization hmda_user;
