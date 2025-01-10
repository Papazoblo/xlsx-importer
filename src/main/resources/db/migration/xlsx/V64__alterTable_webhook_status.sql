alter table webhook_status
    add column alfa_avd_code varchar(16);

update webhook_status
set alfa_avd_code = 'Ipv_h'
where name = 'Заявка Тёплый';

update webhook_status
set alfa_avd_code = 'Ipv_r'
where name = 'Лид (перезвонить позже)';