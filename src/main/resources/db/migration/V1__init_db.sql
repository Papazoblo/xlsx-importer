create sequence if not exists file_info_seq_id;

CREATE TABLE file_info
(
    id         bigint       NOT NULL,
    name       varchar(100) NOT NULL,
    size       bigint,
    type       varchar(100),
    unique_id  varchar(100) NOT NULL,
    tg_file_id varchar(100) NOT NULL,
    hash       varchar(100) not null,
    status     varchar(20)  NOT NULL,
    path       varchar(200) not null,
    deleted    boolean default false,
    create_at  timestamp    not null,
    primary key (id)
);