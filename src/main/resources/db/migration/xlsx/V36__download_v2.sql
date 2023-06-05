--добавляем вебхуки для создания заявки в общую таблицу
insert into webhook_status(id, name)
    (select distinct nextval('webhook_status_seq_id'), ss.name
     from webhook_success_status ss
              left join webhook_status s on s.name = ss.name
     where s.name is null);

--добавляем новые столбцы
alter table webhook_success_status
    add webhook_id bigint;
alter table webhook_success_status
    add type varchar(50);

--переносим данные
update webhook_success_status ss
set webhook_id = (select s.id from webhook_status s where s.name = ss.name),
    type = 'CREATE_REQUEST'
where webhook_id is null;

create unique index webhook_success_status_webhook_id_id_uindex
    on webhook_success_status (webhook_id, bank_name, type);

--удаляем ненужный столбец name
alter table webhook_success_status
    drop column name;

create table contact_new
(
    id          bigint      not null,
    org_name    varchar(1000),
    name        varchar(100),
    surname     varchar(100),
    middle_name varchar(100),
    phone       varchar(20),
    inn         varchar(20) not null unique,
    ogrn        varchar(20),
    region      varchar(500),
    city        varchar(1000),
    create_at   timestamp,
    primary key (id)
);

create sequence if not exists contact_bank_actuality_seq_id;

create table contact_bank_actuality
(
    id                bigint       not null,
    webhook_status_id bigint       not null,
    actuality         varchar(100) not null,
    bank_name         varchar(50)  not null,
    error_count       integer default 0,
    contact_id        bigint       not null,
    primary key (id),
    foreign key (contact_id) references contact_new (id),
    foreign key (webhook_status_id) references webhook_status (id)
);

-- alter table webhook_status
--     add type varchar(10) default 'MINOR';

create sequence if not exists webhook_status_map_seq_id;

create table webhook_status_map
(
    id             bigint       not null,
    from_status_id bigint       not null,
    priority       integer      not null,
    actuality      varchar(100) not null,
    bank_name      varchar(100) not null,
    error_count    varchar(10),
    primary key (id),
    foreign key (from_status_id) references webhook_status (id),
    unique (bank_name, from_status_id, error_count)
);

insert into webhook_status(id, name)
values (-2, 'Отказ_Недозвон');

create sequence if not exists contact_download_info_seq_id;
create table contact_download_info
(
    id                bigint    not null,
    contact_id        bigint    not null,
    file_info_bank_id bigint    not null,
    request_id        bigint,
    check_status      varchar(50),
    create_at         timestamp not null,
    primary key (id),
    foreign key (contact_id) references contact_new (id),
    foreign key (file_info_bank_id) references file_info_bank (id),
    foreign key (request_id) references opening_request (id)
);

