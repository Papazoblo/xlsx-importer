package ru.medvedev.importer.specification;

import org.springframework.data.jpa.domain.Specification;
import ru.medvedev.importer.dto.WebhookSuccessFilter;
import ru.medvedev.importer.entity.WebhookSuccessStatusEntity;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WebhookSuccessSpecification implements Specification<WebhookSuccessStatusEntity> {

    private final WebhookSuccessFilter filter;

    public static WebhookSuccessSpecification of(WebhookSuccessFilter filter) {
        return new WebhookSuccessSpecification(filter);
    }

    private WebhookSuccessSpecification(WebhookSuccessFilter filter) {
        this.filter = filter;
    }

    @Override
    public Predicate toPredicate(Root<WebhookSuccessStatusEntity> root, CriteriaQuery<?> cq, CriteriaBuilder b) {
        List<Predicate> predicates = new ArrayList<>();

        Optional.ofNullable(filter.getBank()).ifPresent(value ->
                predicates.add(b.equal(root.get("bank"), value)));

        predicates.add(Optional.ofNullable(filter.getType())
                .map(value -> b.equal(root.get("type"), value))
                .orElseGet(() -> b.isNull(root.get("type"))));

        Optional.ofNullable(filter.getName()).ifPresent(value ->
                predicates.add(b.equal(root.join("webhookStatusEntity").get("name"), value)));

        if(predicates.isEmpty()) {
            return b.and();
        }
        return b.and(predicates.toArray(new Predicate[0]));
    }
}
