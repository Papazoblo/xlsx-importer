create sequence if not exists download_filter_seq_id;

create table download_filter
(
    id     bigint not null,
    name   varchar(30),
    filter text,
    primary key (id)
);