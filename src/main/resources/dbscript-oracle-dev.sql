-- use oraclepdb container
-- alter session set container = oraclepdb;

-- drop user
drop user rcis;

-- create user
create user rcis identified by Changeme0;

-- grant basic permissions
grant create session to rcis;
grant create table, create view, create procedure to rcis;
alter user rcis quota unlimited on users;

-- switch to user
alter session set current_schema = rcis;

-- create tables
create table role (
     id varchar2(36) not null
    ,role            varchar2(128)
    ,description     varchar2(64)
);

create table login (
     id varchar2(36) not null
    ,username         varchar2(16)
    ,password         varchar2(64)
    ,role_id          varchar2(36)
    ,last_login_date  timestamp null
    ,person_id        varchar2(36) not null
);

create table person (
     id varchar2(36) not null
    ,first_name       varchar2(16)
    ,middle_name      varchar2(16)
    ,last_name        varchar2(64)
    ,email            varchar2(128)
    ,date_of_birth    date
    ,gender           varchar2(16)
    ,address          varchar2(255)
    ,contact_number   varchar2(16)
);

create table department (
     id varchar2(36) not null
    ,department_name      varchar2(30) not null
);

create table position (
     id varchar2(36) not null
    ,position_name        varchar2(100) not null
    ,category             varchar2(20) not null
    ,department_id        varchar2(36)
);

create table personnel (
     id varchar2(36) not null
    ,person_id            varchar2(36) not null
    ,employee_id          varchar2(30)
);

create table personnel_position (
     personnel_id         varchar2(36) not null
    ,position_id          varchar2(36) not null
    ,department_id        varchar2(36)
);

create table section (
     id varchar2(36) not null
    ,code                 varchar2(30) not null
    ,name                 varchar2(100) not null
    ,department_id        varchar2(36) not null
    ,year_level           varchar2(30) not null
    ,period             varchar2(30)
    ,academic_year        varchar2(20) not null
    ,adviser_id           varchar2(36)
);

create table student (
     id varchar2(36) not null
    ,person_id            varchar2(36) not null
    ,student_number       varchar2(30) not null
);

create table student_section (
     id varchar2(36) not null
    ,student_id           varchar2(36) not null
    ,section_id           varchar2(36) not null
    ,enrolled_date        timestamp default systimestamp not null
);

create table consultation (
     id varchar2(36) not null
    ,patient_id           varchar2(36) not null
    ,staff_id             varchar2(36) not null
    ,record_type          varchar2(50) not null
    ,consultation_date    timestamp not null
    ,complaint            varchar2(4000)
    ,diagnosis            varchar2(4000) not null
    ,treatment            varchar2(4000) not null
    ,action_taken         varchar2(4000)
    ,remarks              varchar2(4000)
);

create table medical_record (
     id varchar2(36) not null
    ,patient_id           varchar2(36) not null
    ,consultation_id      varchar2(36)
    ,medical_conditions   varchar2(4000)
    ,allergies            varchar2(4000)
);

create table medicine_category (
     id varchar2(36) not null
    ,category_name        varchar2(100) not null
);

create table medicine (
     id varchar2(36) not null
    ,medicine_name        varchar2(100) not null
    ,category_id          varchar2(36)
    ,unit                 varchar2(30) not null
);

create table medicine_stock (
     id varchar2(36) not null
    ,medicine_id          varchar2(36) not null
    ,personnel_id         varchar2(36) not null
    ,batch_number         varchar2(50)
    ,quantity_received    number(10,0) not null
    ,expiration_date      date
    ,status               varchar2(20) not null
);

create table prescription (
     id varchar2(36) not null
    ,consultation_id      varchar2(36) not null
    ,medicine_stock_id    varchar2(36) not null
    ,personnel_id         varchar2(36) not null
    ,quantity_to_dispense number(19,0) not null
    ,dose                 varchar2(100) not null
    ,frequency            varchar2(100) not null
    ,instructions         varchar2(4000)
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

alter table person
    add constraint PERSON_GENDER_CK
        check (gender in ('Male', 'Female'));

alter table login
    add constraint LOGIN_ROLE_FK
        foreign key (role_id)
            references role (id);

alter table login
    add constraint LOGIN_PERSON_FK
        foreign key (person_id)
            references person (id);

alter table department
    add constraint DEPARTMENT_PK
        primary key (id);

alter table department
    add constraint DEPARTMENT_NAME_UQ
        unique (department_name);

alter table position
    add constraint POSITION_PK
        primary key (id);

alter table position
    add constraint POSITION_DEPARTMENT_FK
        foreign key (department_id)
            references department (id);

alter table personnel
    add constraint PERSONNEL_PK
        primary key (id);

alter table personnel
    add constraint PERSONNEL_PERSON_UQ
        unique (person_id);

alter table personnel
    add constraint PERSONNEL_EMPLOYEE_UQ
        unique (employee_id);

alter table personnel
    add constraint PERSONNEL_PERSON_FK
        foreign key (person_id)
            references person (id);

alter table personnel_position
    add constraint PERSONNEL_POSITION_PK
        primary key (personnel_id, position_id);

alter table personnel_position
    add constraint PERS_POS_PERSONNEL_FK
        foreign key (personnel_id)
            references personnel (id);

alter table personnel_position
    add constraint PERS_POS_POSITION_FK
        foreign key (position_id)
            references position (id);

alter table personnel_position
    add constraint PERS_POS_DEPARTMENT_FK
        foreign key (department_id)
            references department (id);

alter table section
    add constraint SECTION_PK
        primary key (id);

alter table section
    add constraint SECTION_DEPARTMENT_FK
        foreign key (department_id)
            references department (id);

alter table section
    add constraint SECTION_PERSONNEL_FK
        foreign key (adviser_id)
            references personnel (id);

alter table student
    add constraint STUDENT_PK
        primary key (id);

alter table student
    add constraint STUDENT_PERSON_UQ
        unique (person_id);

alter table student
    add constraint STUDENT_NUMBER_UQ
        unique (student_number);

alter table student
    add constraint STUDENT_PERSON_FK
        foreign key (person_id)
            references person (id);

alter table student_section
    add constraint STUDENT_SECTION_PK
        primary key (id);

alter table student_section
    add constraint STUDENT_SECTION_STUDENT_FK
        foreign key (student_id)
            references student (id);

alter table student_section
    add constraint STUDENT_SECTION_SECTION_FK
        foreign key (section_id)
            references section (id);

alter table consultation
    add constraint CONSULTATION_PK
        primary key (id);

alter table consultation
    add constraint CONSULTATION_PERSON_FK
        foreign key (patient_id)
            references person (id);

alter table consultation
    add constraint CONSULTATION_PERSONNEL_FK
        foreign key (staff_id)
            references personnel (id);

alter table consultation
    add constraint CONSULTATION_TYPE_CK
        check (record_type in ('Check-up', 'Follow-up'));

alter table medical_record
    add constraint MEDICAL_RECORD_PK
        primary key (id);

alter table medical_record
    add constraint MEDICAL_RECORD_PERSON_FK
        foreign key (patient_id)
            references person (id);

alter table medical_record
    add constraint MEDICAL_RECORD_CONSULTATION_FK
        foreign key (consultation_id)
            references consultation (id);

alter table medicine_category
    add constraint MEDICINE_CATEGORY_PK
        primary key (id);

alter table medicine_category
    add constraint MEDICINE_CATEGORY_NAME_UQ
        unique (category_name);

alter table medicine
    add constraint MEDICINE_PK
        primary key (id);

alter table medicine
    add constraint MEDICINE_MEDICINE_CATEGORY_FK
        foreign key (category_id)
            references medicine_category (id);

alter table medicine_stock
    add constraint MEDICINE_STOCK_PK
        primary key (id);

alter table medicine_stock
    add constraint MEDICINE_STOCK_MEDICINE_FK
        foreign key (medicine_id)
            references medicine (id);

alter table medicine_stock
    add constraint MEDICINE_STOCK_PERSONNEL_FK
        foreign key (personnel_id)
            references personnel (id);

alter table medicine_stock
    add constraint MEDICINE_STOCK_QTY_CK
        check (quantity_received >= 0);

alter table prescription
    add constraint PRESCRIPTION_PK
        primary key (id);

alter table prescription
    add constraint PRESCRIPTION_CONSULTATION_FK
        foreign key (consultation_id)
            references consultation (id);

alter table prescription
    add constraint PRESCRIPTION_STOCK_FK
        foreign key (medicine_stock_id)
            references medicine_stock (id);

alter table prescription
    add constraint PRESCRIPTION_PERSONNEL_FK
        foreign key (personnel_id)
            references personnel (id);

alter table prescription
    add constraint PRESCRIPTION_QTY_CK
        check (quantity_to_dispense > 0);

-- create test data
insert into role (id, role, description)
values ('32f71c96-4482-4983-8c92-5f002d4349e9', 'Clinic Staff', 'The account for a clinic staff');

insert into person (id, first_name, middle_name, last_name, email, date_of_birth, gender, address, contact_number)
values ('b74de6ca-b83f-4c55-b896-9f517bbeac2f', 'John', 'Michael', 'Doe', 'john.doe@example.com', date '2015-05-15', 'Male', '123 Rizal Street, San Pedro, Laguna', '09191223222');

insert into person (id, first_name, middle_name, last_name, email, date_of_birth, gender, address, contact_number)
values ('f04cac48-889d-4a62-9a2b-a274406ece20', 'Jowan', 'Carl', 'Quirona', 'jowan123@rocs.edu.ph', date '2005-03-14', 'Male', 'Langkaan', '09171234501');
insert into person (id, first_name, middle_name, last_name, email, date_of_birth, gender, address, contact_number)
values ('ea67a306-d343-4847-b204-c92f2d4cc9e0', 'Allison', null, 'Reyes', 'allison23@rocs.edu.ph', date '2004-08-22', 'Female', 'Silang', '09171234502');
insert into person (id, first_name, middle_name, last_name, email, date_of_birth, gender, address, contact_number)
values ('422d22c4-0c8a-42d1-b683-a209603f364a', 'Ana', 'Ramos', 'Corpuz', 'anaramos@rocs.edu.ph', date '1990-05-17', 'Female', 'Tagaytay', '09171234504');
insert into person (id, first_name, middle_name, last_name, email, date_of_birth, gender, address, contact_number)
values ('c6b4191b-f69a-4107-baf6-90a3f0c8a907', 'Carlos', 'Diaz', 'Gona', 'carlosyolo@rocs.edu.ph', date '1985-11-02', 'Male', 'General Trias', '09171234505');
insert into person (id, first_name, middle_name, last_name, email, date_of_birth, gender, address, contact_number)
values ('43e90036-fb5e-4c31-8950-0b2951ad9d71', 'Elena', null, 'Bautista', 'elenabautista@rocs.edu.ph', date '1980-02-11', 'Female', 'Lalaan Dos', '09171234507');
insert into person (id, first_name, middle_name, last_name, email, date_of_birth, gender, address, contact_number)
values ('ba6c33c3-0833-4e25-b93e-b000e0027416', 'Roberto', null, 'Aquino', 'robertoaquino@rocs.edu.ph', date '1978-09-25', 'Male', 'Manila', '09171234508');
insert into person (id, first_name, middle_name, last_name, email, date_of_birth, gender, address, contact_number)
values ('5a4b605d-21d2-42c2-a93e-86899fcc0c3f', 'Grace', 'Mae', 'Villar', 'villarmae@rocs.edu.ph', date '1988-04-03', 'Female', 'Alfonso, Tagaytay', '09171234509');
insert into person (id, first_name, middle_name, last_name, email, date_of_birth, gender, address, contact_number)
values ('6e00132d-76c0-4c86-82c2-53ba0211aaf0', 'Antonio', null, 'Ferrer', 'ferrerantonio@rocs.edu.ph', date '1975-06-12', 'Male', 'Silang', '09171234510');

insert into login (id, username, password, role_id, last_login_date, person_id)
values ('8f92ba81-b75b-43de-96d9-e730ad9c0f42', 'staff', '1234', '32f71c96-4482-4983-8c92-5f002d4349e9', null, 'b74de6ca-b83f-4c55-b896-9f517bbeac2f');

insert into department (id, department_name)
values ('f2d435c8-0ad4-4c03-abf7-cc64009c7532', 'Junior High School');
insert into department (id, department_name)
values ('747a7369-ce7e-45d6-8cfa-4982b01f0963', 'Senior High School');
insert into department (id, department_name)
values ('db9dcd69-e128-4326-a059-3691baa9e1df', 'College');

insert into position (id, position_name, category, department_id)
values ('279f076d-aafb-49d9-b651-96f46c31222e', 'Nurse', 'Clinic Staff', null);
insert into position (id, position_name, category, department_id)
values ('ca208b3d-6dbd-41e6-ba91-88ef7d1a12fb', 'Doctor', 'Clinic Staff', null);
insert into position (id, position_name, category, department_id)
values ('1978aea0-fde4-48ff-bc12-a5aafb5964e6', 'College Teacher', 'RC Employee', 'db9dcd69-e128-4326-a059-3691baa9e1df');
insert into position (id, position_name, category, department_id)
values ('b6d5b58b-6482-44a6-a5e9-f8a9d515213c', 'Adviser', 'RC Employee', null);
insert into position (id, position_name, category, department_id)
values ('e3a3dc8e-4c74-4ef6-8e09-8c58053d9b5e', 'Cashier', 'RC Employee', null);
insert into position (id, position_name, category, department_id)
values ('0eea2668-57cc-4bcc-8fae-4f22265d25c3', 'Maintenance Personnel', 'Community', null);

insert into personnel (id, person_id, employee_id)
values ('79735c7c-5905-4822-a085-d299f3db4522', '422d22c4-0c8a-42d1-b683-a209603f364a', 'EMP-001');
insert into personnel (id, person_id, employee_id)
values ('64ffaffe-79b5-4690-bbd4-587507b6859c', 'c6b4191b-f69a-4107-baf6-90a3f0c8a907', 'EMP-002');
insert into personnel (id, person_id, employee_id)
values ('abef97b3-5a0a-400d-b70e-38c6ce10a11a', '43e90036-fb5e-4c31-8950-0b2951ad9d71', 'EMP-003');
insert into personnel (id, person_id, employee_id)
values ('033a2786-23b9-46ae-96fc-5bf81115669b', 'ba6c33c3-0833-4e25-b93e-b000e0027416', 'EMP-004');
insert into personnel (id, person_id, employee_id)
values ('7c2f912e-7bb2-4aea-b0f1-3c5b844c4015', '5a4b605d-21d2-42c2-a93e-86899fcc0c3f', 'EMP-005');
insert into personnel (id, person_id, employee_id)
values ('583ce499-4a13-40b2-86cc-1134e07fa7c8', '6e00132d-76c0-4c86-82c2-53ba0211aaf0', null);

insert into personnel_position (personnel_id, position_id, department_id)
values ('79735c7c-5905-4822-a085-d299f3db4522', '279f076d-aafb-49d9-b651-96f46c31222e', null);
insert into personnel_position (personnel_id, position_id, department_id)
values ('64ffaffe-79b5-4690-bbd4-587507b6859c', 'ca208b3d-6dbd-41e6-ba91-88ef7d1a12fb', null);
insert into personnel_position (personnel_id, position_id, department_id)
values ('abef97b3-5a0a-400d-b70e-38c6ce10a11a', '1978aea0-fde4-48ff-bc12-a5aafb5964e6', 'db9dcd69-e128-4326-a059-3691baa9e1df');
insert into personnel_position (personnel_id, position_id, department_id)
values ('abef97b3-5a0a-400d-b70e-38c6ce10a11a', 'b6d5b58b-6482-44a6-a5e9-f8a9d515213c', 'db9dcd69-e128-4326-a059-3691baa9e1df');
insert into personnel_position (personnel_id, position_id, department_id)
values ('033a2786-23b9-46ae-96fc-5bf81115669b', '1978aea0-fde4-48ff-bc12-a5aafb5964e6', 'db9dcd69-e128-4326-a059-3691baa9e1df');
insert into personnel_position (personnel_id, position_id, department_id)
values ('033a2786-23b9-46ae-96fc-5bf81115669b', 'b6d5b58b-6482-44a6-a5e9-f8a9d515213c', 'db9dcd69-e128-4326-a059-3691baa9e1df');
insert into personnel_position (personnel_id, position_id, department_id)
values ('7c2f912e-7bb2-4aea-b0f1-3c5b844c4015', 'e3a3dc8e-4c74-4ef6-8e09-8c58053d9b5e', null);
insert into personnel_position (personnel_id, position_id, department_id)
values ('583ce499-4a13-40b2-86cc-1134e07fa7c8', '0eea2668-57cc-4bcc-8fae-4f22265d25c3', null);

insert into section (id, code, name, department_id, year_level, period, academic_year, adviser_id)
values ('655e5a20-cf01-4acb-b36f-7a191c3cf385', '401', 'BSIT 401', 'db9dcd69-e128-4326-a059-3691baa9e1df', '2nd Year', '2nd Semester', '2026-2027', 'abef97b3-5a0a-400d-b70e-38c6ce10a11a');
insert into section (id, code, name, department_id, year_level, period, academic_year, adviser_id)
values ('521340cc-a469-4b0b-a321-1643f8aaf92c', '601', 'BSIT 601', 'db9dcd69-e128-4326-a059-3691baa9e1df', '3rd Year', '2nd Semester', '2026-2027', '033a2786-23b9-46ae-96fc-5bf81115669b');

insert into student (id, person_id, student_number)
values ('bc62f3b3-0d73-4253-8e2b-b35e1ba87c00', 'f04cac48-889d-4a62-9a2b-a274406ece20', 'CT22-0166');
insert into student (id, person_id, student_number)
values ('bdc3dcf0-8d35-4d55-b9bc-6dd323921527', 'ea67a306-d343-4847-b204-c92f2d4cc9e0', 'CT23-0987');

insert into student_section (id, student_id, section_id)
values ('2951e45a-9759-473d-92aa-9521a880e459', 'bc62f3b3-0d73-4253-8e2b-b35e1ba87c00', '655e5a20-cf01-4acb-b36f-7a191c3cf385');
insert into student_section (id, student_id, section_id)
values ('606abc21-6d16-4b04-9c33-2c7082a1a181', 'bdc3dcf0-8d35-4d55-b9bc-6dd323921527', '521340cc-a469-4b0b-a321-1643f8aaf92c');

insert into consultation (id, patient_id, staff_id, record_type, consultation_date, complaint, diagnosis, treatment, action_taken, remarks)
values ('a451a504-a3d4-4721-bf8d-a18b8c199356', 'f04cac48-889d-4a62-9a2b-a274406ece20', '64ffaffe-79b5-4690-bbd4-587507b6859c', 'Check-up', timestamp '2026-10-20 09:30:00', 'Cough, sore throat and fever', 'Bacterial throat infection', 'Antibiotic course and rest', 'Prescribed medicine', 'Return after 7 days');
insert into consultation (id, patient_id, staff_id, record_type, consultation_date, complaint, diagnosis, treatment, action_taken, remarks)
values ('0bd75b66-0afb-4901-88d6-c1e851432727', '6e00132d-76c0-4c86-82c2-53ba0211aaf0', '79735c7c-5905-4822-a085-d299f3db4522', 'Follow-up', timestamp '2026-10-22 14:00:00', 'Dizziness', 'Mild dehydration', 'Drink plenty of water and rest', 'Advised rest', 'No medicine needed');

insert into medical_record (id, patient_id, consultation_id, medical_conditions, allergies)
values ('4e49eb71-3481-476b-b6ba-5662a48df1e5', 'f04cac48-889d-4a62-9a2b-a274406ece20', 'a451a504-a3d4-4721-bf8d-a18b8c199356', 'Asthma', null);
insert into medical_record (id, patient_id, consultation_id, medical_conditions, allergies)
values ('669ce09d-4086-4497-a76e-8cb9afe2b635', '6e00132d-76c0-4c86-82c2-53ba0211aaf0', '0bd75b66-0afb-4901-88d6-c1e851432727', 'Hypertension', null);

insert into medicine_category (id, category_name)
values ('fde2280e-3564-4ced-87d5-a06454eec0b3', 'Analgesic');
insert into medicine_category (id, category_name)
values ('e855961e-acf2-4bec-9262-30d1961d40b1', 'Antibiotic');

insert into medicine (id, medicine_name, category_id, unit)
values ('ebb3209c-50ea-4fbd-8b7e-be4540ab5b57', 'Paracetamol 500mg', 'fde2280e-3564-4ced-87d5-a06454eec0b3', 'tablet');
insert into medicine (id, medicine_name, category_id, unit)
values ('36c00788-fcfd-485e-8ad3-de08fd49cb76', 'Amoxicillin 500mg', 'e855961e-acf2-4bec-9262-30d1961d40b1', 'capsule');

insert into medicine_stock (id, medicine_id, personnel_id, batch_number, quantity_received, expiration_date, status)
values ('cff69d39-caa4-4a79-978b-8927f5b0c1a4', 'ebb3209c-50ea-4fbd-8b7e-be4540ab5b57', '79735c7c-5905-4822-a085-d299f3db4522', 'B-001', 50, date '2027-12-31', 'Enough');
insert into medicine_stock (id, medicine_id, personnel_id, batch_number, quantity_received, expiration_date, status)
values ('70aaf305-a7f5-41f1-bd0a-6ad73c042be9', '36c00788-fcfd-485e-8ad3-de08fd49cb76', '79735c7c-5905-4822-a085-d299f3db4522', 'B-002', 50, date '2027-06-30', 'Enough');

insert into prescription (id, consultation_id, medicine_stock_id, personnel_id, quantity_to_dispense, dose, frequency, instructions)
values ('be1215cb-d1f2-415b-ba58-c8510d6579b7', 'a451a504-a3d4-4721-bf8d-a18b8c199356', 'cff69d39-caa4-4a79-978b-8927f5b0c1a4', '64ffaffe-79b5-4690-bbd4-587507b6859c', 1, '500mg', 'Every 6 hours', 'Take after meals');
insert into prescription (id, consultation_id, medicine_stock_id, personnel_id, quantity_to_dispense, dose, frequency, instructions)
values ('71525039-9ceb-4b7b-b295-ce151a53678d', 'a451a504-a3d4-4721-bf8d-a18b8c199356', '70aaf305-a7f5-41f1-bd0a-6ad73c042be9', '64ffaffe-79b5-4690-bbd4-587507b6859c', 2, '500mg', '3 times a day', 'Finish all capsules');

-- create join, view, index, procedure, function
create function fn_remaining_stock (p_medicine_stock_id in varchar2)
return number
is
    v_remaining number;
begin
    select ms.quantity_received - coalesce(sum(p.quantity_to_dispense), 0)
      into v_remaining
      from medicine_stock ms
      left join prescription p on p.medicine_stock_id = ms.id
     where ms.id = p_medicine_stock_id
     group by ms.id, ms.quantity_received;

    return v_remaining;
end;
/

select fn_remaining_stock('cff69d39-caa4-4a79-978b-8927f5b0c1a4') as remaining from dual;
select fn_remaining_stock('70aaf305-a7f5-41f1-bd0a-6ad73c042be9') as remaining from dual;

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