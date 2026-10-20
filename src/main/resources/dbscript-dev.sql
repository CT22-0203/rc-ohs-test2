-- use oraclepdb container
-- alter session set container = oraclepdb;

-- drop user
drop user rcis;

-- create user
create user rcis identified by Changeme0;

-- grant basic permissions
grant create session to rcis;
grant create table, create view to rcis;
alter user rcis quota unlimited on users;

-- switch to user
alter session set current_schema = rcis;

-- drp db objects
drop table role;
drop table login;
drop table person;

-- create tables
create table role (
                      id              number(19,0)
    ,role           varchar2(128)
    ,description    varchar2(64)
);

create table login (
                       id              number(19,0)
    ,username       varchar2(16)
    ,password       varchar2(64)
    ,role_id        number(19,0)
    ,last_login_date timestamp
);

create table person (
                        id               number(19,0)
    ,first_name      varchar2(16)
    ,last_name       varchar2(64)
    ,login_id        number(19,0)
);

-- create table constraints
alter table role
    add constraint ROLE_PK
        primary key (id);

alter table login
    add constraint LOGIN_PK
        primary key (id);

alter table person
    add constraint PERSON_PK
        primary key (id);

alter table login
    add constraint LOGIN_ROLE_FK
        foreign key (role_id)
            references role (id);

alter table person
    add constraint PERSON_LOGIN_FK
        foreign key (login_id)
            references login (id);

-- create test data
insert into role (id, role, description)
values (1, 'Clinic Staff', 'The account for a clinic staff');

insert into login (id, username, password, role_id, last_login_date)
values (1, 'staff', '1234', 1, null);

insert into person (id, first_name, last_name, login_id)
values (3, 'John', 'Doe', 1);

commit;