package org.petclinic.web.rest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.petclinic.repository.SpecialtyRepository;
import org.petclinic.service.SpecialtyService;
import org.petclinic.service.dto.SpecialtyDTO;
import org.petclinic.web.rest.errors.BadRequestAlertException;
import org.petclinic.web.rest.errors.ElasticsearchExceptionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link org.petclinic.domain.Specialty}.
 */
@RestController
@RequestMapping("/api/specialties")
public class SpecialtyResource {

    private final Logger log = LoggerFactory.getLogger(SpecialtyResource.class);

    private static final String ENTITY_NAME = "specialty";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final SpecialtyService specialtyService;

    private final SpecialtyRepository specialtyRepository;

    public SpecialtyResource(SpecialtyService specialtyService, SpecialtyRepository specialtyRepository) {
        this.specialtyService = specialtyService;
        this.specialtyRepository = specialtyRepository;
    }

    /**
     * {@code POST  /specialties} : Create a new specialty.
     *
     * @param specialtyDTO the specialtyDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new specialtyDTO, or with status {@code 400 (Bad Request)} if the specialty has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<SpecialtyDTO> createSpecialty(@Valid @RequestBody SpecialtyDTO specialtyDTO) throws URISyntaxException {
        log.debug("REST request to save Specialty : {}", specialtyDTO);
        if (specialtyDTO.getId() != null) {
            throw new BadRequestAlertException("A new specialty cannot already have an ID", ENTITY_NAME, "idexists");
        }
        specialtyDTO = specialtyService.save(specialtyDTO);
        return ResponseEntity.created(new URI("/api/specialties/" + specialtyDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, specialtyDTO.getId().toString()))
            .body(specialtyDTO);
    }

    /**
     * {@code PUT  /specialties/:id} : Updates an existing specialty.
     *
     * @param id the id of the specialtyDTO to save.
     * @param specialtyDTO the specialtyDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated specialtyDTO,
     * or with status {@code 400 (Bad Request)} if the specialtyDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the specialtyDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<SpecialtyDTO> updateSpecialty(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody SpecialtyDTO specialtyDTO
    ) throws URISyntaxException {
        log.debug("REST request to update Specialty : {}, {}", id, specialtyDTO);
        if (specialtyDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, specialtyDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!specialtyRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        specialtyDTO = specialtyService.update(specialtyDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, specialtyDTO.getId().toString()))
            .body(specialtyDTO);
    }

    /**
     * {@code PATCH  /specialties/:id} : Partial updates given fields of an existing specialty, field will ignore if it is null
     *
     * @param id the id of the specialtyDTO to save.
     * @param specialtyDTO the specialtyDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated specialtyDTO,
     * or with status {@code 400 (Bad Request)} if the specialtyDTO is not valid,
     * or with status {@code 404 (Not Found)} if the specialtyDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the specialtyDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<SpecialtyDTO> partialUpdateSpecialty(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody SpecialtyDTO specialtyDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update Specialty partially : {}, {}", id, specialtyDTO);
        if (specialtyDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, specialtyDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!specialtyRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<SpecialtyDTO> result = specialtyService.partialUpdate(specialtyDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, specialtyDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /specialties} : get all the specialties.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of specialties in body.
     */
    @GetMapping("")
    public ResponseEntity<List<SpecialtyDTO>> getAllSpecialties(@org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        log.debug("REST request to get a page of Specialties");
        Page<SpecialtyDTO> page = specialtyService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /specialties/:id} : get the "id" specialty.
     *
     * @param id the id of the specialtyDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the specialtyDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<SpecialtyDTO> getSpecialty(@PathVariable("id") Long id) {
        log.debug("REST request to get Specialty : {}", id);
        Optional<SpecialtyDTO> specialtyDTO = specialtyService.findOne(id);
        return ResponseUtil.wrapOrNotFound(specialtyDTO);
    }

    /**
     * {@code DELETE  /specialties/:id} : delete the "id" specialty.
     *
     * @param id the id of the specialtyDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSpecialty(@PathVariable("id") Long id) {
        log.debug("REST request to delete Specialty : {}", id);
        specialtyService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }

    /**
     * {@code SEARCH  /specialties/_search?query=:query} : search for the specialty corresponding
     * to the query.
     *
     * @param query the query of the specialty search.
     * @param pageable the pagination information.
     * @return the result of the search.
     */
    @GetMapping("/_search")
    public ResponseEntity<List<SpecialtyDTO>> searchSpecialties(
        @RequestParam("query") String query,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        log.debug("REST request to search for a page of Specialties for query {}", query);
        try {
            Page<SpecialtyDTO> page = specialtyService.search(query, pageable);
            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
            return ResponseEntity.ok().headers(headers).body(page.getContent());
        } catch (RuntimeException e) {
            throw ElasticsearchExceptionMapper.mapException(e);
        }
    }
}
