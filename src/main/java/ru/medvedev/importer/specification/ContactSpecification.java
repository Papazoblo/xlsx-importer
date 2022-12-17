package ru.medvedev.importer.specification;

import org.apache.logging.log4j.util.Strings;
import org.springframework.data.jpa.domain.Specification;
import ru.medvedev.importer.dto.ContactFilter;
import ru.medvedev.importer.entity.ContactBankActualityEntity;
import ru.medvedev.importer.entity.ContactNewEntity;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static ru.medvedev.importer.enums.Bank.VTB;
import static ru.medvedev.importer.enums.Bank.VTB_OPENING;

public class ContactSpecification implements Specification<ContactNewEntity> {

    private final ContactFilter filter;

    public static ContactSpecification of(ContactFilter contactFilter) {
        return new ContactSpecification(contactFilter);
    }

    private ContactSpecification(ContactFilter filter) {
        this.filter = filter;
    }

    @Override
    public Predicate toPredicate(Root<ContactNewEntity> root, CriteriaQuery<?> query, CriteriaBuilder builder) {

        List<Predicate> predicates = new ArrayList<>();

        query.distinct(true);

        Optional.ofNullable(filter.getOrgName())
                .filter(Strings::isNotBlank)
                .ifPresent(value ->
                        predicates.add(builder.like(builder.lower(root.get("orgName")), "%" + value.toLowerCase() + "%")));

        Optional.ofNullable(filter.getName())
                .filter(Strings::isNotBlank)
                .ifPresent(value ->
                        predicates.add(builder.like(builder.lower(root.get("name")), "%" + value.toLowerCase() + "%")));

        Optional.ofNullable(filter.getSurname())
                .filter(Strings::isNotBlank)
                .ifPresent(value ->
                        predicates.add(builder.like(builder.lower(root.get("surname")), "%" + value.toLowerCase() + "%")));

        Optional.ofNullable(filter.getMiddleName())
                .filter(Strings::isNotBlank)
                .ifPresent(value ->
                        predicates.add(builder.like(builder.lower(root.get("middleName")), "%" + value.toLowerCase() + "%")));

        Optional.ofNullable(filter.getPhone())
                .filter(Strings::isNotBlank)
                .ifPresent(value ->
                        predicates.add(builder.like(builder.lower(root.get("phone")), "%" + value.toLowerCase() + "%")));

        Optional.ofNullable(filter.getInn())
                .filter(Strings::isNotBlank)
                .ifPresent(value ->
                        predicates.add(builder.like(builder.lower(root.get("inn")), "%" + value.toLowerCase() + "%")));

        Optional.ofNullable(filter.getOgrn())
                .filter(Strings::isNotBlank)
                .ifPresent(value ->
                        predicates.add(builder.like(builder.lower(root.get("ogrn")), "%" + value.toLowerCase() + "%")));

        Optional.ofNullable(filter.getRegion())
                .filter(Strings::isNotBlank)
                .ifPresent(value ->
                        predicates.add(builder.like(builder.lower(root.get("region")), "%" + value.toLowerCase() + "%")));

        Optional.ofNullable(filter.getCity())
                .filter(Strings::isNotBlank)
                .ifPresent(value ->
                        predicates.add(builder.like(builder.lower(root.get("city")), "%" + value.toLowerCase() + "%")));

        /*if (!filter.getStatus().isEmpty()) {
            predicates.add(builder.in(root.get("status")).value(filter.getStatus()));
        }

        if (!filter.getBank().isEmpty()) {
            predicates.add(builder.in(root.get("bank")).value(filter.getBank()));
        }

        if (!filter.getOriginal().isEmpty()) {
            predicates.add(builder.in(root.join("contactFileInfoEntityList").get("original"))
                    .value(filter.getOriginal()));
        }*/

        Optional.ofNullable(filter.getCreateDateFrom()).ifPresent(value ->
                predicates.add(builder.greaterThanOrEqualTo(root.get("createAt"), value)));

        Optional.ofNullable(filter.getCreateDateTo()).ifPresent(value ->
                predicates.add(builder.lessThanOrEqualTo(root.get("createAt"), value)));

        Join<ContactNewEntity, ContactBankActualityEntity> contactActualityJoin = root.join("actualityList", JoinType.LEFT);

        List<Predicate> subPredicate = new ArrayList<>();
        if (!filter.getOpeningActuality().isEmpty() || !filter.getOpeningWebhook().isEmpty()) {

            List<Predicate> bankPredicate = new ArrayList<>();

            if (!filter.getOpeningActuality().isEmpty()) {
                bankPredicate.add(builder.in(contactActualityJoin.get("actuality")).value(filter.getOpeningActuality()));
            }

            if (!filter.getOpeningWebhook().isEmpty()) {
                bankPredicate.add(builder.in(contactActualityJoin.get("webhookStatusId")).value(filter.getOpeningWebhook()));
            }

            bankPredicate.add(builder.equal(contactActualityJoin.get("bank"), VTB_OPENING));

            if (bankPredicate.size() > 1) {
                subPredicate.add(builder.and(bankPredicate.toArray(new Predicate[0])));
            }
        }

        if (!filter.getVtbActuality().isEmpty() || !filter.getVtbWebhook().isEmpty()) {

            List<Predicate> bankPredicate = new ArrayList<>();

            if (!filter.getVtbActuality().isEmpty()) {
                bankPredicate.add(builder.in(contactActualityJoin.get("actuality")).value(filter.getVtbActuality()));
            }

            if (!filter.getVtbWebhook().isEmpty()) {
                bankPredicate.add(builder.in(contactActualityJoin.get("webhookStatusId")).value(filter.getVtbWebhook()));
            }

            bankPredicate.add(builder.equal(contactActualityJoin.get("bank"), VTB));

            if (bankPredicate.size() > 1) {
                subPredicate.add(builder.and(bankPredicate.toArray(new Predicate[0])));
            }
        }

        if (!subPredicate.isEmpty()) {
            predicates.add(builder.or(subPredicate.toArray(new Predicate[0])));
        }

        if (predicates.isEmpty()) {
            return builder.and();
        }
        return builder.and(predicates.toArray(new Predicate[0]));
    }
}
