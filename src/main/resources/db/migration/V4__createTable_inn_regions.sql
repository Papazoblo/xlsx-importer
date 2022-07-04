create sequence if not exists inn_region_seq_id;

CREATE TABLE inn_region
(
    id   bigint       not null,
    code varchar(3)   NOT NULL,
    name varchar(100) NOT NULL,
    primary key (id)
);

insert into inn_region(id, code, name)
values (nextval('inn_region_seq_id'), '01', 'Республика Адыгея') ,
    (nextval('inn_region_seq_id'), '02', 'Республика Башкортостан')
    ,
    (nextval('inn_region_seq_id'), '03', 'Республика Бурятия')
    ,
    (nextval('inn_region_seq_id'), '04', 'Республика Алтай')
    ,
    (nextval('inn_region_seq_id'), '05', 'Республика Дагестан')
    ,
    (nextval('inn_region_seq_id'), '06', 'Ингушская область')
    ,
(nextval('inn_region_seq_id'), '07', 'Кабардино-Балкарская Республика')
,
    (nextval('inn_region_seq_id'), '08', 'Республика Калмыкия')
    ,
(nextval('inn_region_seq_id'), '09', 'Карачаево-Черкесская Республика')
,
    (nextval('inn_region_seq_id'), '10', 'Республика Карелия')
    ,
    (nextval('inn_region_seq_id'), '11', 'Омская область')
    ,
    (nextval('inn_region_seq_id'), '12', 'Республика Марий Эл')
    ,
    (nextval('inn_region_seq_id'), '13', 'Республика Мордовия')
    ,
    (nextval('inn_region_seq_id'), '14', 'Республика Саха')
    ,
    (nextval('inn_region_seq_id'), '15', 'Республика Северная Осетия')
    ,
    (nextval('inn_region_seq_id'), '16', 'Республика Татарстан')
    ,
    (nextval('inn_region_seq_id'), '17', 'Республика Тыва')
    ,
    (nextval('inn_region_seq_id'), '18', 'Удмуртская Республика')
    ,
    (nextval('inn_region_seq_id'), '19', 'Республика Хакасия')
    ,
    (nextval('inn_region_seq_id'), '22', 'Алтайский край')
    ,
    (nextval('inn_region_seq_id'), '23', 'Краснодарский край')
    ,
    (nextval('inn_region_seq_id'), '24', 'Красноярский край')
    ,
    (nextval('inn_region_seq_id'), '25', 'Приморский край')
    ,
    (nextval('inn_region_seq_id'), '26', 'Ставропольский край')
    ,
    (nextval('inn_region_seq_id'), '27', 'Хабаровский край')
    ,
    (nextval('inn_region_seq_id'), '28', 'Амурская область')
    ,
    (nextval('inn_region_seq_id'), '29', 'Архангельская область')
    ,
    (nextval('inn_region_seq_id'), '30', 'Астраханская область')
    ,
    (nextval('inn_region_seq_id'), '31', 'Белгородская область')
    ,
    (nextval('inn_region_seq_id'), '32', 'Брянская область')
    ,
    (nextval('inn_region_seq_id'), '46', 'Курская область')
    ,
    (nextval('inn_region_seq_id'), '47', 'Ленинградская область')
    ,
    (nextval('inn_region_seq_id'), '48', 'Липецкая область')
    ,
    (nextval('inn_region_seq_id'), '49', 'Магаданская область')
    ,
    (nextval('inn_region_seq_id'), '50', 'Московская область')
    ,
    (nextval('inn_region_seq_id'), '51', 'Мурманская область')
    ,
    (nextval('inn_region_seq_id'), '52', 'Нижегородская область')
    ,
    (nextval('inn_region_seq_id'), '53', 'Новгородская область')
    ,
    (nextval('inn_region_seq_id'), '54', 'Новосибирская область')
    ,
    (nextval('inn_region_seq_id'), '55', 'Омская область')
    ,
    (nextval('inn_region_seq_id'), '56', 'Оренбургская область')
    ,
    (nextval('inn_region_seq_id'), '57', 'Орловская область')
    ,
    (nextval('inn_region_seq_id'), '70', 'Томская область')
    ,
    (nextval('inn_region_seq_id'), '71', 'Тульская область')
    ,
    (nextval('inn_region_seq_id'), '72', 'Тюменская область')
    ,
    (nextval('inn_region_seq_id'), '73', 'Ульяновская область')
    ,
    (nextval('inn_region_seq_id'), '74', 'Челябинская область')
    ,
    (nextval('inn_region_seq_id'), '75', 'Читинская область')
    ,
    (nextval('inn_region_seq_id'), '76', 'Ярославская область')
    ,
    (nextval('inn_region_seq_id'), '77', 'Москва')
    ,
    (nextval('inn_region_seq_id'), '78', 'Санкт-Петербург')
    ,
    (nextval('inn_region_seq_id'), '79', 'Еврейская АО')
    ,
    (nextval('inn_region_seq_id'), '80', 'Агинский Бурятский АО')
    ,
    (nextval('inn_region_seq_id'), '33', 'Владимирская область')
    ,
    (nextval('inn_region_seq_id'), '81', 'Коми-Пермяцкий АО')
    ,
    (nextval('inn_region_seq_id'), '55', 'Республика Коми')
    ,
    (nextval('inn_region_seq_id'), '34', 'Волгоградская область')
    ,
    (nextval('inn_region_seq_id'), '58', 'Пензенская область')
    ,
    (nextval('inn_region_seq_id'), '82', 'Корякский АО')
    ,
    (nextval('inn_region_seq_id'), '35', 'Вологодская область')
    ,
    (nextval('inn_region_seq_id'), '59', 'Пермская область')
    ,
    (nextval('inn_region_seq_id'), '83', 'Ненецкий АО')
    ,
    (nextval('inn_region_seq_id'), '36', 'Воронежская область')
    ,
    (nextval('inn_region_seq_id'), '60', 'Псковская область')
    ,
(nextval('inn_region_seq_id'), '84', 'Таймырский (Долгано- Ненецкий) АО')
,
    (nextval('inn_region_seq_id'), '37', 'Ивановская область')
    ,
    (nextval('inn_region_seq_id'), '61', 'Ростовская область')
    ,
    (nextval('inn_region_seq_id'), '38', 'Иркутская область')
    ,
    (nextval('inn_region_seq_id'), '62', 'Рязанская область')
    ,
    (nextval('inn_region_seq_id'), '85', 'Усть-Ордынский Бурятский АО')
    ,
    (nextval('inn_region_seq_id'), '39', 'Калининградская область')
    ,
    (nextval('inn_region_seq_id'), '63', 'Самарская область')
    ,
    (nextval('inn_region_seq_id'), '40', 'Калужская область')
    ,
    (nextval('inn_region_seq_id'), '64', 'Саратовская область')
    ,
    (nextval('inn_region_seq_id'), '86', 'Ханты-Мансийский АО')
    ,
    (nextval('inn_region_seq_id'), '41', 'Камчатская область')
    ,
    (nextval('inn_region_seq_id'), '65', 'Сахалинская область')
    ,
    (nextval('inn_region_seq_id'), '87', 'Чукотский АО')
    ,
    (nextval('inn_region_seq_id'), '42', 'Кемеровская область')
    ,
    (nextval('inn_region_seq_id'), '66', 'Свердловская область')
    ,
    (nextval('inn_region_seq_id'), '88', 'Эвенкийский АО')
    ,
    (nextval('inn_region_seq_id'), '43', 'Кировская область')
    ,
    (nextval('inn_region_seq_id'), '67', 'Смоленская область')
    ,
    (nextval('inn_region_seq_id'), '89', 'Ямало-Ненецкий АО')
    ,
    (nextval('inn_region_seq_id'), '20', 'Чеченская Республика')
    ,
    (nextval('inn_region_seq_id'), '44', 'Костромская область')
    ,
    (nextval('inn_region_seq_id'), '68', 'Тамбовская область')
    ,
    (nextval('inn_region_seq_id'), '99', 'Байконур')
    ,
    (nextval('inn_region_seq_id'), '21', 'Чувашская Республика')
    ,
    (nextval('inn_region_seq_id'), '45', 'Курганская область')
    ,
    (nextval('inn_region_seq_id'), '69', 'Тверская область');