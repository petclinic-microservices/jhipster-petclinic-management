package org.petclinic.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.petclinic.domain.PetAsserts.*;
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
import org.petclinic.domain.Pet;
import org.petclinic.repository.PetRepository;
import org.petclinic.repository.search.PetSearchRepository;
import org.petclinic.service.PetService;
import org.petclinic.service.dto.PetDTO;
import org.petclinic.service.mapper.PetMapper;
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
 * Integration tests for the {@link PetResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class PetResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final LocalDate DEFAULT_BIRTH_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_BIRTH_DATE = LocalDate.now(ZoneId.systemDefault());

    private static final String ENTITY_API_URL = "/api/pets";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/pets/_search";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private PetRepository petRepository;

    @Mock
    private PetRepository petRepositoryMock;

    @Autowired
    private PetMapper petMapper;

    @Mock
    private PetService petServiceMock;

    @Autowired
    private PetSearchRepository petSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restPetMockMvc;

    private Pet pet;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Pet createEntity(EntityManager em) {
        Pet pet = new Pet().name(DEFAULT_NAME).birthDate(DEFAULT_BIRTH_DATE);
        return pet;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Pet createUpdatedEntity(EntityManager em) {
        Pet pet = new Pet().name(UPDATED_NAME).birthDate(UPDATED_BIRTH_DATE);
        return pet;
    }

    @AfterEach
    public void cleanupElasticSearchRepository() {
        petSearchRepository.deleteAll();
        assertThat(petSearchRepository.count()).isEqualTo(0);
    }

    @BeforeEach
    public void initTest() {
        pet = createEntity(em);
    }

    @Test
    @Transactional
    void createPet() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(petSearchRepository.findAll());
        // Create the Pet
        PetDTO petDTO = petMapper.toDto(pet);
        var returnedPetDTO = om.readValue(
            restPetMockMvc
                .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(petDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            PetDTO.class
        );

        // Validate the Pet in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedPet = petMapper.toEntity(returnedPetDTO);
        assertPetUpdatableFieldsEquals(returnedPet, getPersistedPet(returnedPet));

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(petSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });
    }

    @Test
    @Transactional
    void createPetWithExistingId() throws Exception {
        // Create the Pet with an existing ID
        pet.setId(1L);
        PetDTO petDTO = petMapper.toDto(pet);

        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(petSearchRepository.findAll());

        // An entity with an existing ID cannot be created, so this API call must fail
        restPetMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(petDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Pet in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(petSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void getAllPets() throws Exception {
        // Initialize the database
        petRepository.saveAndFlush(pet);

        // Get all the petList
        restPetMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(pet.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].birthDate").value(hasItem(DEFAULT_BIRTH_DATE.toString())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllPetsWithEagerRelationshipsIsEnabled() throws Exception {
        when(petServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restPetMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(petServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllPetsWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(petServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restPetMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(petRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getPet() throws Exception {
        // Initialize the database
        petRepository.saveAndFlush(pet);

        // Get the pet
        restPetMockMvc
            .perform(get(ENTITY_API_URL_ID, pet.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(pet.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.birthDate").value(DEFAULT_BIRTH_DATE.toString()));
    }

    @Test
    @Transactional
    void getNonExistingPet() throws Exception {
        // Get the pet
        restPetMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingPet() throws Exception {
        // Initialize the database
        petRepository.saveAndFlush(pet);

        long databaseSizeBeforeUpdate = getRepositoryCount();
        petSearchRepository.save(pet);
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(petSearchRepository.findAll());

        // Update the pet
        Pet updatedPet = petRepository.findById(pet.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedPet are not directly saved in db
        em.detach(updatedPet);
        updatedPet.name(UPDATED_NAME).birthDate(UPDATED_BIRTH_DATE);
        PetDTO petDTO = petMapper.toDto(updatedPet);

        restPetMockMvc
            .perform(
                put(ENTITY_API_URL_ID, petDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(petDTO))
            )
            .andExpect(status().isOk());

        // Validate the Pet in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedPetToMatchAllProperties(updatedPet);

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(petSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<Pet> petSearchList = Streamable.of(petSearchRepository.findAll()).toList();
                Pet testPetSearch = petSearchList.get(searchDatabaseSizeAfter - 1);

                assertPetAllPropertiesEquals(testPetSearch, updatedPet);
            });
    }

    @Test
    @Transactional
    void putNonExistingPet() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(petSearchRepository.findAll());
        pet.setId(longCount.incrementAndGet());

        // Create the Pet
        PetDTO petDTO = petMapper.toDto(pet);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPetMockMvc
            .perform(
                put(ENTITY_API_URL_ID, petDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(petDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Pet in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(petSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithIdMismatchPet() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(petSearchRepository.findAll());
        pet.setId(longCount.incrementAndGet());

        // Create the Pet
        PetDTO petDTO = petMapper.toDto(pet);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPetMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(petDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Pet in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(petSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamPet() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(petSearchRepository.findAll());
        pet.setId(longCount.incrementAndGet());

        // Create the Pet
        PetDTO petDTO = petMapper.toDto(pet);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPetMockMvc
            .perform(put(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(petDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Pet in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(petSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void partialUpdatePetWithPatch() throws Exception {
        // Initialize the database
        petRepository.saveAndFlush(pet);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the pet using partial update
        Pet partialUpdatedPet = new Pet();
        partialUpdatedPet.setId(pet.getId());

        partialUpdatedPet.birthDate(UPDATED_BIRTH_DATE);

        restPetMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedPet.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedPet))
            )
            .andExpect(status().isOk());

        // Validate the Pet in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPetUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedPet, pet), getPersistedPet(pet));
    }

    @Test
    @Transactional
    void fullUpdatePetWithPatch() throws Exception {
        // Initialize the database
        petRepository.saveAndFlush(pet);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the pet using partial update
        Pet partialUpdatedPet = new Pet();
        partialUpdatedPet.setId(pet.getId());

        partialUpdatedPet.name(UPDATED_NAME).birthDate(UPDATED_BIRTH_DATE);

        restPetMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedPet.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedPet))
            )
            .andExpect(status().isOk());

        // Validate the Pet in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPetUpdatableFieldsEquals(partialUpdatedPet, getPersistedPet(partialUpdatedPet));
    }

    @Test
    @Transactional
    void patchNonExistingPet() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(petSearchRepository.findAll());
        pet.setId(longCount.incrementAndGet());

        // Create the Pet
        PetDTO petDTO = petMapper.toDto(pet);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPetMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, petDTO.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(petDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Pet in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(petSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithIdMismatchPet() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(petSearchRepository.findAll());
        pet.setId(longCount.incrementAndGet());

        // Create the Pet
        PetDTO petDTO = petMapper.toDto(pet);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPetMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(petDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Pet in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(petSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamPet() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(petSearchRepository.findAll());
        pet.setId(longCount.incrementAndGet());

        // Create the Pet
        PetDTO petDTO = petMapper.toDto(pet);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPetMockMvc
            .perform(patch(ENTITY_API_URL).with(csrf()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(petDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Pet in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(petSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void deletePet() throws Exception {
        // Initialize the database
        petRepository.saveAndFlush(pet);
        petRepository.save(pet);
        petSearchRepository.save(pet);

        long databaseSizeBeforeDelete = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(petSearchRepository.findAll());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the pet
        restPetMockMvc
            .perform(delete(ENTITY_API_URL_ID, pet.getId()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(petSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    @Transactional
    void searchPet() throws Exception {
        // Initialize the database
        pet = petRepository.saveAndFlush(pet);
        petSearchRepository.save(pet);

        // Search the pet
        restPetMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + pet.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(pet.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].birthDate").value(hasItem(DEFAULT_BIRTH_DATE.toString())));
    }

    protected long getRepositoryCount() {
        return petRepository.count();
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

    protected Pet getPersistedPet(Pet pet) {
        return petRepository.findById(pet.getId()).orElseThrow();
    }

    protected void assertPersistedPetToMatchAllProperties(Pet expectedPet) {
        assertPetAllPropertiesEquals(expectedPet, getPersistedPet(expectedPet));
    }

    protected void assertPersistedPetToMatchUpdatableProperties(Pet expectedPet) {
        assertPetAllUpdatablePropertiesEquals(expectedPet, getPersistedPet(expectedPet));
    }
}
