package ru.medvedev.importer.specification;

import org.springframework.data.jpa.domain.Specification;
import ru.medvedev.importer.dto.ContactNewFilter;
import ru.medvedev.importer.entity.ContactBankActualityEntity;
import ru.medvedev.importer.entity.ContactNewEntity;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static ru.medvedev.importer.enums.Bank.VTB;
import static ru.medvedev.importer.enums.Bank.VTB_OPENING;

public class ContactNewSpecification implements Specification<ContactNewEntity> {

    private final ContactNewFilter filter;

    public static ContactNewSpecification of(ContactNewFilter contactFilter) {
        return new ContactNewSpecification(contactFilter);
    }

    private ContactNewSpecification(ContactNewFilter filter) {
        this.filter = filter;
    }

    @Override
    public Predicate toPredicate(Root<ContactNewEntity> root, CriteriaQuery<?> query, CriteriaBuilder builder) {

        List<Predicate> predicates = new ArrayList<>();

        query.distinct(true);

        Optional.ofNullable(filter.getCreateDateFrom()).ifPresent(value ->
                predicates.add(builder.greaterThanOrEqualTo(root.get("createDateFrom"), value)));

        Optional.ofNullable(filter.getCreateDateTo()).ifPresent(value ->
                predicates.add(builder.lessThanOrEqualTo(root.get("createDateTo"), value)));

        Join<ContactNewEntity, ContactBankActualityEntity> contactActualityJoin = root.join("actualityList");

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
