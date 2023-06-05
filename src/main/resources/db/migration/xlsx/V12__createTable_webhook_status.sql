create sequence if not exists webhook_status_seq_id;

create table webhook_status
(
    id   bigint       not null,
    name varchar(100) not null,
    primary key (id)
);

insert into webhook_status(id, name)
values (-1, 'Не прозванивался');

alter table contact
    add column webhook_status_id bigint;
