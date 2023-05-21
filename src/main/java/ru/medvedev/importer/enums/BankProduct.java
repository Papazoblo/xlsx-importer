package ru.medvedev.importer.enums;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BankProduct {

    LP_RKO("РКО"),
    LP_ACQ_TR("Эквайринг (Торговый)"),
    LP_AKASSA("Альфа-касса"),
    LP_LOAN_BUS("Бизнес кредит - кредит на ЮЛ/ИП"),
    LP_OVER_ADV("Овердрафт для новых клиентов банка"),
    LP_SPECACC44("Спец. счет 44-Ф3"),
    LP_ACQ_E("Эквайринг (Интернет)");

    private final String title;
}
