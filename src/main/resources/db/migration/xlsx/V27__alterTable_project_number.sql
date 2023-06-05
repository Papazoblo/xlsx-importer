alter table project_number
    add column bank_name varchar(50);

update project_number
set bank_name = 'VTB'
where 1 = 1;