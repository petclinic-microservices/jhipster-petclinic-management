package org.petclinic.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.petclinic.domain.VetSpecialtyAsserts.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.assertj.core.util.IterableUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.petclinic.IntegrationTest;
import org.petclinic.domain.VetSpecialty;
import org.petclinic.repository.VetSpecialtyRepository;
import org.petclinic.repository.search.VetSpecialtySearchRepository;
import org.petclinic.service.dto.VetSpecialtyDTO;
import org.petclinic.service.mapper.VetSpecialtyMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link VetSpecialtyResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class VetSpecialtyResourceIT {

    private static final String ENTITY_API_URL = "/api/vet-specialties";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/vet-specialties/_search";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private VetSpecialtyRepository vetSpecialtyRepository;

    @Autowired
    private VetSpecialtyMapper vetSpecialtyMapper;

    @Autowired
    private VetSpecialtySearchRepository vetSpecialtySearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restVetSpecialtyMockMvc;

    private VetSpecialty vetSpecialty;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static VetSpecialty createEntity(EntityManager em) {
        VetSpecialty vetSpecialty = new VetSpecialty();
        return vetSpecialty;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static VetSpecialty createUpdatedEntity(EntityManager em) {
        VetSpecialty vetSpecialty = new VetSpecialty();
        return vetSpecialty;
    }

    @AfterEach
    public void cleanupElasticSearchRepository() {
        vetSpecialtySearchRepository.deleteAll();
        assertThat(vetSpecialtySearchRepository.count()).isEqualTo(0);
    }

    @BeforeEach
    public void initTest() {
        vetSpecialty = createEntity(em);
    }

    @Test
    @Transactional
    void createVetSpecialty() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(vetSpecialtySearchRepository.findAll());
        // Create the VetSpecialty
        VetSpecialtyDTO vetSpecialtyDTO = vetSpecialtyMapper.toDto(vetSpecialty);
        var returnedVetSpecialtyDTO = om.readValue(
            restVetSpecialtyMockMvc
                .perform(
                    post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(vetSpecialtyDTO))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            VetSpecialtyDTO.class
        );

        // Validate the VetSpecialty in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedVetSpecialty = vetSpecialtyMapper.toEntity(returnedVetSpecialtyDTO);
        assertVetSpecialtyUpdatableFieldsEquals(returnedVetSpecialty, getPersistedVetSpecialty(returnedVetSpecialty));

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(vetSpecialtySearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });
    }

    @Test
    @Transactional
    void createVetSpecialtyWithExistingId() throws Exception {
        // Create the VetSpecialty with an existing ID
        vetSpecialty.setId(1L);
        VetSpecialtyDTO vetSpecialtyDTO = vetSpecialtyMapper.toDto(vetSpecialty);

        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(vetSpecialtySearchRepository.findAll());

        // An entity with an existing ID cannot be created, so this API call must fail
        restVetSpecialtyMockMvc
            .perform(
                post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(vetSpecialtyDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the VetSpecialty in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(vetSpecialtySearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void getAllVetSpecialties() throws Exception {
        // Initialize the database
        vetSpecialtyRepository.saveAndFlush(vetSpecialty);

        // Get all the vetSpecialtyList
        restVetSpecialtyMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(vetSpecialty.getId().intValue())));
    }

    @Test
    @Transactional
    void getVetSpecialty() throws Exception {
        // Initialize the database
        vetSpecialtyRepository.saveAndFlush(vetSpecialty);

        // Get the vetSpecialty
        restVetSpecialtyMockMvc
            .perform(get(ENTITY_API_URL_ID, vetSpecialty.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(vetSpecialty.getId().intValue()));
    }

    @Test
    @Transactional
    void getNonExistingVetSpecialty() throws Exception {
        // Get the vetSpecialty
        restVetSpecialtyMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void deleteVetSpecialty() throws Exception {
        // Initialize the database
        vetSpecialtyRepository.saveAndFlush(vetSpecialty);
        vetSpecialtyRepository.save(vetSpecialty);
        vetSpecialtySearchRepository.save(vetSpecialty);

        long databaseSizeBeforeDelete = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(vetSpecialtySearchRepository.findAll());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the vetSpecialty
        restVetSpecialtyMockMvc
            .perform(delete(ENTITY_API_URL_ID, vetSpecialty.getId()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(vetSpecialtySearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    @Transactional
    void searchVetSpecialty() throws Exception {
        // Initialize the database
        vetSpecialty = vetSpecialtyRepository.saveAndFlush(vetSpecialty);
        vetSpecialtySearchRepository.save(vetSpecialty);

        // Search the vetSpecialty
        restVetSpecialtyMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + vetSpecialty.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(vetSpecialty.getId().intValue())));
    }

    protected long getRepositoryCount() {
        return vetSpecialtyRepository.count();
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

    protected VetSpecialty getPersistedVetSpecialty(VetSpecialty vetSpecialty) {
        return vetSpecialtyRepository.findById(vetSpecialty.getId()).orElseThrow();
    }

    protected void assertPersistedVetSpecialtyToMatchAllProperties(VetSpecialty expectedVetSpecialty) {
        assertVetSpecialtyAllPropertiesEquals(expectedVetSpecialty, getPersistedVetSpecialty(expectedVetSpecialty));
    }

    protected void assertPersistedVetSpecialtyToMatchUpdatableProperties(VetSpecialty expectedVetSpecialty) {
        assertVetSpecialtyAllUpdatablePropertiesEquals(expectedVetSpecialty, getPersistedVetSpecialty(expectedVetSpecialty));
    }
}
