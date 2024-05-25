package org.petclinic.repository.search;

import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
import java.util.List;
import org.petclinic.domain.Specialty;
import org.petclinic.repository.SpecialtyRepository;
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
 * Spring Data Elasticsearch repository for the {@link Specialty} entity.
 */
public interface SpecialtySearchRepository extends ElasticsearchRepository<Specialty, Long>, SpecialtySearchRepositoryInternal {}

interface SpecialtySearchRepositoryInternal {
    Page<Specialty> search(String query, Pageable pageable);

    Page<Specialty> search(Query query);

    @Async
    void index(Specialty entity);

    @Async
    void deleteFromIndexById(Long id);
}

class SpecialtySearchRepositoryInternalImpl implements SpecialtySearchRepositoryInternal {

    private final ElasticsearchTemplate elasticsearchTemplate;
    private final SpecialtyRepository repository;

    SpecialtySearchRepositoryInternalImpl(ElasticsearchTemplate elasticsearchTemplate, SpecialtyRepository repository) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
    }

    @Override
    public Page<Specialty> search(String query, Pageable pageable) {
        NativeQuery nativeQuery = new NativeQuery(QueryStringQuery.of(qs -> qs.query(query))._toQuery());
        return search(nativeQuery.setPageable(pageable));
    }

    @Override
    public Page<Specialty> search(Query query) {
        SearchHits<Specialty> searchHits = elasticsearchTemplate.search(query, Specialty.class);
        List<Specialty> hits = searchHits.map(SearchHit::getContent).stream().toList();
        return new PageImpl<>(hits, query.getPageable(), searchHits.getTotalHits());
    }

    @Override
    public void index(Specialty entity) {
        repository.findById(entity.getId()).ifPresent(elasticsearchTemplate::save);
    }

    @Override
    public void deleteFromIndexById(Long id) {
        elasticsearchTemplate.delete(String.valueOf(id), Specialty.class);
    }
}
