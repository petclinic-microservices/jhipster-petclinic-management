package org.petclinic.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.petclinic.domain.VetAsserts.*;
import static org.petclinic.web.rest.TestUtil.createUpdateProxyForBean;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
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
import org.petclinic.domain.Vet;
import org.petclinic.repository.VetRepository;
import org.petclinic.repository.search.VetSearchRepository;
import org.petclinic.service.VetService;
import org.petclinic.service.dto.VetDTO;
import org.petclinic.service.mapper.VetMapper;
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
 * Integration tests for the {@link VetResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class VetResourceIT {

    private static final String DEFAULT_FIRST_NAME = "AAAAAAAAAA";
    private static final String UPDATED_FIRST_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_LAST_NAME = "AAAAAAAAAA";
    private static final String UPDATED_LAST_NAME = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/vets";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/vets/_search";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private VetRepository vetRepository;

    @Mock
    private VetRepository vetRepositoryMock;

    @Autowired
    private VetMapper vetMapper;

    @Mock
    private VetService vetServiceMock;

    @Autowired
    private VetSearchRepository vetSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restVetMockMvc;

    private Vet vet;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Vet createEntity(EntityManager em) {
        Vet vet = new Vet().firstName(DEFAULT_FIRST_NAME).lastName(DEFAULT_LAST_NAME);
        return vet;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Vet createUpdatedEntity(EntityManager em) {
        Vet vet = new Vet().firstName(UPDATED_FIRST_NAME).lastName(UPDATED_LAST_NAME);
        return vet;
    }

    @AfterEach
    public void cleanupElasticSearchRepository() {
        vetSearchRepository.deleteAll();
        assertThat(vetSearchRepository.count()).isEqualTo(0);
    }

    @BeforeEach
    public void initTest() {
        vet = createEntity(em);
    }

    @Test
    @Transactional
    void createVet() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(vetSearchRepository.findAll());
        // Create the Vet
        VetDTO vetDTO = vetMapper.toDto(vet);
        var returnedVetDTO = om.readValue(
            restVetMockMvc
                .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(vetDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            VetDTO.class
        );

        // Validate the Vet in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedVet = vetMapper.toEntity(returnedVetDTO);
        assertVetUpdatableFieldsEquals(returnedVet, getPersistedVet(returnedVet));

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(vetSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });
    }

    @Test
    @Transactional
    void createVetWithExistingId() throws Exception {
        // Create the Vet with an existing ID
        vet.setId(1L);
        VetDTO vetDTO = vetMapper.toDto(vet);

        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(vetSearchRepository.findAll());

        // An entity with an existing ID cannot be created, so this API call must fail
        restVetMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(vetDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Vet in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(vetSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void getAllVets() throws Exception {
        // Initialize the database
        vetRepository.saveAndFlush(vet);

        // Get all the vetList
        restVetMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(vet.getId().intValue())))
            .andExpect(jsonPath("$.[*].firstName").value(hasItem(DEFAULT_FIRST_NAME)))
            .andExpect(jsonPath("$.[*].lastName").value(hasItem(DEFAULT_LAST_NAME)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllVetsWithEagerRelationshipsIsEnabled() throws Exception {
        when(vetServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restVetMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(vetServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllVetsWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(vetServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restVetMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(vetRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getVet() throws Exception {
        // Initialize the database
        vetRepository.saveAndFlush(vet);

        // Get the vet
        restVetMockMvc
            .perform(get(ENTITY_API_URL_ID, vet.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(vet.getId().intValue()))
            .andExpect(jsonPath("$.firstName").value(DEFAULT_FIRST_NAME))
            .andExpect(jsonPath("$.lastName").value(DEFAULT_LAST_NAME));
    }

    @Test
    @Transactional
    void getNonExistingVet() throws Exception {
        // Get the vet
        restVetMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingVet() throws Exception {
        // Initialize the database
        vetRepository.saveAndFlush(vet);

        long databaseSizeBeforeUpdate = getRepositoryCount();
        vetSearchRepository.save(vet);
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(vetSearchRepository.findAll());

        // Update the vet
        Vet updatedVet = vetRepository.findById(vet.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedVet are not directly saved in db
        em.detach(updatedVet);
        updatedVet.firstName(UPDATED_FIRST_NAME).lastName(UPDATED_LAST_NAME);
        VetDTO vetDTO = vetMapper.toDto(updatedVet);

        restVetMockMvc
            .perform(
                put(ENTITY_API_URL_ID, vetDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(vetDTO))
            )
            .andExpect(status().isOk());

        // Validate the Vet in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedVetToMatchAllProperties(updatedVet);

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(vetSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<Vet> vetSearchList = Streamable.of(vetSearchRepository.findAll()).toList();
                Vet testVetSearch = vetSearchList.get(searchDatabaseSizeAfter - 1);

                assertVetAllPropertiesEquals(testVetSearch, updatedVet);
            });
    }

    @Test
    @Transactional
    void putNonExistingVet() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(vetSearchRepository.findAll());
        vet.setId(longCount.incrementAndGet());

        // Create the Vet
        VetDTO vetDTO = vetMapper.toDto(vet);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restVetMockMvc
            .perform(
                put(ENTITY_API_URL_ID, vetDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(vetDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Vet in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(vetSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithIdMismatchVet() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(vetSearchRepository.findAll());
        vet.setId(longCount.incrementAndGet());

        // Create the Vet
        VetDTO vetDTO = vetMapper.toDto(vet);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restVetMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(vetDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Vet in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(vetSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamVet() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(vetSearchRepository.findAll());
        vet.setId(longCount.incrementAndGet());

        // Create the Vet
        VetDTO vetDTO = vetMapper.toDto(vet);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restVetMockMvc
            .perform(put(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(vetDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Vet in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(vetSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void partialUpdateVetWithPatch() throws Exception {
        // Initialize the database
        vetRepository.saveAndFlush(vet);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the vet using partial update
        Vet partialUpdatedVet = new Vet();
        partialUpdatedVet.setId(vet.getId());

        restVetMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedVet.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedVet))
            )
            .andExpect(status().isOk());

        // Validate the Vet in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertVetUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedVet, vet), getPersistedVet(vet));
    }

    @Test
    @Transactional
    void fullUpdateVetWithPatch() throws Exception {
        // Initialize the database
        vetRepository.saveAndFlush(vet);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the vet using partial update
        Vet partialUpdatedVet = new Vet();
        partialUpdatedVet.setId(vet.getId());

        partialUpdatedVet.firstName(UPDATED_FIRST_NAME).lastName(UPDATED_LAST_NAME);

        restVetMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedVet.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedVet))
            )
            .andExpect(status().isOk());

        // Validate the Vet in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertVetUpdatableFieldsEquals(partialUpdatedVet, getPersistedVet(partialUpdatedVet));
    }

    @Test
    @Transactional
    void patchNonExistingVet() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(vetSearchRepository.findAll());
        vet.setId(longCount.incrementAndGet());

        // Create the Vet
        VetDTO vetDTO = vetMapper.toDto(vet);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restVetMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, vetDTO.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(vetDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Vet in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(vetSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithIdMismatchVet() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(vetSearchRepository.findAll());
        vet.setId(longCount.incrementAndGet());

        // Create the Vet
        VetDTO vetDTO = vetMapper.toDto(vet);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restVetMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(vetDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Vet in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(vetSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamVet() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(vetSearchRepository.findAll());
        vet.setId(longCount.incrementAndGet());

        // Create the Vet
        VetDTO vetDTO = vetMapper.toDto(vet);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restVetMockMvc
            .perform(patch(ENTITY_API_URL).with(csrf()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(vetDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Vet in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(vetSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void deleteVet() throws Exception {
        // Initialize the database
        vetRepository.saveAndFlush(vet);
        vetRepository.save(vet);
        vetSearchRepository.save(vet);

        long databaseSizeBeforeDelete = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(vetSearchRepository.findAll());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the vet
        restVetMockMvc
            .perform(delete(ENTITY_API_URL_ID, vet.getId()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(vetSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    @Transactional
    void searchVet() throws Exception {
        // Initialize the database
        vet = vetRepository.saveAndFlush(vet);
        vetSearchRepository.save(vet);

        // Search the vet
        restVetMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + vet.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(vet.getId().intValue())))
            .andExpect(jsonPath("$.[*].firstName").value(hasItem(DEFAULT_FIRST_NAME)))
            .andExpect(jsonPath("$.[*].lastName").value(hasItem(DEFAULT_LAST_NAME)));
    }

    protected long getRepositoryCount() {
        return vetRepository.count();
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

    protected Vet getPersistedVet(Vet vet) {
        return vetRepository.findById(vet.getId()).orElseThrow();
    }

    protected void assertPersistedVetToMatchAllProperties(Vet expectedVet) {
        assertVetAllPropertiesEquals(expectedVet, getPersistedVet(expectedVet));
    }

    protected void assertPersistedVetToMatchUpdatableProperties(Vet expectedVet) {
        assertVetAllUpdatablePropertiesEquals(expectedVet, getPersistedVet(expectedVet));
    }
}
