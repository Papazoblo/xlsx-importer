create sequence if not exists user_seq_id;
create sequence if not exists permission_seq_id;

CREATE TABLE users
(
    id        bigint       not null,
    login     varchar(100) not null unique,
    password  varchar(200) not null,
    fio       varchar(300) NOT NULL,
    active    boolean default true,
    create_at timestamp    not null,
    primary key (id)
);

CREATE TABLE permission
(
    id   bigint       not null,
    code varchar(100) not null,
    name varchar(100) NOT NULL,
    primary key (id)
);

create table user_permission
(
    user_id       bigint not null,
    permission_id bigint not null,
    primary key (user_id, permission_id)
);