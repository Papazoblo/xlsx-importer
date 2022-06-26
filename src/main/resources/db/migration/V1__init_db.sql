create sequence if not exists file_info_seq_id;
create sequence if not exists contact_seq_id;
create sequence if not exists field_name_variants_seq_id;

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

create table if not exists contact
(
    id        bigint       not null,
    org_name  varchar(300) not null,
    fio       varchar(300) not null,
    phone     varchar(20)  not null,
    inn       varchar(20)  not null,
    ogrn      varchar(20),
    region    varchar(20)  not null,
    address   varchar(300),
    status    varchar(20)  not null,
    create_at timestamp    not null,
    primary key (id)
);

create table if not exists contact_file_info
(
    contact_id bigint not null,
    file_id    bigint not null,
    original   boolean,
    primary key (contact_id, file_id),
    foreign key (contact_id) references contact (id),
    foreign key (file_id) references file_info (id)
);

create table if not exists field_name_variants
(
    id    bigint      not null,
    field varchar(20) not null,
    name  varchar(30) not null,
    primary key (id)
);