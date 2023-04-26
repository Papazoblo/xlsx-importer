package ru.medvedev.importer.specification;

import org.springframework.data.jpa.domain.Specification;
import ru.medvedev.importer.dto.InnExcludeFilter;
import ru.medvedev.importer.entity.InnExcludeEntity;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InnExcludeSpecification implements Specification<InnExcludeEntity> {

    private InnExcludeFilter filter;

    private InnExcludeSpecification(InnExcludeFilter filter) {
        this.filter = filter;
    }

    public static InnExcludeSpecification of(InnExcludeFilter filter) {
        return new InnExcludeSpecification(filter);
    }

    @Override
    public Predicate toPredicate(Root<InnExcludeEntity> root, CriteriaQuery<?> cq, CriteriaBuilder cb) {

        List<Predicate> predicates = new ArrayList<>();

        Optional.ofNullable(filter.getInn()).ifPresent(value ->
                predicates.add(cb.like(root.get("id").get("inn"), "%" + value + "%")));

        Optional.ofNullable(filter.getBank()).ifPresent(value ->
                predicates.add(cb.equal(root.get("id").get("bank"), value)));

        Optional.ofNullable(filter.getCreateDateFrom()).ifPresent(value ->
                predicates.add(cb.greaterThanOrEqualTo(root.get("createDate"), value)));

        Optional.ofNullable(filter.getCreateDateTo()).ifPresent(value ->
                predicates.add(cb.lessThanOrEqualTo(root.get("createDate"), value)));

        if (predicates.isEmpty()) {
            return cb.and();
        }
        return cb.and(predicates.toArray(new Predicate[0]));
    }
}
