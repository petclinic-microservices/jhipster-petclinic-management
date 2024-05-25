package org.petclinic.repository.search;

import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
import java.util.List;
import org.petclinic.domain.Vet;
import org.petclinic.repository.VetRepository;
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
 * Spring Data Elasticsearch repository for the {@link Vet} entity.
 */
public interface VetSearchRepository extends ElasticsearchRepository<Vet, Long>, VetSearchRepositoryInternal {}

interface VetSearchRepositoryInternal {
    Page<Vet> search(String query, Pageable pageable);

    Page<Vet> search(Query query);

    @Async
    void index(Vet entity);

    @Async
    void deleteFromIndexById(Long id);
}

class VetSearchRepositoryInternalImpl implements VetSearchRepositoryInternal {

    private final ElasticsearchTemplate elasticsearchTemplate;
    private final VetRepository repository;

    VetSearchRepositoryInternalImpl(ElasticsearchTemplate elasticsearchTemplate, VetRepository repository) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
    }

    @Override
    public Page<Vet> search(String query, Pageable pageable) {
        NativeQuery nativeQuery = new NativeQuery(QueryStringQuery.of(qs -> qs.query(query))._toQuery());
        return search(nativeQuery.setPageable(pageable));
    }

    @Override
    public Page<Vet> search(Query query) {
        SearchHits<Vet> searchHits = elasticsearchTemplate.search(query, Vet.class);
        List<Vet> hits = searchHits.map(SearchHit::getContent).stream().toList();
        return new PageImpl<>(hits, query.getPageable(), searchHits.getTotalHits());
    }

    @Override
    public void index(Vet entity) {
        repository.findOneWithEagerRelationships(entity.getId()).ifPresent(elasticsearchTemplate::save);
    }

    @Override
    public void deleteFromIndexById(Long id) {
        elasticsearchTemplate.delete(String.valueOf(id), Vet.class);
    }
}
