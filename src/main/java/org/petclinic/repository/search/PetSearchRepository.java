package org.petclinic.repository.search;

import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
import java.util.List;
import org.petclinic.domain.Pet;
import org.petclinic.repository.PetRepository;
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
 * Spring Data Elasticsearch repository for the {@link Pet} entity.
 */
public interface PetSearchRepository extends ElasticsearchRepository<Pet, Long>, PetSearchRepositoryInternal {}

interface PetSearchRepositoryInternal {
    Page<Pet> search(String query, Pageable pageable);

    Page<Pet> search(Query query);

    @Async
    void index(Pet entity);

    @Async
    void deleteFromIndexById(Long id);
}

class PetSearchRepositoryInternalImpl implements PetSearchRepositoryInternal {

    private final ElasticsearchTemplate elasticsearchTemplate;
    private final PetRepository repository;

    PetSearchRepositoryInternalImpl(ElasticsearchTemplate elasticsearchTemplate, PetRepository repository) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
    }

    @Override
    public Page<Pet> search(String query, Pageable pageable) {
        NativeQuery nativeQuery = new NativeQuery(QueryStringQuery.of(qs -> qs.query(query))._toQuery());
        return search(nativeQuery.setPageable(pageable));
    }

    @Override
    public Page<Pet> search(Query query) {
        SearchHits<Pet> searchHits = elasticsearchTemplate.search(query, Pet.class);
        List<Pet> hits = searchHits.map(SearchHit::getContent).stream().toList();
        return new PageImpl<>(hits, query.getPageable(), searchHits.getTotalHits());
    }

    @Override
    public void index(Pet entity) {
        repository.findOneWithEagerRelationships(entity.getId()).ifPresent(elasticsearchTemplate::save);
    }

    @Override
    public void deleteFromIndexById(Long id) {
        elasticsearchTemplate.delete(String.valueOf(id), Pet.class);
    }
}
