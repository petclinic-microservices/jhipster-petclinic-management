package org.petclinic.repository.search;

import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
import java.util.List;
import org.petclinic.domain.Visit;
import org.petclinic.repository.VisitRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.scheduling.annotation.Async;

/**
 * Spring Data Elasticsearch repository for the {@link Visit} entity.
 */
public interface VisitSearchRepository extends ElasticsearchRepository<Visit, Long>, VisitSearchRepositoryInternal {}

interface VisitSearchRepositoryInternal {
    Page<Visit> search(String query, Pageable pageable);

    Page<Visit> search(Query query);

    @Async
    void index(Visit entity);

    @Async
    void deleteFromIndexById(Long id);
}

class VisitSearchRepositoryInternalImpl implements VisitSearchRepositoryInternal {

    private final ElasticsearchTemplate elasticsearchTemplate;
    private final VisitRepository repository;

    VisitSearchRepositoryInternalImpl(ElasticsearchTemplate elasticsearchTemplate, VisitRepository repository) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
    }

    @Override
    public Page<Visit> search(String query, Pageable pageable) {
        NativeQuery nativeQuery = new NativeQuery(QueryStringQuery.of(qs -> qs.query(query))._toQuery());
        return search(nativeQuery.setPageable(pageable));
    }

    @Override
    public Page<Visit> search(Query query) {
        SearchHits<Visit> searchHits = elasticsearchTemplate.search(query, Visit.class);
        List<Visit> hits = searchHits.map(SearchHit::getContent).stream().toList();
        return new PageImpl<>(hits, query.getPageable(), searchHits.getTotalHits());
    }

    @Override
    public void index(Visit entity) {
        repository.findOneWithEagerRelationships(entity.getId()).ifPresent(elasticsearchTemplate::save);
    }

    @Override
    public void deleteFromIndexById(Long id) {
        elasticsearchTemplate.delete(String.valueOf(id), Visit.class);
    }
}
