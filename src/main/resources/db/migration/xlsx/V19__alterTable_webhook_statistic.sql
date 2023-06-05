alter table webhook_statistic
    add column name varchar(500);

create sequence if not exists notification_chat_seq_id;

create table notification_chat
(
    id      bigint not null,
    chat_id bigint not null,
    primary key (id)
);