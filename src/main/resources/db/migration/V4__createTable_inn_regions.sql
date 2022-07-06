create sequence if not exists region_seq_id;

CREATE TABLE region
(
    id   bigint       not null,
    code varchar(3)   NOT NULL,
    name varchar(100) NOT NULL,
    primary key (id)
);

insert into region(id, code, name)
values (nextval('region_seq_id'), '01', 'Республика Адыгея') ,
    (nextval('region_seq_id'), '02', 'Республика Башкортостан')
    ,
    (nextval('region_seq_id'), '03', 'Республика Бурятия')
    ,
    (nextval('region_seq_id'), '04', 'Республика Алтай')
    ,
    (nextval('region_seq_id'), '05', 'Республика Дагестан')
    ,
    (nextval('region_seq_id'), '06', 'Ингушская область')
    ,
    (nextval('region_seq_id'), '07', 'Кабардино-Балкарская Республика')
    ,
    (nextval('region_seq_id'), '08', 'Республика Калмыкия')
    ,
    (nextval('region_seq_id'), '09', 'Карачаево-Черкесская Республика')
    ,
    (nextval('region_seq_id'), '10', 'Республика Карелия')
    ,
    (nextval('region_seq_id'), '11', 'Республика Коми')
    ,
    (nextval('region_seq_id'), '12', 'Республика Марий Эл')
    ,
    (nextval('region_seq_id'), '13', 'Республика Мордовия')
    ,
    (nextval('region_seq_id'), '14', 'Республика Саха')
    ,
    (nextval('region_seq_id'), '15', 'Республика Северная Осетия')
    ,
    (nextval('region_seq_id'), '16', 'Республика Татарстан')
    ,
    (nextval('region_seq_id'), '17', 'Республика Тыва')
    ,
    (nextval('region_seq_id'), '18', 'Удмуртская Республика')
    ,
    (nextval('region_seq_id'), '19', 'Республика Хакасия')
    ,
    (nextval('region_seq_id'), '20', 'Чеченская Республика')
    ,
    (nextval('region_seq_id'), '21', 'Чувашская Республика')
    ,
    (nextval('region_seq_id'), '22', 'Алтайский край')
    ,
    (nextval('region_seq_id'), '23', 'Краснодарский край')
    ,
    (nextval('region_seq_id'), '24', 'Красноярский край')
    ,
    (nextval('region_seq_id'), '25', 'Приморский край')
    ,
    (nextval('region_seq_id'), '26', 'Ставропольский край')
    ,
    (nextval('region_seq_id'), '27', 'Хабаровский край')
    ,
    (nextval('region_seq_id'), '28', 'Амурская область')
    ,
    (nextval('region_seq_id'), '29', 'Архангельская область')
    ,
    (nextval('region_seq_id'), '30', 'Астраханская область')
    ,
    (nextval('region_seq_id'), '31', 'Белгородская область')
    ,
    (nextval('region_seq_id'), '32', 'Брянская область')
    ,
    (nextval('region_seq_id'), '33', 'Владимирская область')
    ,
    (nextval('region_seq_id'), '34', 'Волгоградская область')
    ,
    (nextval('region_seq_id'), '35', 'Вологодская область')
    ,
    (nextval('region_seq_id'), '36', 'Воронежская область')
    ,
    (nextval('region_seq_id'), '37', 'Ивановская область')
    ,
    (nextval('region_seq_id'), '38', 'Иркутская область')
    ,
    (nextval('region_seq_id'), '39', 'Калининградская область')
    ,
    (nextval('region_seq_id'), '40', 'Калужская область')
    ,
    (nextval('region_seq_id'), '41', 'Камчатская область')
    ,
    (nextval('region_seq_id'), '42', 'Кемеровская область')
    ,
    (nextval('region_seq_id'), '43', 'Кировская область')
    ,
    (nextval('region_seq_id'), '44', 'Костромская область')
    ,
    (nextval('region_seq_id'), '45', 'Курганская область')
    ,
    (nextval('region_seq_id'), '46', 'Курская область')
    ,
    (nextval('region_seq_id'), '47', 'Ленинградская область')
    ,
    (nextval('region_seq_id'), '48', 'Липецкая область')
    ,
    (nextval('region_seq_id'), '49', 'Магаданская область')
    ,
    (nextval('region_seq_id'), '50', 'Московская область')
    ,
    (nextval('region_seq_id'), '51', 'Мурманская область')
    ,
    (nextval('region_seq_id'), '52', 'Нижегородская область')
    ,
    (nextval('region_seq_id'), '53', 'Новгородская область')
    ,
    (nextval('region_seq_id'), '54', 'Новосибирская область')
    ,
    (nextval('region_seq_id'), '55', 'Омская область')
    ,
    (nextval('region_seq_id'), '56', 'Оренбургская область')
    ,
    (nextval('region_seq_id'), '57', 'Орловская область')
    ,
    (nextval('region_seq_id'), '58', 'Пензенская область')
    ,
    (nextval('region_seq_id'), '59', 'Пермская область')
    ,
    (nextval('region_seq_id'), '60', 'Псковская область')
    ,
    (nextval('region_seq_id'), '61', 'Ростовская область')
    ,
    (nextval('region_seq_id'), '62', 'Рязанская область')
    ,
    (nextval('region_seq_id'), '63', 'Самарская область')
    ,
    (nextval('region_seq_id'), '64', 'Саратовская область')
    ,
    (nextval('region_seq_id'), '65', 'Сахалинская область')
    ,
    (nextval('region_seq_id'), '66', 'Свердловская область')
    ,
    (nextval('region_seq_id'), '67', 'Смоленская область')
    ,
    (nextval('region_seq_id'), '68', 'Тамбовская область')
    ,
    (nextval('region_seq_id'), '69', 'Тверская область')
    ,
    (nextval('region_seq_id'), '70', 'Томская область')
    ,
    (nextval('region_seq_id'), '71', 'Тульская область')
    ,
    (nextval('region_seq_id'), '72', 'Тюменская область')
    ,
    (nextval('region_seq_id'), '73', 'Ульяновская область')
    ,
    (nextval('region_seq_id'), '74', 'Челябинская область')
    ,
    (nextval('region_seq_id'), '75', 'Читинская область')
    ,
    (nextval('region_seq_id'), '76', 'Ярославская область')
    ,
    (nextval('region_seq_id'), '77', 'Москва')
    ,
    (nextval('region_seq_id'), '78', 'Санкт-Петербург')
    ,
    (nextval('region_seq_id'), '79', 'Еврейская АО')
    ,
    (nextval('region_seq_id'), '80', 'Агинский Бурятский АО')
    ,
    (nextval('region_seq_id'), '81', 'Коми-Пермяцкий АО')
    ,
    (nextval('region_seq_id'), '82', 'Корякский АО')
    ,
    (nextval('region_seq_id'), '83', 'Ненецкий АО')
    ,
    (nextval('region_seq_id'), '84', 'Таймырский (Долгано- Ненецкий) АО')
    ,
    (nextval('region_seq_id'), '85', 'Усть-Ордынский Бурятский АО')
    ,
    (nextval('region_seq_id'), '86', 'Ханты-Мансийский АО')
    ,
    (nextval('region_seq_id'), '87', 'Чукотский АО')
    ,
    (nextval('region_seq_id'), '88', 'Эвенкийский АО')
    ,
    (nextval('region_seq_id'), '89', 'Ямало-Ненецкий АО')
    ,
    (nextval('region_seq_id'), '99', 'Байконур');