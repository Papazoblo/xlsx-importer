create sequence if not exists file_info_bank_seq_id;

create table file_info_bank
(
    id           bigint      not null,
    file_info_id bigint      not null,
    bank         varchar(50) not null,
    project_id   bigint      not null,
    primary key (id),
    foreign key (file_info_id) references file_info (id)
);

alter table contact
    add column bank_name varchar(50);

update contact
set bank_name = 'VTB'
where 1 = 1;

alter table contact rename column address to city;

alter table webhook_success_status
    add column bank_name varchar(50);

update webhook_success_status
set bank_name = 'VTB'
where 1 = 1;

alter table webhook_statistic
    add column bank_name varchar(50);

update webhook_statistic
set bank_name = 'VTB'
where 1 = 1;

alter table webhook_statistic
    add email varchar(200);
