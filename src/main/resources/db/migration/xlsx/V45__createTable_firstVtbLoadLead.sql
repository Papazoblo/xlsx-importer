create table first_load_lead
(
    inn         varchar(12) not null,
    create_date timestamp   not null,
    primary key (inn)
);

alter table file_info
    add column is_first_vtb_load_lead boolean;
alter table file_info
    add column is_not_load_from_vtb_dedublicate boolean;