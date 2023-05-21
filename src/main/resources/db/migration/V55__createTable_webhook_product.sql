create sequence if not exists bank_product_seq_id;
create table bank_product
(
    id    bigint       not null,
    code  varchar(50)  not null,
    title varchar(200) not null default '',
    bank  varchar(50)  not null,
    primary key (id)
);

insert into bank_product(id, code, title, bank)
values (nextval('bank_product_seq_id'), 'LP_RKO', 'РКО', 'ALFA');
insert into bank_product(id, code, title, bank)
values (nextval('bank_product_seq_id'), 'LP_ACQ_TR', 'Эквайринг (Торговый)', 'ALFA');
insert into bank_product(id, code, title, bank)
values (nextval('bank_product_seq_id'), 'LP_AKASSA', 'Альфа-касса', 'ALFA');
insert into bank_product(id, code, title, bank)
values (nextval('bank_product_seq_id'), 'LP_LOAN_BUS', 'Бизнес кредит - кредит на ЮЛ/ИП', 'ALFA');
insert into bank_product(id, code, title, bank)
values (nextval('bank_product_seq_id'), 'LP_OVER_ADV', 'Овердрафт для новых клиентов банка', 'ALFA');
insert into bank_product(id, code, title, bank)
values (nextval('bank_product_seq_id'), 'LP_SPECACC44', 'Спец. счет 44-Ф3', 'ALFA');
insert into bank_product(id, code, title, bank)
values (nextval('bank_product_seq_id'), 'LP_ACQ_E', 'Эквайринг (Интернет)', 'ALFA');

create table webhook_success_status_product
(
    webhook_success_id bigint not null,
    product_id         bigint not null,
    primary key (webhook_success_id, product_id),
    foreign key (webhook_success_id) references webhook_success_status (id),
    foreign key (product_id) references bank_product (id)
);