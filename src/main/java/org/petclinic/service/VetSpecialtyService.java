package org.petclinic.service;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.petclinic.domain.VetSpecialty;
import org.petclinic.repository.VetSpecialtyRepository;
import org.petclinic.repository.search.VetSpecialtySearchRepository;
import org.petclinic.service.dto.VetSpecialtyDTO;
import org.petclinic.service.mapper.VetSpecialtyMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link org.petclinic.domain.VetSpecialty}.
 */
@Service
@Transactional
public class VetSpecialtyService {

    private final Logger log = LoggerFactory.getLogger(VetSpecialtyService.class);

    private final VetSpecialtyRepository vetSpecialtyRepository;

    private final VetSpecialtyMapper vetSpecialtyMapper;

    private final VetSpecialtySearchRepository vetSpecialtySearchRepository;

    public VetSpecialtyService(
        VetSpecialtyRepository vetSpecialtyRepository,
        VetSpecialtyMapper vetSpecialtyMapper,
        VetSpecialtySearchRepository vetSpecialtySearchRepository
    ) {
        this.vetSpecialtyRepository = vetSpecialtyRepository;
        this.vetSpecialtyMapper = vetSpecialtyMapper;
        this.vetSpecialtySearchRepository = vetSpecialtySearchRepository;
    }

    /**
     * Save a vetSpecialty.
     *
     * @param vetSpecialtyDTO the entity to save.
     * @return the persisted entity.
     */
    public VetSpecialtyDTO save(VetSpecialtyDTO vetSpecialtyDTO) {
        log.debug("Request to save VetSpecialty : {}", vetSpecialtyDTO);
        VetSpecialty vetSpecialty = vetSpecialtyMapper.toEntity(vetSpecialtyDTO);
        vetSpecialty = vetSpecialtyRepository.save(vetSpecialty);
        vetSpecialtySearchRepository.index(vetSpecialty);
        return vetSpecialtyMapper.toDto(vetSpecialty);
    }

    /**
     * Get all the vetSpecialties.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<VetSpecialtyDTO> findAll() {
        log.debug("Request to get all VetSpecialties");
        return vetSpecialtyRepository.findAll().stream().map(vetSpecialtyMapper::toDto).collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Get one vetSpecialty by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<VetSpecialtyDTO> findOne(Long id) {
        log.debug("Request to get VetSpecialty : {}", id);
        return vetSpecialtyRepository.findById(id).map(vetSpecialtyMapper::toDto);
    }

    /**
     * Delete the vetSpecialty by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete VetSpecialty : {}", id);
        vetSpecialtyRepository.deleteById(id);
        vetSpecialtySearchRepository.deleteFromIndexById(id);
    }

    /**
     * Search for the vetSpecialty corresponding to the query.
     *
     * @param query the query of the search.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<VetSpecialtyDTO> search(String query) {
        log.debug("Request to search VetSpecialties for query {}", query);
        try {
            return StreamSupport.stream(vetSpecialtySearchRepository.search(query).spliterator(), false)
                .map(vetSpecialtyMapper::toDto)
                .toList();
        } catch (RuntimeException e) {
            throw e;
        }
    }
}
