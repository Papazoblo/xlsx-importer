create sequence if not exists city_seq_id;

create table city
(
    id            bigint not null,
    name          varchar(50),
    raiffeisen_id varchar(50),
    primary key (id)
);