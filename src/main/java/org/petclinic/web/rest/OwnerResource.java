package org.petclinic.web.rest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.petclinic.repository.OwnerRepository;
import org.petclinic.service.OwnerService;
import org.petclinic.service.dto.OwnerDTO;
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
 * REST controller for managing {@link org.petclinic.domain.Owner}.
 */
@RestController
@RequestMapping("/api/owners")
public class OwnerResource {

    private final Logger log = LoggerFactory.getLogger(OwnerResource.class);

    private static final String ENTITY_NAME = "owner";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final OwnerService ownerService;

    private final OwnerRepository ownerRepository;

    public OwnerResource(OwnerService ownerService, OwnerRepository ownerRepository) {
        this.ownerService = ownerService;
        this.ownerRepository = ownerRepository;
    }

    /**
     * {@code POST  /owners} : Create a new owner.
     *
     * @param ownerDTO the ownerDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new ownerDTO, or with status {@code 400 (Bad Request)} if the owner has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<OwnerDTO> createOwner(@Valid @RequestBody OwnerDTO ownerDTO) throws URISyntaxException {
        log.debug("REST request to save Owner : {}", ownerDTO);
        if (ownerDTO.getId() != null) {
            throw new BadRequestAlertException("A new owner cannot already have an ID", ENTITY_NAME, "idexists");
        }
        ownerDTO = ownerService.save(ownerDTO);
        return ResponseEntity.created(new URI("/api/owners/" + ownerDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, ownerDTO.getId().toString()))
            .body(ownerDTO);
    }

    /**
     * {@code PUT  /owners/:id} : Updates an existing owner.
     *
     * @param id the id of the ownerDTO to save.
     * @param ownerDTO the ownerDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated ownerDTO,
     * or with status {@code 400 (Bad Request)} if the ownerDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the ownerDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<OwnerDTO> updateOwner(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody OwnerDTO ownerDTO
    ) throws URISyntaxException {
        log.debug("REST request to update Owner : {}, {}", id, ownerDTO);
        if (ownerDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, ownerDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!ownerRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        ownerDTO = ownerService.update(ownerDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, ownerDTO.getId().toString()))
            .body(ownerDTO);
    }

    /**
     * {@code PATCH  /owners/:id} : Partial updates given fields of an existing owner, field will ignore if it is null
     *
     * @param id the id of the ownerDTO to save.
     * @param ownerDTO the ownerDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated ownerDTO,
     * or with status {@code 400 (Bad Request)} if the ownerDTO is not valid,
     * or with status {@code 404 (Not Found)} if the ownerDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the ownerDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<OwnerDTO> partialUpdateOwner(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody OwnerDTO ownerDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update Owner partially : {}, {}", id, ownerDTO);
        if (ownerDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, ownerDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!ownerRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<OwnerDTO> result = ownerService.partialUpdate(ownerDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, ownerDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /owners} : get all the owners.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of owners in body.
     */
    @GetMapping("")
    public ResponseEntity<List<OwnerDTO>> getAllOwners(@org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        log.debug("REST request to get a page of Owners");
        Page<OwnerDTO> page = ownerService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /owners/:id} : get the "id" owner.
     *
     * @param id the id of the ownerDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the ownerDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<OwnerDTO> getOwner(@PathVariable("id") Long id) {
        log.debug("REST request to get Owner : {}", id);
        Optional<OwnerDTO> ownerDTO = ownerService.findOne(id);
        return ResponseUtil.wrapOrNotFound(ownerDTO);
    }

    /**
     * {@code DELETE  /owners/:id} : delete the "id" owner.
     *
     * @param id the id of the ownerDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOwner(@PathVariable("id") Long id) {
        log.debug("REST request to delete Owner : {}", id);
        ownerService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }

    /**
     * {@code SEARCH  /owners/_search?query=:query} : search for the owner corresponding
     * to the query.
     *
     * @param query the query of the owner search.
     * @param pageable the pagination information.
     * @return the result of the search.
     */
    @GetMapping("/_search")
    public ResponseEntity<List<OwnerDTO>> searchOwners(
        @RequestParam("query") String query,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        log.debug("REST request to search for a page of Owners for query {}", query);
        try {
            Page<OwnerDTO> page = ownerService.search(query, pageable);
            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
            return ResponseEntity.ok().headers(headers).body(page.getContent());
        } catch (RuntimeException e) {
            throw ElasticsearchExceptionMapper.mapException(e);
        }
    }
}
