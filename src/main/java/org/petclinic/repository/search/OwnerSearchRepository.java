package org.petclinic.repository.search;

import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
import java.util.List;
import org.petclinic.domain.Owner;
import org.petclinic.repository.OwnerRepository;
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
 * Spring Data Elasticsearch repository for the {@link Owner} entity.
 */
public interface OwnerSearchRepository extends ElasticsearchRepository<Owner, Long>, OwnerSearchRepositoryInternal {}

interface OwnerSearchRepositoryInternal {
    Page<Owner> search(String query, Pageable pageable);

    Page<Owner> search(Query query);

    @Async
    void index(Owner entity);

    @Async
    void deleteFromIndexById(Long id);
}

class OwnerSearchRepositoryInternalImpl implements OwnerSearchRepositoryInternal {

    private final ElasticsearchTemplate elasticsearchTemplate;
    private final OwnerRepository repository;

    OwnerSearchRepositoryInternalImpl(ElasticsearchTemplate elasticsearchTemplate, OwnerRepository repository) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
    }

    @Override
    public Page<Owner> search(String query, Pageable pageable) {
        NativeQuery nativeQuery = new NativeQuery(QueryStringQuery.of(qs -> qs.query(query))._toQuery());
        return search(nativeQuery.setPageable(pageable));
    }

    @Override
    public Page<Owner> search(Query query) {
        SearchHits<Owner> searchHits = elasticsearchTemplate.search(query, Owner.class);
        List<Owner> hits = searchHits.map(SearchHit::getContent).stream().toList();
        return new PageImpl<>(hits, query.getPageable(), searchHits.getTotalHits());
    }

    @Override
    public void index(Owner entity) {
        repository.findById(entity.getId()).ifPresent(elasticsearchTemplate::save);
    }

    @Override
    public void deleteFromIndexById(Long id) {
        elasticsearchTemplate.delete(String.valueOf(id), Owner.class);
    }
}
