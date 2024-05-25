package org.petclinic.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.petclinic.domain.VisitAsserts.*;
import static org.petclinic.web.rest.TestUtil.createUpdateProxyForBean;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.assertj.core.util.IterableUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.petclinic.IntegrationTest;
import org.petclinic.domain.Visit;
import org.petclinic.repository.VisitRepository;
import org.petclinic.repository.search.VisitSearchRepository;
import org.petclinic.service.VisitService;
import org.petclinic.service.dto.VisitDTO;
import org.petclinic.service.mapper.VisitMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Streamable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link VisitResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class VisitResourceIT {

    private static final LocalDate DEFAULT_VISIT_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_VISIT_DATE = LocalDate.now(ZoneId.systemDefault());

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/visits";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/visits/_search";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private VisitRepository visitRepository;

    @Mock
    private VisitRepository visitRepositoryMock;

    @Autowired
    private VisitMapper visitMapper;

    @Mock
    private VisitService visitServiceMock;

    @Autowired
    private VisitSearchRepository visitSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restVisitMockMvc;

    private Visit visit;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Visit createEntity(EntityManager em) {
        Visit visit = new Visit().visitDate(DEFAULT_VISIT_DATE).description(DEFAULT_DESCRIPTION);
        return visit;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Visit createUpdatedEntity(EntityManager em) {
        Visit visit = new Visit().visitDate(UPDATED_VISIT_DATE).description(UPDATED_DESCRIPTION);
        return visit;
    }

    @AfterEach
    public void cleanupElasticSearchRepository() {
        visitSearchRepository.deleteAll();
        assertThat(visitSearchRepository.count()).isEqualTo(0);
    }

    @BeforeEach
    public void initTest() {
        visit = createEntity(em);
    }

    @Test
    @Transactional
    void createVisit() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(visitSearchRepository.findAll());
        // Create the Visit
        VisitDTO visitDTO = visitMapper.toDto(visit);
        var returnedVisitDTO = om.readValue(
            restVisitMockMvc
                .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(visitDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            VisitDTO.class
        );

        // Validate the Visit in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedVisit = visitMapper.toEntity(returnedVisitDTO);
        assertVisitUpdatableFieldsEquals(returnedVisit, getPersistedVisit(returnedVisit));

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(visitSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });
    }

    @Test
    @Transactional
    void createVisitWithExistingId() throws Exception {
        // Create the Visit with an existing ID
        visit.setId(1L);
        VisitDTO visitDTO = visitMapper.toDto(visit);

        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(visitSearchRepository.findAll());

        // An entity with an existing ID cannot be created, so this API call must fail
        restVisitMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(visitDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Visit in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(visitSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void getAllVisits() throws Exception {
        // Initialize the database
        visitRepository.saveAndFlush(visit);

        // Get all the visitList
        restVisitMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(visit.getId().intValue())))
            .andExpect(jsonPath("$.[*].visitDate").value(hasItem(DEFAULT_VISIT_DATE.toString())))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllVisitsWithEagerRelationshipsIsEnabled() throws Exception {
        when(visitServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restVisitMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(visitServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllVisitsWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(visitServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restVisitMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(visitRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getVisit() throws Exception {
        // Initialize the database
        visitRepository.saveAndFlush(visit);

        // Get the visit
        restVisitMockMvc
            .perform(get(ENTITY_API_URL_ID, visit.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(visit.getId().intValue()))
            .andExpect(jsonPath("$.visitDate").value(DEFAULT_VISIT_DATE.toString()))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION));
    }

    @Test
    @Transactional
    void getNonExistingVisit() throws Exception {
        // Get the visit
        restVisitMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingVisit() throws Exception {
        // Initialize the database
        visitRepository.saveAndFlush(visit);

        long databaseSizeBeforeUpdate = getRepositoryCount();
        visitSearchRepository.save(visit);
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(visitSearchRepository.findAll());

        // Update the visit
        Visit updatedVisit = visitRepository.findById(visit.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedVisit are not directly saved in db
        em.detach(updatedVisit);
        updatedVisit.visitDate(UPDATED_VISIT_DATE).description(UPDATED_DESCRIPTION);
        VisitDTO visitDTO = visitMapper.toDto(updatedVisit);

        restVisitMockMvc
            .perform(
                put(ENTITY_API_URL_ID, visitDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(visitDTO))
            )
            .andExpect(status().isOk());

        // Validate the Visit in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedVisitToMatchAllProperties(updatedVisit);

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(visitSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<Visit> visitSearchList = Streamable.of(visitSearchRepository.findAll()).toList();
                Visit testVisitSearch = visitSearchList.get(searchDatabaseSizeAfter - 1);

                assertVisitAllPropertiesEquals(testVisitSearch, updatedVisit);
            });
    }

    @Test
    @Transactional
    void putNonExistingVisit() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(visitSearchRepository.findAll());
        visit.setId(longCount.incrementAndGet());

        // Create the Visit
        VisitDTO visitDTO = visitMapper.toDto(visit);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restVisitMockMvc
            .perform(
                put(ENTITY_API_URL_ID, visitDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(visitDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Visit in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(visitSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithIdMismatchVisit() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(visitSearchRepository.findAll());
        visit.setId(longCount.incrementAndGet());

        // Create the Visit
        VisitDTO visitDTO = visitMapper.toDto(visit);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restVisitMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(visitDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Visit in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(visitSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamVisit() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(visitSearchRepository.findAll());
        visit.setId(longCount.incrementAndGet());

        // Create the Visit
        VisitDTO visitDTO = visitMapper.toDto(visit);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restVisitMockMvc
            .perform(put(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(visitDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Visit in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(visitSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void partialUpdateVisitWithPatch() throws Exception {
        // Initialize the database
        visitRepository.saveAndFlush(visit);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the visit using partial update
        Visit partialUpdatedVisit = new Visit();
        partialUpdatedVisit.setId(visit.getId());

        partialUpdatedVisit.visitDate(UPDATED_VISIT_DATE);

        restVisitMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedVisit.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedVisit))
            )
            .andExpect(status().isOk());

        // Validate the Visit in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertVisitUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedVisit, visit), getPersistedVisit(visit));
    }

    @Test
    @Transactional
    void fullUpdateVisitWithPatch() throws Exception {
        // Initialize the database
        visitRepository.saveAndFlush(visit);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the visit using partial update
        Visit partialUpdatedVisit = new Visit();
        partialUpdatedVisit.setId(visit.getId());

        partialUpdatedVisit.visitDate(UPDATED_VISIT_DATE).description(UPDATED_DESCRIPTION);

        restVisitMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedVisit.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedVisit))
            )
            .andExpect(status().isOk());

        // Validate the Visit in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertVisitUpdatableFieldsEquals(partialUpdatedVisit, getPersistedVisit(partialUpdatedVisit));
    }

    @Test
    @Transactional
    void patchNonExistingVisit() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(visitSearchRepository.findAll());
        visit.setId(longCount.incrementAndGet());

        // Create the Visit
        VisitDTO visitDTO = visitMapper.toDto(visit);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restVisitMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, visitDTO.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(visitDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Visit in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(visitSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithIdMismatchVisit() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(visitSearchRepository.findAll());
        visit.setId(longCount.incrementAndGet());

        // Create the Visit
        VisitDTO visitDTO = visitMapper.toDto(visit);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restVisitMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(visitDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Visit in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(visitSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamVisit() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(visitSearchRepository.findAll());
        visit.setId(longCount.incrementAndGet());

        // Create the Visit
        VisitDTO visitDTO = visitMapper.toDto(visit);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restVisitMockMvc
            .perform(patch(ENTITY_API_URL).with(csrf()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(visitDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Visit in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(visitSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void deleteVisit() throws Exception {
        // Initialize the database
        visitRepository.saveAndFlush(visit);
        visitRepository.save(visit);
        visitSearchRepository.save(visit);

        long databaseSizeBeforeDelete = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(visitSearchRepository.findAll());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the visit
        restVisitMockMvc
            .perform(delete(ENTITY_API_URL_ID, visit.getId()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(visitSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    @Transactional
    void searchVisit() throws Exception {
        // Initialize the database
        visit = visitRepository.saveAndFlush(visit);
        visitSearchRepository.save(visit);

        // Search the visit
        restVisitMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + visit.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(visit.getId().intValue())))
            .andExpect(jsonPath("$.[*].visitDate").value(hasItem(DEFAULT_VISIT_DATE.toString())))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)));
    }

    protected long getRepositoryCount() {
        return visitRepository.count();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected Visit getPersistedVisit(Visit visit) {
        return visitRepository.findById(visit.getId()).orElseThrow();
    }

    protected void assertPersistedVisitToMatchAllProperties(Visit expectedVisit) {
        assertVisitAllPropertiesEquals(expectedVisit, getPersistedVisit(expectedVisit));
    }

    protected void assertPersistedVisitToMatchUpdatableProperties(Visit expectedVisit) {
        assertVisitAllUpdatablePropertiesEquals(expectedVisit, getPersistedVisit(expectedVisit));
    }
}
