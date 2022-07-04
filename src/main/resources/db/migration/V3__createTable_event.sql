create sequence if not exists event_seq_id;

CREATE TABLE event
(
    id          bigint       NOT NULL,
    create_at   timestamp    NOT NULL,
    type        varchar(20)  NOT NULL,
    description varchar(200) NOT NULL,
    file_id     bigint,
    primary key (id)
);

alter table file_info
    add column chat_id bigint not null;