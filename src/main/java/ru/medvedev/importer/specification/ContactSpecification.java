package ru.medvedev.importer.specification;

import org.springframework.data.jpa.domain.Specification;
import ru.medvedev.importer.dto.ContactFilter;
import ru.medvedev.importer.entity.ContactEntity;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ContactSpecification implements Specification<ContactEntity> {

    private final ContactFilter filter;

    public static ContactSpecification of(ContactFilter contactFilter) {
        return new ContactSpecification(contactFilter);
    }

    private ContactSpecification(ContactFilter filter) {
        this.filter = filter;
    }

    @Override
    public Predicate toPredicate(Root<ContactEntity> root, CriteriaQuery<?> query, CriteriaBuilder builder) {

        List<Predicate> predicates = new ArrayList<>();

        Optional.ofNullable(filter.getOrgName()).ifPresent(value ->
                predicates.add(builder.like(builder.lower(root.get("orgName")), "%" + value.toLowerCase() + "%")));

        Optional.ofNullable(filter.getName()).ifPresent(value ->
                predicates.add(builder.like(builder.lower(root.get("name")), "%" + value.toLowerCase() + "%")));

        Optional.ofNullable(filter.getSurname()).ifPresent(value ->
                predicates.add(builder.like(builder.lower(root.get("surname")), "%" + value.toLowerCase() + "%")));

        Optional.ofNullable(filter.getMiddleName()).ifPresent(value ->
                predicates.add(builder.like(builder.lower(root.get("middleName")), "%" + value.toLowerCase() + "%")));

        Optional.ofNullable(filter.getPhone()).ifPresent(value ->
                predicates.add(builder.like(builder.lower(root.get("phone")), "%" + value.toLowerCase() + "%")));

        Optional.ofNullable(filter.getInn()).ifPresent(value ->
                predicates.add(builder.like(builder.lower(root.get("inn")), "%" + value.toLowerCase() + "%")));

        Optional.ofNullable(filter.getOgrn()).ifPresent(value ->
                predicates.add(builder.like(builder.lower(root.get("ogrn")), "%" + value.toLowerCase() + "%")));

        Optional.ofNullable(filter.getRegion()).ifPresent(value ->
                predicates.add(builder.like(builder.lower(root.get("region")), "%" + value.toLowerCase() + "%")));

        Optional.ofNullable(filter.getCity()).ifPresent(value ->
                predicates.add(builder.like(builder.lower(root.get("city")), "%" + value.toLowerCase() + "%")));

        Optional.ofNullable(filter.getCreateDateFrom()).ifPresent(value ->
                predicates.add(builder.greaterThanOrEqualTo(root.get("createAt"), value)));

        Optional.ofNullable(filter.getCreateDateTo()).ifPresent(value ->
                predicates.add(builder.lessThanOrEqualTo(root.get("createAt"), value)));

        if (!filter.getStatus().isEmpty()) {
            predicates.add(builder.in(root.get("status")).value(filter.getStatus()));
        }

        if (!filter.getBank().isEmpty()) {
            predicates.add(builder.in(root.get("bank")).value(filter.getBank()));
        }

        if (!filter.getOriginal().isEmpty()) {
            predicates.add(builder.in(root.join("contactFileInfoEntityList").get("original"))
                    .value(filter.getOriginal()));
        }

        if (predicates.isEmpty()) {
            return builder.and();
        }
        return builder.and(predicates.toArray(new Predicate[0]));
    }
}
