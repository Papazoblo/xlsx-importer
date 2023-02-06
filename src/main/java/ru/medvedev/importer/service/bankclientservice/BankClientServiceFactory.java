package ru.medvedev.importer.service.bankclientservice;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.medvedev.importer.enums.Bank;

@Service
@RequiredArgsConstructor
public class BankClientServiceFactory {

    private final VtbClientService vtbClientService;
    private final OpeningClientService openingClientService;
    private final RaiffeisenClientService raiffeisenClientService;

    public BankClientService getBankClientService(Bank bank) {
        switch (bank) {
            case VTB:
                return vtbClientService;
            case VTB_OPENING:
                return openingClientService;
            case RAIFFEISEN:
                return raiffeisenClientService;
        }
        return null;
    }
}
