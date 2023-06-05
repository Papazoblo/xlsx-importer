alter table webhook_statistic
    add column opening_id varchar(100);

alter table file_info_bank
    add column download_status varchar(50);

update file_info_bank
set download_status = 'SUCCESS'
where 1 = 1;


alter table contact
    add column file_info_bank_id bigint;
alter table contact
    add column check_status varchar(50);
alter table contact
    add column opening_request_id bigint;
alter table contact
    add foreign key (file_info_bank_id)
        references file_info_bank (id);

create sequence if not exists opening_request_seq_id;

create table opening_request
(
    id                bigint       not null,
    request_id        varchar(100) not null,
    status            varchar(50)  not null,
    file_info_bank_id bigint       not null,
    primary key (id),
    foreign key (file_info_bank_id) references file_info_bank (id)
);

alter table contact
    add foreign key (opening_request_id)
        references opening_request (id);