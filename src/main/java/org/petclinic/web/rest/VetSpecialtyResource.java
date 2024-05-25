package org.petclinic.web.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import org.petclinic.repository.VetSpecialtyRepository;
import org.petclinic.service.VetSpecialtyService;
import org.petclinic.service.dto.VetSpecialtyDTO;
import org.petclinic.web.rest.errors.BadRequestAlertException;
import org.petclinic.web.rest.errors.ElasticsearchExceptionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link org.petclinic.domain.VetSpecialty}.
 */
@RestController
@RequestMapping("/api/vet-specialties")
public class VetSpecialtyResource {

    private final Logger log = LoggerFactory.getLogger(VetSpecialtyResource.class);

    private static final String ENTITY_NAME = "vetSpecialty";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final VetSpecialtyService vetSpecialtyService;

    private final VetSpecialtyRepository vetSpecialtyRepository;

    public VetSpecialtyResource(VetSpecialtyService vetSpecialtyService, VetSpecialtyRepository vetSpecialtyRepository) {
        this.vetSpecialtyService = vetSpecialtyService;
        this.vetSpecialtyRepository = vetSpecialtyRepository;
    }

    /**
     * {@code POST  /vet-specialties} : Create a new vetSpecialty.
     *
     * @param vetSpecialtyDTO the vetSpecialtyDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new vetSpecialtyDTO, or with status {@code 400 (Bad Request)} if the vetSpecialty has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<VetSpecialtyDTO> createVetSpecialty(@RequestBody VetSpecialtyDTO vetSpecialtyDTO) throws URISyntaxException {
        log.debug("REST request to save VetSpecialty : {}", vetSpecialtyDTO);
        if (vetSpecialtyDTO.getId() != null) {
            throw new BadRequestAlertException("A new vetSpecialty cannot already have an ID", ENTITY_NAME, "idexists");
        }
        vetSpecialtyDTO = vetSpecialtyService.save(vetSpecialtyDTO);
        return ResponseEntity.created(new URI("/api/vet-specialties/" + vetSpecialtyDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, vetSpecialtyDTO.getId().toString()))
            .body(vetSpecialtyDTO);
    }

    /**
     * {@code GET  /vet-specialties} : get all the vetSpecialties.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of vetSpecialties in body.
     */
    @GetMapping("")
    public List<VetSpecialtyDTO> getAllVetSpecialties() {
        log.debug("REST request to get all VetSpecialties");
        return vetSpecialtyService.findAll();
    }

    /**
     * {@code GET  /vet-specialties/:id} : get the "id" vetSpecialty.
     *
     * @param id the id of the vetSpecialtyDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the vetSpecialtyDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<VetSpecialtyDTO> getVetSpecialty(@PathVariable("id") Long id) {
        log.debug("REST request to get VetSpecialty : {}", id);
        Optional<VetSpecialtyDTO> vetSpecialtyDTO = vetSpecialtyService.findOne(id);
        return ResponseUtil.wrapOrNotFound(vetSpecialtyDTO);
    }

    /**
     * {@code DELETE  /vet-specialties/:id} : delete the "id" vetSpecialty.
     *
     * @param id the id of the vetSpecialtyDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVetSpecialty(@PathVariable("id") Long id) {
        log.debug("REST request to delete VetSpecialty : {}", id);
        vetSpecialtyService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }

    /**
     * {@code SEARCH  /vet-specialties/_search?query=:query} : search for the vetSpecialty corresponding
     * to the query.
     *
     * @param query the query of the vetSpecialty search.
     * @return the result of the search.
     */
    @GetMapping("/_search")
    public List<VetSpecialtyDTO> searchVetSpecialties(@RequestParam("query") String query) {
        log.debug("REST request to search VetSpecialties for query {}", query);
        try {
            return vetSpecialtyService.search(query);
        } catch (RuntimeException e) {
            throw ElasticsearchExceptionMapper.mapException(e);
        }
    }
}
