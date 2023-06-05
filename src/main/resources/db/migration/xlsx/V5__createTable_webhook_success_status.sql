create sequence if not exists webhook_success_status_seq_id;

CREATE TABLE webhook_success_status
(
    id   bigint       not null,
    name varchar(100) NOT NULL,
    primary key (id)
);

insert into webhook_success_status(id, name)
values (nextval('webhook_success_status_seq_id'), 'ВТБ: Счет открыт') ,
    (nextval('webhook_success_status_seq_id'), 'ВТБ: Заявка "Горячий"')
    ,
    (nextval('webhook_success_status_seq_id'), 'ВТБ: Заявка "Теплый"')
    ,
    (nextval('webhook_success_status_seq_id'), 'ВТБ: Заявка "Вотсап"');