insert into contact_new (id,
                         name,
                         surname,
                         middle_name,
                         inn,
                         phone,
                         city,
                         org_name,
                         ogrn,
                         region,
                         create_at)
    (select nextval('contact_seq_id'),
            c.name,
            c.surname,
            c.middle_name,
            c.inn,
            c.phone,
            c.city,
            c.org_name,
            c.ogrn,
            c.region,
            c.create_at
     from contact c
              join (select max(id) as id, inn
                    from contact
                    group by inn) res on res.id = c.id);


alter table webhook_status_map alter column bank_name drop not null;