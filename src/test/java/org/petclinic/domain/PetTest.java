package org.petclinic.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.petclinic.domain.OwnerTestSamples.*;
import static org.petclinic.domain.PetTestSamples.*;
import static org.petclinic.domain.PetTypeTestSamples.*;

import org.junit.jupiter.api.Test;
import org.petclinic.web.rest.TestUtil;

class PetTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Pet.class);
        Pet pet1 = getPetSample1();
        Pet pet2 = new Pet();
        assertThat(pet1).isNotEqualTo(pet2);

        pet2.setId(pet1.getId());
        assertThat(pet1).isEqualTo(pet2);

        pet2 = getPetSample2();
        assertThat(pet1).isNotEqualTo(pet2);
    }

    @Test
    void typeTest() throws Exception {
        Pet pet = getPetRandomSampleGenerator();
        PetType petTypeBack = getPetTypeRandomSampleGenerator();

        pet.setType(petTypeBack);
        assertThat(pet.getType()).isEqualTo(petTypeBack);

        pet.type(null);
        assertThat(pet.getType()).isNull();
    }

    @Test
    void ownerTest() throws Exception {
        Pet pet = getPetRandomSampleGenerator();
        Owner ownerBack = getOwnerRandomSampleGenerator();

        pet.setOwner(ownerBack);
        assertThat(pet.getOwner()).isEqualTo(ownerBack);

        pet.owner(null);
        assertThat(pet.getOwner()).isNull();
    }
}
