package org.petclinic.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.petclinic.domain.PetTypeAsserts.*;
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
import org.petclinic.domain.PetType;
import org.petclinic.repository.PetTypeRepository;
import org.petclinic.repository.search.PetTypeSearchRepository;
import org.petclinic.service.dto.PetTypeDTO;
import org.petclinic.service.mapper.PetTypeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.util.Streamable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link PetTypeResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class PetTypeResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/pet-types";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/pet-types/_search";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private PetTypeRepository petTypeRepository;

    @Autowired
    private PetTypeMapper petTypeMapper;

    @Autowired
    private PetTypeSearchRepository petTypeSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restPetTypeMockMvc;

    private PetType petType;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static PetType createEntity(EntityManager em) {
        PetType petType = new PetType().name(DEFAULT_NAME);
        return petType;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static PetType createUpdatedEntity(EntityManager em) {
        PetType petType = new PetType().name(UPDATED_NAME);
        return petType;
    }

    @AfterEach
    public void cleanupElasticSearchRepository() {
        petTypeSearchRepository.deleteAll();
        assertThat(petTypeSearchRepository.count()).isEqualTo(0);
    }

    @BeforeEach
    public void initTest() {
        petType = createEntity(em);
    }

    @Test
    @Transactional
    void createPetType() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(petTypeSearchRepository.findAll());
        // Create the PetType
        PetTypeDTO petTypeDTO = petTypeMapper.toDto(petType);
        var returnedPetTypeDTO = om.readValue(
            restPetTypeMockMvc
                .perform(
                    post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(petTypeDTO))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            PetTypeDTO.class
        );

        // Validate the PetType in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedPetType = petTypeMapper.toEntity(returnedPetTypeDTO);
        assertPetTypeUpdatableFieldsEquals(returnedPetType, getPersistedPetType(returnedPetType));

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(petTypeSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });
    }

    @Test
    @Transactional
    void createPetTypeWithExistingId() throws Exception {
        // Create the PetType with an existing ID
        petType.setId(1L);
        PetTypeDTO petTypeDTO = petTypeMapper.toDto(petType);

        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(petTypeSearchRepository.findAll());

        // An entity with an existing ID cannot be created, so this API call must fail
        restPetTypeMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(petTypeDTO)))
            .andExpect(status().isBadRequest());

        // Validate the PetType in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(petTypeSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void getAllPetTypes() throws Exception {
        // Initialize the database
        petTypeRepository.saveAndFlush(petType);

        // Get all the petTypeList
        restPetTypeMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(petType.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)));
    }

    @Test
    @Transactional
    void getPetType() throws Exception {
        // Initialize the database
        petTypeRepository.saveAndFlush(petType);

        // Get the petType
        restPetTypeMockMvc
            .perform(get(ENTITY_API_URL_ID, petType.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(petType.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME));
    }

    @Test
    @Transactional
    void getNonExistingPetType() throws Exception {
        // Get the petType
        restPetTypeMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingPetType() throws Exception {
        // Initialize the database
        petTypeRepository.saveAndFlush(petType);

        long databaseSizeBeforeUpdate = getRepositoryCount();
        petTypeSearchRepository.save(petType);
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(petTypeSearchRepository.findAll());

        // Update the petType
        PetType updatedPetType = petTypeRepository.findById(petType.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedPetType are not directly saved in db
        em.detach(updatedPetType);
        updatedPetType.name(UPDATED_NAME);
        PetTypeDTO petTypeDTO = petTypeMapper.toDto(updatedPetType);

        restPetTypeMockMvc
            .perform(
                put(ENTITY_API_URL_ID, petTypeDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(petTypeDTO))
            )
            .andExpect(status().isOk());

        // Validate the PetType in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedPetTypeToMatchAllProperties(updatedPetType);

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(petTypeSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<PetType> petTypeSearchList = Streamable.of(petTypeSearchRepository.findAll()).toList();
                PetType testPetTypeSearch = petTypeSearchList.get(searchDatabaseSizeAfter - 1);

                assertPetTypeAllPropertiesEquals(testPetTypeSearch, updatedPetType);
            });
    }

    @Test
    @Transactional
    void putNonExistingPetType() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(petTypeSearchRepository.findAll());
        petType.setId(longCount.incrementAndGet());

        // Create the PetType
        PetTypeDTO petTypeDTO = petTypeMapper.toDto(petType);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPetTypeMockMvc
            .perform(
                put(ENTITY_API_URL_ID, petTypeDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(petTypeDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the PetType in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(petTypeSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithIdMismatchPetType() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(petTypeSearchRepository.findAll());
        petType.setId(longCount.incrementAndGet());

        // Create the PetType
        PetTypeDTO petTypeDTO = petTypeMapper.toDto(petType);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPetTypeMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(petTypeDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the PetType in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(petTypeSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamPetType() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(petTypeSearchRepository.findAll());
        petType.setId(longCount.incrementAndGet());

        // Create the PetType
        PetTypeDTO petTypeDTO = petTypeMapper.toDto(petType);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPetTypeMockMvc
            .perform(put(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(petTypeDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the PetType in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(petTypeSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void partialUpdatePetTypeWithPatch() throws Exception {
        // Initialize the database
        petTypeRepository.saveAndFlush(petType);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the petType using partial update
        PetType partialUpdatedPetType = new PetType();
        partialUpdatedPetType.setId(petType.getId());

        partialUpdatedPetType.name(UPDATED_NAME);

        restPetTypeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedPetType.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedPetType))
            )
            .andExpect(status().isOk());

        // Validate the PetType in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPetTypeUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedPetType, petType), getPersistedPetType(petType));
    }

    @Test
    @Transactional
    void fullUpdatePetTypeWithPatch() throws Exception {
        // Initialize the database
        petTypeRepository.saveAndFlush(petType);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the petType using partial update
        PetType partialUpdatedPetType = new PetType();
        partialUpdatedPetType.setId(petType.getId());

        partialUpdatedPetType.name(UPDATED_NAME);

        restPetTypeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedPetType.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedPetType))
            )
            .andExpect(status().isOk());

        // Validate the PetType in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPetTypeUpdatableFieldsEquals(partialUpdatedPetType, getPersistedPetType(partialUpdatedPetType));
    }

    @Test
    @Transactional
    void patchNonExistingPetType() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(petTypeSearchRepository.findAll());
        petType.setId(longCount.incrementAndGet());

        // Create the PetType
        PetTypeDTO petTypeDTO = petTypeMapper.toDto(petType);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPetTypeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, petTypeDTO.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(petTypeDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the PetType in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(petTypeSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithIdMismatchPetType() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(petTypeSearchRepository.findAll());
        petType.setId(longCount.incrementAndGet());

        // Create the PetType
        PetTypeDTO petTypeDTO = petTypeMapper.toDto(petType);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPetTypeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(petTypeDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the PetType in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(petTypeSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamPetType() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(petTypeSearchRepository.findAll());
        petType.setId(longCount.incrementAndGet());

        // Create the PetType
        PetTypeDTO petTypeDTO = petTypeMapper.toDto(petType);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPetTypeMockMvc
            .perform(
                patch(ENTITY_API_URL).with(csrf()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(petTypeDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the PetType in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(petTypeSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void deletePetType() throws Exception {
        // Initialize the database
        petTypeRepository.saveAndFlush(petType);
        petTypeRepository.save(petType);
        petTypeSearchRepository.save(petType);

        long databaseSizeBeforeDelete = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(petTypeSearchRepository.findAll());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the petType
        restPetTypeMockMvc
            .perform(delete(ENTITY_API_URL_ID, petType.getId()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(petTypeSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    @Transactional
    void searchPetType() throws Exception {
        // Initialize the database
        petType = petTypeRepository.saveAndFlush(petType);
        petTypeSearchRepository.save(petType);

        // Search the petType
        restPetTypeMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + petType.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(petType.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)));
    }

    protected long getRepositoryCount() {
        return petTypeRepository.count();
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

    protected PetType getPersistedPetType(PetType petType) {
        return petTypeRepository.findById(petType.getId()).orElseThrow();
    }

    protected void assertPersistedPetTypeToMatchAllProperties(PetType expectedPetType) {
        assertPetTypeAllPropertiesEquals(expectedPetType, getPersistedPetType(expectedPetType));
    }

    protected void assertPersistedPetTypeToMatchUpdatableProperties(PetType expectedPetType) {
        assertPetTypeAllUpdatablePropertiesEquals(expectedPetType, getPersistedPetType(expectedPetType));
    }
}
