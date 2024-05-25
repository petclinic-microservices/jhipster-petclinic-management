package org.petclinic.repository.search;

import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
import java.util.stream.Stream;
import org.petclinic.domain.VetSpecialty;
import org.petclinic.repository.VetSpecialtyRepository;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.scheduling.annotation.Async;

/**
 * Spring Data Elasticsearch repository for the {@link VetSpecialty} entity.
 */
public interface VetSpecialtySearchRepository extends ElasticsearchRepository<VetSpecialty, Long>, VetSpecialtySearchRepositoryInternal {}

interface VetSpecialtySearchRepositoryInternal {
    Stream<VetSpecialty> search(String query);

    Stream<VetSpecialty> search(Query query);

    @Async
    void index(VetSpecialty entity);

    @Async
    void deleteFromIndexById(Long id);
}

class VetSpecialtySearchRepositoryInternalImpl implements VetSpecialtySearchRepositoryInternal {

    private final ElasticsearchTemplate elasticsearchTemplate;
    private final VetSpecialtyRepository repository;

    VetSpecialtySearchRepositoryInternalImpl(ElasticsearchTemplate elasticsearchTemplate, VetSpecialtyRepository repository) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
    }

    @Override
    public Stream<VetSpecialty> search(String query) {
        NativeQuery nativeQuery = new NativeQuery(QueryStringQuery.of(qs -> qs.query(query))._toQuery());
        return search(nativeQuery);
    }

    @Override
    public Stream<VetSpecialty> search(Query query) {
        return elasticsearchTemplate.search(query, VetSpecialty.class).map(SearchHit::getContent).stream();
    }

    @Override
    public void index(VetSpecialty entity) {
        repository.findById(entity.getId()).ifPresent(elasticsearchTemplate::save);
    }

    @Override
    public void deleteFromIndexById(Long id) {
        elasticsearchTemplate.delete(String.valueOf(id), VetSpecialty.class);
    }
}
