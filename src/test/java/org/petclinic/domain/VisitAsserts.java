package org.petclinic.domain;

import static org.assertj.core.api.Assertions.assertThat;

public class VisitAsserts {

    /**
     * Asserts that the entity has all properties (fields/relationships) set.
     *
     * @param expected the expected entity
     * @param actual the actual entity
     */
    public static void assertVisitAllPropertiesEquals(Visit expected, Visit actual) {
        assertVisitAutoGeneratedPropertiesEquals(expected, actual);
        assertVisitAllUpdatablePropertiesEquals(expected, actual);
    }

    /**
     * Asserts that the entity has all updatable properties (fields/relationships) set.
     *
     * @param expected the expected entity
     * @param actual the actual entity
     */
    public static void assertVisitAllUpdatablePropertiesEquals(Visit expected, Visit actual) {
        assertVisitUpdatableFieldsEquals(expected, actual);
        assertVisitUpdatableRelationshipsEquals(expected, actual);
    }

    /**
     * Asserts that the entity has all the auto generated properties (fields/relationships) set.
     *
     * @param expected the expected entity
     * @param actual the actual entity
     */
    public static void assertVisitAutoGeneratedPropertiesEquals(Visit expected, Visit actual) {
        assertThat(expected)
            .as("Verify Visit auto generated properties")
            .satisfies(e -> assertThat(e.getId()).as("check id").isEqualTo(actual.getId()));
    }

    /**
     * Asserts that the entity has all the updatable fields set.
     *
     * @param expected the expected entity
     * @param actual the actual entity
     */
    public static void assertVisitUpdatableFieldsEquals(Visit expected, Visit actual) {
        assertThat(expected)
            .as("Verify Visit relevant properties")
            .satisfies(e -> assertThat(e.getVisitDate()).as("check visitDate").isEqualTo(actual.getVisitDate()))
            .satisfies(e -> assertThat(e.getDescription()).as("check description").isEqualTo(actual.getDescription()));
    }

    /**
     * Asserts that the entity has all the updatable relationships set.
     *
     * @param expected the expected entity
     * @param actual the actual entity
     */
    public static void assertVisitUpdatableRelationshipsEquals(Visit expected, Visit actual) {
        assertThat(expected)
            .as("Verify Visit relationships")
            .satisfies(e -> assertThat(e.getPet()).as("check pet").isEqualTo(actual.getPet()));
    }
}
