-- drop user and database
drop user if exists 'rcis'@'localhost';
drop database if exists rcis;

-- create database
create database rcis;

-- create user
create user 'rcis'@'localhost' identified by 'Changeme0';

-- grant basic permissions
grant all privileges on rcis.* to 'rcis'@'localhost';
flush privileges;

use rcis;

-- create tables
create table role (
    id            int primary key auto_increment,
    role          varchar(128),
    description   varchar(64)
);

create table login (
    id               int primary key auto_increment,
    username         varchar(16),
    password         varchar(64),
    role_id          int,
    last_login_date  timestamp null,
    foreign key (role_id) references role (id)
);

create table person (
    id              int primary key auto_increment,
    first_name      varchar(16),
    middle_name     varchar(16),
    last_name       varchar(64),
    email           varchar(128),
    date_of_birth   date,
    gender          varchar(16),
    address         varchar(255),
    contact_number  varchar(16),
    login_id        int,
    foreign key (login_id) references login (id)
);

-- create test data
insert into role (role, description)
values ('Clinic Staff', 'The account for a clinic staff');

insert into login (username, password, role_id, last_login_date)
values ('staff', '1234', 1, null);


insert into person (first_name, middle_name, last_name, email, date_of_birth, gender, address, contact_number, login_id)
values ('John', 'Michael', 'Doe', 'john.doe@example.com', '2015-05-15', 'Male', '123 Rizal Street, San Pedro, Laguna', '09191223222', 1);



