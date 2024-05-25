package org.petclinic.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.petclinic.domain.SpecialtyAsserts.*;
import static org.petclinic.web.rest.TestUtil.createUpdateProxyForBean;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.assertj.core.util.IterableUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.petclinic.IntegrationTest;
import org.petclinic.domain.Specialty;
import org.petclinic.repository.SpecialtyRepository;
import org.petclinic.repository.search.SpecialtySearchRepository;
import org.petclinic.service.dto.SpecialtyDTO;
import org.petclinic.service.mapper.SpecialtyMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.util.Streamable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link SpecialtyResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class SpecialtyResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/specialties";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/specialties/_search";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private SpecialtyRepository specialtyRepository;

    @Autowired
    private SpecialtyMapper specialtyMapper;

    @Autowired
    private SpecialtySearchRepository specialtySearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restSpecialtyMockMvc;

    private Specialty specialty;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Specialty createEntity(EntityManager em) {
        Specialty specialty = new Specialty().name(DEFAULT_NAME);
        return specialty;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Specialty createUpdatedEntity(EntityManager em) {
        Specialty specialty = new Specialty().name(UPDATED_NAME);
        return specialty;
    }

    @AfterEach
    public void cleanupElasticSearchRepository() {
        specialtySearchRepository.deleteAll();
        assertThat(specialtySearchRepository.count()).isEqualTo(0);
    }

    @BeforeEach
    public void initTest() {
        specialty = createEntity(em);
    }

    @Test
    @Transactional
    void createSpecialty() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(specialtySearchRepository.findAll());
        // Create the Specialty
        SpecialtyDTO specialtyDTO = specialtyMapper.toDto(specialty);
        var returnedSpecialtyDTO = om.readValue(
            restSpecialtyMockMvc
                .perform(
                    post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(specialtyDTO))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            SpecialtyDTO.class
        );

        // Validate the Specialty in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedSpecialty = specialtyMapper.toEntity(returnedSpecialtyDTO);
        assertSpecialtyUpdatableFieldsEquals(returnedSpecialty, getPersistedSpecialty(returnedSpecialty));

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(specialtySearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });
    }

    @Test
    @Transactional
    void createSpecialtyWithExistingId() throws Exception {
        // Create the Specialty with an existing ID
        specialty.setId(1L);
        SpecialtyDTO specialtyDTO = specialtyMapper.toDto(specialty);

        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(specialtySearchRepository.findAll());

        // An entity with an existing ID cannot be created, so this API call must fail
        restSpecialtyMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(specialtyDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Specialty in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(specialtySearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void getAllSpecialties() throws Exception {
        // Initialize the database
        specialtyRepository.saveAndFlush(specialty);

        // Get all the specialtyList
        restSpecialtyMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(specialty.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)));
    }

    @Test
    @Transactional
    void getSpecialty() throws Exception {
        // Initialize the database
        specialtyRepository.saveAndFlush(specialty);

        // Get the specialty
        restSpecialtyMockMvc
            .perform(get(ENTITY_API_URL_ID, specialty.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(specialty.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME));
    }

    @Test
    @Transactional
    void getNonExistingSpecialty() throws Exception {
        // Get the specialty
        restSpecialtyMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingSpecialty() throws Exception {
        // Initialize the database
        specialtyRepository.saveAndFlush(specialty);

        long databaseSizeBeforeUpdate = getRepositoryCount();
        specialtySearchRepository.save(specialty);
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(specialtySearchRepository.findAll());

        // Update the specialty
        Specialty updatedSpecialty = specialtyRepository.findById(specialty.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedSpecialty are not directly saved in db
        em.detach(updatedSpecialty);
        updatedSpecialty.name(UPDATED_NAME);
        SpecialtyDTO specialtyDTO = specialtyMapper.toDto(updatedSpecialty);

        restSpecialtyMockMvc
            .perform(
                put(ENTITY_API_URL_ID, specialtyDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(specialtyDTO))
            )
            .andExpect(status().isOk());

        // Validate the Specialty in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedSpecialtyToMatchAllProperties(updatedSpecialty);

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(specialtySearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<Specialty> specialtySearchList = Streamable.of(specialtySearchRepository.findAll()).toList();
                Specialty testSpecialtySearch = specialtySearchList.get(searchDatabaseSizeAfter - 1);

                assertSpecialtyAllPropertiesEquals(testSpecialtySearch, updatedSpecialty);
            });
    }

    @Test
    @Transactional
    void putNonExistingSpecialty() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(specialtySearchRepository.findAll());
        specialty.setId(longCount.incrementAndGet());

        // Create the Specialty
        SpecialtyDTO specialtyDTO = specialtyMapper.toDto(specialty);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restSpecialtyMockMvc
            .perform(
                put(ENTITY_API_URL_ID, specialtyDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(specialtyDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Specialty in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(specialtySearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithIdMismatchSpecialty() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(specialtySearchRepository.findAll());
        specialty.setId(longCount.incrementAndGet());

        // Create the Specialty
        SpecialtyDTO specialtyDTO = specialtyMapper.toDto(specialty);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSpecialtyMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(specialtyDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Specialty in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(specialtySearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamSpecialty() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(specialtySearchRepository.findAll());
        specialty.setId(longCount.incrementAndGet());

        // Create the Specialty
        SpecialtyDTO specialtyDTO = specialtyMapper.toDto(specialty);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSpecialtyMockMvc
            .perform(put(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(specialtyDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Specialty in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(specialtySearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void partialUpdateSpecialtyWithPatch() throws Exception {
        // Initialize the database
        specialtyRepository.saveAndFlush(specialty);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the specialty using partial update
        Specialty partialUpdatedSpecialty = new Specialty();
        partialUpdatedSpecialty.setId(specialty.getId());

        partialUpdatedSpecialty.name(UPDATED_NAME);

        restSpecialtyMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedSpecialty.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedSpecialty))
            )
            .andExpect(status().isOk());

        // Validate the Specialty in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertSpecialtyUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedSpecialty, specialty),
            getPersistedSpecialty(specialty)
        );
    }

    @Test
    @Transactional
    void fullUpdateSpecialtyWithPatch() throws Exception {
        // Initialize the database
        specialtyRepository.saveAndFlush(specialty);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the specialty using partial update
        Specialty partialUpdatedSpecialty = new Specialty();
        partialUpdatedSpecialty.setId(specialty.getId());

        partialUpdatedSpecialty.name(UPDATED_NAME);

        restSpecialtyMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedSpecialty.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedSpecialty))
            )
            .andExpect(status().isOk());

        // Validate the Specialty in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertSpecialtyUpdatableFieldsEquals(partialUpdatedSpecialty, getPersistedSpecialty(partialUpdatedSpecialty));
    }

    @Test
    @Transactional
    void patchNonExistingSpecialty() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(specialtySearchRepository.findAll());
        specialty.setId(longCount.incrementAndGet());

        // Create the Specialty
        SpecialtyDTO specialtyDTO = specialtyMapper.toDto(specialty);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restSpecialtyMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, specialtyDTO.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(specialtyDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Specialty in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(specialtySearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithIdMismatchSpecialty() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(specialtySearchRepository.findAll());
        specialty.setId(longCount.incrementAndGet());

        // Create the Specialty
        SpecialtyDTO specialtyDTO = specialtyMapper.toDto(specialty);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSpecialtyMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(specialtyDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Specialty in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(specialtySearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamSpecialty() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(specialtySearchRepository.findAll());
        specialty.setId(longCount.incrementAndGet());

        // Create the Specialty
        SpecialtyDTO specialtyDTO = specialtyMapper.toDto(specialty);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSpecialtyMockMvc
            .perform(
                patch(ENTITY_API_URL).with(csrf()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(specialtyDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the Specialty in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(specialtySearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void deleteSpecialty() throws Exception {
        // Initialize the database
        specialtyRepository.saveAndFlush(specialty);
        specialtyRepository.save(specialty);
        specialtySearchRepository.save(specialty);

        long databaseSizeBeforeDelete = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(specialtySearchRepository.findAll());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the specialty
        restSpecialtyMockMvc
            .perform(delete(ENTITY_API_URL_ID, specialty.getId()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(specialtySearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    @Transactional
    void searchSpecialty() throws Exception {
        // Initialize the database
        specialty = specialtyRepository.saveAndFlush(specialty);
        specialtySearchRepository.save(specialty);

        // Search the specialty
        restSpecialtyMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + specialty.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(specialty.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)));
    }

    protected long getRepositoryCount() {
        return specialtyRepository.count();
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

    protected Specialty getPersistedSpecialty(Specialty specialty) {
        return specialtyRepository.findById(specialty.getId()).orElseThrow();
    }

    protected void assertPersistedSpecialtyToMatchAllProperties(Specialty expectedSpecialty) {
        assertSpecialtyAllPropertiesEquals(expectedSpecialty, getPersistedSpecialty(expectedSpecialty));
    }

    protected void assertPersistedSpecialtyToMatchUpdatableProperties(Specialty expectedSpecialty) {
        assertSpecialtyAllUpdatablePropertiesEquals(expectedSpecialty, getPersistedSpecialty(expectedSpecialty));
    }
}
