create table inn_exclude
(
    inn         varchar(12) not null,
    bank        varchar(30) not null,
    create_date timestamp   not null,
    primary key (inn, bank)
);