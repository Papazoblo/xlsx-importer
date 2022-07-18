create sequence if not exists webhook_statistic_seq_id;

create table webhook_statistic
(
    id                bigint      not null,
    inn               varchar(15) not null,
    webhook_status_id bigint      not null,
    status            varchar(15) not null,
    create_at         timestamp   not null,
    primary key (id),
    foreign key (webhook_status_id) references webhook_success_status (id)
);