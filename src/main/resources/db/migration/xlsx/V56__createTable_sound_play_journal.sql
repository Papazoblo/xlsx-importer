create sequence if not exists sound_play_journal_seq_id;
create table sound_play_journal
(
    id               bigint      not null,
    request_id       varchar(50) not null,
    duration         bigint,
    name             varchar(200),
    utc_millis_start bigint,
    utc_millis_stop  bigint,
    create_at        timestamp   not null,
    update_at        timestamp   not null,
    primary key (id)
);