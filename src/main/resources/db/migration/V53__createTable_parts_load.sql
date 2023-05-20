create sequence if not exists parts_load_seq_id;
create table parts_load
(
    id          bigint      not null,
    date_from   timestamp   not null,
    date_to     timestamp   not null,
    bank        varchar(50) not null,
    count       integer     not null,
    create_date timestamp   not null,
    status      varchar(50) not null,
    primary key (id)
);

create table parts_load_webhook
(
    parts_load_id        bigint not null,
    webhook_statistic_id bigint not null,
    primary key (parts_load_id, webhook_statistic_id),
    foreign key (parts_load_id) references parts_load (id),
    foreign key (webhook_statistic_id) references webhook_statistic (id)
);