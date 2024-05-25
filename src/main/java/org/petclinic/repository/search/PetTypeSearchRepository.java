package org.petclinic.repository.search;

import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
import java.util.List;
import org.petclinic.domain.PetType;
import org.petclinic.repository.PetTypeRepository;
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
 * Spring Data Elasticsearch repository for the {@link PetType} entity.
 */
public interface PetTypeSearchRepository extends ElasticsearchRepository<PetType, Long>, PetTypeSearchRepositoryInternal {}

interface PetTypeSearchRepositoryInternal {
    Page<PetType> search(String query, Pageable pageable);

    Page<PetType> search(Query query);

    @Async
    void index(PetType entity);

    @Async
    void deleteFromIndexById(Long id);
}

class PetTypeSearchRepositoryInternalImpl implements PetTypeSearchRepositoryInternal {

    private final ElasticsearchTemplate elasticsearchTemplate;
    private final PetTypeRepository repository;

    PetTypeSearchRepositoryInternalImpl(ElasticsearchTemplate elasticsearchTemplate, PetTypeRepository repository) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
    }

    @Override
    public Page<PetType> search(String query, Pageable pageable) {
        NativeQuery nativeQuery = new NativeQuery(QueryStringQuery.of(qs -> qs.query(query))._toQuery());
        return search(nativeQuery.setPageable(pageable));
    }

    @Override
    public Page<PetType> search(Query query) {
        SearchHits<PetType> searchHits = elasticsearchTemplate.search(query, PetType.class);
        List<PetType> hits = searchHits.map(SearchHit::getContent).stream().toList();
        return new PageImpl<>(hits, query.getPageable(), searchHits.getTotalHits());
    }

    @Override
    public void index(PetType entity) {
        repository.findById(entity.getId()).ifPresent(elasticsearchTemplate::save);
    }

    @Override
    public void deleteFromIndexById(Long id) {
        elasticsearchTemplate.delete(String.valueOf(id), PetType.class);
    }
}
