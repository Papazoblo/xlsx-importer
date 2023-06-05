create sequence if not exists auto_load_seq_id;
create sequence if not exists auto_load_log_seq_id;

create table auto_load
(
    id             bigint        not null,
    interval       int           not null,
    period         varchar(100)  not null,
    filter         varchar(1000) not null,
    last_load_date timestamp     not null,
    create_date    timestamp     not null,
    enabled        boolean       not null,
    deleted        boolean       not null,
    primary key (id)
);

create table auto_load_log
(
    id           bigint    not null,
    auto_load_id bigint    not null,
    load_date    timestamp not null,
    loaded_count bigint    not null,
    primary key (id),
    foreign key (auto_load_id) references auto_load (id)
);