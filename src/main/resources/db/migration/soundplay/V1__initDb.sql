create sequence if not exists sound_play_journal_seq_id;
create table sound_play_journal
(
    id               bigint      not null,
    request_id       varchar(60) not null unique,
    duration         bigint,
    name             varchar(200),
    user_id          varchar(100),
    utc_millis_start bigint,
    utc_millis_stop  bigint,
    create_at        timestamp   not null,
    update_at        timestamp   not null,
    primary key (id)
);