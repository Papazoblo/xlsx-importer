insert into first_load_lead(inn, create_date)
    (select distinct c.inn, now()
     from contact_download_info cdi_out
              join (select cdi.contact_id, max(cdi.id) as id_in
                    from contact_download_info cdi
                             join file_info_bank fib on cdi.file_info_bank_id = fib.id
                    where fib.bank = 'VTB'
                      and cdi.create_at >= (current_date - 21)
                    group by cdi.contact_id) res on res.id_in = cdi_out.id
              join contact_new c on res.contact_id = c.id
     where cdi_out.check_status = 'REJECTED');