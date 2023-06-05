create sequence if not exists user_seq_id;

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

create table user_permission
(
    user_id         bigint       not null,
    permission_code varchar(100) not null,
    primary key (user_id, permission_code)
);

insert into users(id, login, password, fio, active, create_at)
values (nextval('user_seq_id'), 'admin', '$2a$04$ifclY8MuC.qt5CC1ImUUmuL0rsgEtVlHhm7ol.BIEzjw.kxmnarim', 'Админ', true,
        current_timestamp);

insert into user_permission(user_id, permission_code)
values ((select id from users where login = 'admin'), 'DOWNLOAD_XLSX') ,
    ((select id from users where login = 'admin'), 'FILE_STORAGE')
    ,
    ((select id from users where login = 'admin'), 'CONTACTS')
    ,
    ((select id from users where login = 'admin'), 'EVENTS')
    ,
    ((select id from users where login = 'admin'), 'COLUMN_NAME')
    ,
    ((select id from users where login = 'admin'), 'DOWNLOADS_PROJECT')
    ,
    ((select id from users where login = 'admin'), 'WEBHOOK_STATUS')
    ,
    ((select id from users where login = 'admin'), 'USERS');