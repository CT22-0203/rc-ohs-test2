-- drop user and database
drop user if exists 'rcis'@'localhost';
drop database if exists rcis;

-- create database
create database rcis;

-- create user
create user 'rcis'@'localhost' identified by 'Changeme0';

-- grant
grant select, insert, update, delete on rcis.* to 'rcis'@'localhost';
grant create, alter, references on rcis.* to 'rcis'@'localhost';
grant create view, show view on rcis.* to 'rcis'@'localhost';
grant create routine, execute on rcis.* to 'rcis'@'localhost';
flush privileges;

use rcis;

-- create tables
create table role (
    id varchar(36) primary key,
    role varchar(128),
    description varchar(64)
);

create table person (
    id varchar(36) primary key,
    first_name varchar(16),
    middle_name varchar(16),
    last_name varchar(64),
    email varchar(128),
    date_of_birth date,
    gender enum('Male', 'Female'),
    address varchar(255),
    contact_number varchar(16)
);

create table login (
    id varchar(36) primary key,
    username varchar(16),
    password varchar(64),
    role_id varchar(36),
    last_login_date timestamp null,
    person_id varchar(36) not null,
    foreign key (role_id) references role (id),
    foreign key (person_id) references person (id)
);

create table department (
    id varchar(36) primary key,
    department_name varchar(30) not null unique
);

create table position (
    id varchar(36) primary key,
    position_name varchar(100) not null,
    category varchar(20) not null,
    department_id varchar(36),
    foreign key (department_id) references department (id)
);

create table personnel (
    id varchar(36) primary key,
    person_id varchar(36) not null unique,
    employee_id varchar(30) unique,
    foreign key (person_id) references person (id)
);

create table personnel_position (
    personnel_id varchar(36) not null,
    position_id varchar(36) not null,
    department_id varchar(36),
    primary key (personnel_id, position_id),
    foreign key (personnel_id) references personnel (id),
    foreign key (position_id) references position (id),
    foreign key (department_id) references department (id)
);

create table section (
    id varchar(36) primary key,
    code varchar(30) not null,
    name varchar(100) not null,
    department_id varchar(36) not null,
    year_level varchar(30) not null,
    period varchar(30),
    academic_year varchar(20) not null,
    adviser_id varchar(36),
    foreign key (department_id) references department (id),
    foreign key (adviser_id) references personnel (id)
);

create table student (
    id varchar(36) primary key,
    person_id varchar(36) not null unique,
    student_number varchar(30) not null unique,
    foreign key (person_id) references person (id)
);

create table student_section (
    id varchar(36) primary key,
    student_id varchar(36) not null,
    section_id varchar(36) not null,
    enrolled_date timestamp not null default current_timestamp,
    foreign key (student_id) references student (id),
    foreign key (section_id) references section (id)
);

create table consultation (
    id varchar(36) primary key,
    patient_id varchar(36) not null,
    staff_id varchar(36) not null,
    record_type varchar(50) not null,
    consultation_date datetime not null,
    complaint text,
    diagnosis text not null,
    treatment text not null,
    action_taken text,
    remarks text,
    foreign key (patient_id) references person (id),
    foreign key (staff_id) references personnel (id),
    check (record_type in ('Check-up', 'Follow-up'))
);

create table medical_record (
    id varchar(36) primary key,
    patient_id varchar(36) not null,
    consultation_id varchar(36),
    medical_conditions text,
    allergies text,
    foreign key (patient_id) references person (id),
    foreign key (consultation_id) references consultation (id)
);

create table medicine_category (
    id varchar(36) primary key,
    category_name varchar(100) not null unique
);

create table medicine (
    id varchar(36) primary key,
    medicine_name varchar(100) not null,
    category_id varchar(36),
    unit varchar(30) not null,
    foreign key (category_id) references medicine_category (id)
);

create table medicine_stock (
    id varchar(36) primary key,
    medicine_id varchar(36) not null,
    personnel_id varchar(36) not null,
    batch_number varchar(50),
    quantity_received int not null,
    expiration_date date,
    status varchar(20) not null,
    foreign key (medicine_id) references medicine (id),
    foreign key (personnel_id) references personnel (id),
    check (quantity_received >= 0)
);

create table prescription (
    id varchar(36) primary key,
    consultation_id varchar(36) not null,
    medicine_stock_id varchar(36) not null,
    personnel_id varchar(36) not null,
    quantity_to_dispense int not null,
    dose varchar(100) not null,
    frequency varchar(100) not null,
    instructions text,
    foreign key (consultation_id) references consultation (id),
    foreign key (medicine_stock_id) references medicine_stock (id),
    foreign key (personnel_id) references personnel (id),
    check (quantity_to_dispense > 0)
);

-- create test data


insert into role (id, role, description) values
('32f71c96-4482-4983-8c92-5f002d4349e9', 'Clinic Staff', 'The account for a clinic staff');

insert into person (id, first_name, middle_name, last_name, email, date_of_birth, gender, address, contact_number) values
('b74de6ca-b83f-4c55-b896-9f517bbeac2f', 'John', 'Michael', 'Doe', 'john.doe@example.com', '2015-05-15', 'Male', '123 Rizal Street, San Pedro, Laguna', '09191223222');

insert into person (id, first_name, middle_name, last_name, email, date_of_birth, gender, address, contact_number) values
('f04cac48-889d-4a62-9a2b-a274406ece20', 'Jowan', 'Carl', 'Quirona', 'jowan123@rocs.edu.ph', '2005-03-14', 'Male', 'Langkaan', '09171234501'),
('ea67a306-d343-4847-b204-c92f2d4cc9e0', 'Allison', null, 'Reyes', 'allison23@rocs.edu.ph', '2004-08-22', 'Female', 'Silang', '09171234502'),
('422d22c4-0c8a-42d1-b683-a209603f364a', 'Ana', 'Ramos', 'Corpuz', 'anaramos@rocs.edu.ph', '1990-05-17', 'Female', 'Tagaytay', '09171234504'),
('c6b4191b-f69a-4107-baf6-90a3f0c8a907', 'Carlos', 'Diaz', 'Gona', 'carlosyolo@rocs.edu.ph', '1985-11-02', 'Male', 'General Trias', '09171234505'),
('43e90036-fb5e-4c31-8950-0b2951ad9d71', 'Elena', null, 'Bautista', 'elenabautista@rocs.edu.ph', '1980-02-11', 'Female', 'Lalaan Dos', '09171234507'),
('ba6c33c3-0833-4e25-b93e-b000e0027416', 'Roberto', null, 'Aquino', 'robertoaquino@rocs.edu.ph', '1978-09-25', 'Male', 'Manila', '09171234508'),
('5a4b605d-21d2-42c2-a93e-86899fcc0c3f', 'Grace', 'Mae', 'Villar', 'villarmae@rocs.edu.ph', '1988-04-03', 'Female', 'Alfonso, Tagaytay', '09171234509'),
('6e00132d-76c0-4c86-82c2-53ba0211aaf0', 'Antonio', null, 'Ferrer', 'ferrerantonio@rocs.edu.ph', '1975-06-12', 'Male', 'Silang', '09171234510');

insert into login (id, username, password, role_id, last_login_date, person_id) values
('8f92ba81-b75b-43de-96d9-e730ad9c0f42', 'staff', '1234', '32f71c96-4482-4983-8c92-5f002d4349e9', null, 'b74de6ca-b83f-4c55-b896-9f517bbeac2f');

insert into department (id, department_name) values
('f2d435c8-0ad4-4c03-abf7-cc64009c7532', 'Junior High School'),
('747a7369-ce7e-45d6-8cfa-4982b01f0963', 'Senior High School'),
('db9dcd69-e128-4326-a059-3691baa9e1df', 'College');

insert into position (id, position_name, category, department_id) values
('279f076d-aafb-49d9-b651-96f46c31222e', 'Nurse', 'Clinic Staff', null),
('ca208b3d-6dbd-41e6-ba91-88ef7d1a12fb', 'Doctor', 'Clinic Staff', null),
('1978aea0-fde4-48ff-bc12-a5aafb5964e6', 'College Teacher', 'RC Employee', 'db9dcd69-e128-4326-a059-3691baa9e1df'),
('b6d5b58b-6482-44a6-a5e9-f8a9d515213c', 'Adviser', 'RC Employee', null),
('e3a3dc8e-4c74-4ef6-8e09-8c58053d9b5e', 'Cashier', 'RC Employee', null),
('0eea2668-57cc-4bcc-8fae-4f22265d25c3', 'Maintenance Personnel', 'Community', null);

insert into personnel (id, person_id, employee_id) values
('79735c7c-5905-4822-a085-d299f3db4522', '422d22c4-0c8a-42d1-b683-a209603f364a', 'EMP-001'),
('64ffaffe-79b5-4690-bbd4-587507b6859c', 'c6b4191b-f69a-4107-baf6-90a3f0c8a907', 'EMP-002'),
('abef97b3-5a0a-400d-b70e-38c6ce10a11a', '43e90036-fb5e-4c31-8950-0b2951ad9d71', 'EMP-003'),
('033a2786-23b9-46ae-96fc-5bf81115669b', 'ba6c33c3-0833-4e25-b93e-b000e0027416', 'EMP-004'),
('7c2f912e-7bb2-4aea-b0f1-3c5b844c4015', '5a4b605d-21d2-42c2-a93e-86899fcc0c3f', 'EMP-005'),
('583ce499-4a13-40b2-86cc-1134e07fa7c8', '6e00132d-76c0-4c86-82c2-53ba0211aaf0', null);

insert into personnel_position (personnel_id, position_id, department_id) values
('79735c7c-5905-4822-a085-d299f3db4522', '279f076d-aafb-49d9-b651-96f46c31222e', null),
('64ffaffe-79b5-4690-bbd4-587507b6859c', 'ca208b3d-6dbd-41e6-ba91-88ef7d1a12fb', null),
('abef97b3-5a0a-400d-b70e-38c6ce10a11a', '1978aea0-fde4-48ff-bc12-a5aafb5964e6', 'db9dcd69-e128-4326-a059-3691baa9e1df'),
('abef97b3-5a0a-400d-b70e-38c6ce10a11a', 'b6d5b58b-6482-44a6-a5e9-f8a9d515213c', 'db9dcd69-e128-4326-a059-3691baa9e1df'),
('033a2786-23b9-46ae-96fc-5bf81115669b', '1978aea0-fde4-48ff-bc12-a5aafb5964e6', 'db9dcd69-e128-4326-a059-3691baa9e1df'),
('033a2786-23b9-46ae-96fc-5bf81115669b', 'b6d5b58b-6482-44a6-a5e9-f8a9d515213c', 'db9dcd69-e128-4326-a059-3691baa9e1df'),
('7c2f912e-7bb2-4aea-b0f1-3c5b844c4015', 'e3a3dc8e-4c74-4ef6-8e09-8c58053d9b5e', null),
('583ce499-4a13-40b2-86cc-1134e07fa7c8', '0eea2668-57cc-4bcc-8fae-4f22265d25c3', null);

insert into section (id, code, name, department_id, year_level, period, academic_year, adviser_id) values
('655e5a20-cf01-4acb-b36f-7a191c3cf385', '401', 'BSIT 401', 'db9dcd69-e128-4326-a059-3691baa9e1df', '2nd Year', '2nd Semester', '2026-2027', 'abef97b3-5a0a-400d-b70e-38c6ce10a11a'),
('521340cc-a469-4b0b-a321-1643f8aaf92c', '601', 'BSIT 601', 'db9dcd69-e128-4326-a059-3691baa9e1df', '3rd Year', '2nd Semester', '2026-2027', '033a2786-23b9-46ae-96fc-5bf81115669b');

insert into student (id, person_id, student_number) values
('bc62f3b3-0d73-4253-8e2b-b35e1ba87c00', 'f04cac48-889d-4a62-9a2b-a274406ece20', 'CT22-0166'),
('bdc3dcf0-8d35-4d55-b9bc-6dd323921527', 'ea67a306-d343-4847-b204-c92f2d4cc9e0', 'CT23-0987');

insert into student_section (id, student_id, section_id) values
('2951e45a-9759-473d-92aa-9521a880e459', 'bc62f3b3-0d73-4253-8e2b-b35e1ba87c00', '655e5a20-cf01-4acb-b36f-7a191c3cf385'),
('606abc21-6d16-4b04-9c33-2c7082a1a181', 'bdc3dcf0-8d35-4d55-b9bc-6dd323921527', '521340cc-a469-4b0b-a321-1643f8aaf92c');

insert into consultation (id, patient_id, staff_id, record_type, consultation_date, complaint, diagnosis, treatment, action_taken, remarks) values
('a451a504-a3d4-4721-bf8d-a18b8c199356', 'f04cac48-889d-4a62-9a2b-a274406ece20', '64ffaffe-79b5-4690-bbd4-587507b6859c', 'Check-up', '2026-10-20 09:30:00', 'Cough, sore throat and fever', 'Bacterial throat infection', 'Antibiotic course and rest', 'Prescribed medicine', 'Return after 7 days'),
('0bd75b66-0afb-4901-88d6-c1e851432727', '6e00132d-76c0-4c86-82c2-53ba0211aaf0', '79735c7c-5905-4822-a085-d299f3db4522', 'Follow-up', '2026-10-22 14:00:00', 'Dizziness', 'Mild dehydration', 'Drink plenty of water and rest', 'Advised rest', 'No medicine needed');

insert into medical_record (id, patient_id, consultation_id, medical_conditions, allergies) values
('4e49eb71-3481-476b-b6ba-5662a48df1e5', 'f04cac48-889d-4a62-9a2b-a274406ece20', 'a451a504-a3d4-4721-bf8d-a18b8c199356', 'Asthma', null),
('669ce09d-4086-4497-a76e-8cb9afe2b635', '6e00132d-76c0-4c86-82c2-53ba0211aaf0', '0bd75b66-0afb-4901-88d6-c1e851432727', 'Hypertension', null);

insert into medicine_category (id, category_name) values
('fde2280e-3564-4ced-87d5-a06454eec0b3', 'Analgesic'),
('e855961e-acf2-4bec-9262-30d1961d40b1', 'Antibiotic');

insert into medicine (id, medicine_name, category_id, unit) values
('ebb3209c-50ea-4fbd-8b7e-be4540ab5b57', 'Paracetamol 500mg', 'fde2280e-3564-4ced-87d5-a06454eec0b3', 'tablet'),
('36c00788-fcfd-485e-8ad3-de08fd49cb76', 'Amoxicillin 500mg', 'e855961e-acf2-4bec-9262-30d1961d40b1', 'capsule');

insert into medicine_stock (id, medicine_id, personnel_id, batch_number, quantity_received, expiration_date, status) values
('cff69d39-caa4-4a79-978b-8927f5b0c1a4', 'ebb3209c-50ea-4fbd-8b7e-be4540ab5b57', '79735c7c-5905-4822-a085-d299f3db4522', 'B-001', 50, '2027-12-31', 'Enough'),
('70aaf305-a7f5-41f1-bd0a-6ad73c042be9', '36c00788-fcfd-485e-8ad3-de08fd49cb76', '79735c7c-5905-4822-a085-d299f3db4522', 'B-002', 50, '2027-06-30', 'Enough');

insert into prescription (id, consultation_id, medicine_stock_id, personnel_id, quantity_to_dispense, dose, frequency, instructions) values
('be1215cb-d1f2-415b-ba58-c8510d6579b7', 'a451a504-a3d4-4721-bf8d-a18b8c199356', 'cff69d39-caa4-4a79-978b-8927f5b0c1a4', '64ffaffe-79b5-4690-bbd4-587507b6859c', 1, '500mg', 'Every 6 hours', 'Take after meals'),
('71525039-9ceb-4b7b-b295-ce151a53678d', 'a451a504-a3d4-4721-bf8d-a18b8c199356', '70aaf305-a7f5-41f1-bd0a-6ad73c042be9', '64ffaffe-79b5-4690-bbd4-587507b6859c', 2, '500mg', '3 times a day', 'Finish all capsules');

-- create join, function, view, procedure
delimiter $$
create function fn_remaining_stock (p_medicine_stock_id varchar(36))
returns int
reads sql data
begin
    declare v_remaining int;

    select ms.quantity_received - coalesce(sum(p.quantity_to_dispense), 0)
      into v_remaining
      from medicine_stock ms
      left join prescription p on p.medicine_stock_id = ms.id
     where ms.id = p_medicine_stock_id
     group by ms.id, ms.quantity_received;

    return v_remaining;
end$$
delimiter ;

select fn_remaining_stock('cff69d39-caa4-4a79-978b-8927f5b0c1a4');
select fn_remaining_stock('70aaf305-a7f5-41f1-bd0a-6ad73c042be9');

create view inventory_view as
select ms.id as medicine_stock_id,
       m.id as medicine_id,
       m.medicine_name,
       mc.category_name,
       m.unit,
       ms.batch_number,
       ms.quantity_received,
       fn_remaining_stock(ms.id) as remaining_quantity,
       ms.expiration_date,
       ms.status
  from medicine_stock ms
  join medicine m on m.id = ms.medicine_id
  left join medicine_category mc on mc.id = m.category_id;

select * from inventory_view;

commit;