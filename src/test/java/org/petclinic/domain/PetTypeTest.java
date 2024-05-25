package org.petclinic.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.petclinic.domain.PetTypeTestSamples.*;

import org.junit.jupiter.api.Test;
import org.petclinic.web.rest.TestUtil;

class PetTypeTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(PetType.class);
        PetType petType1 = getPetTypeSample1();
        PetType petType2 = new PetType();
        assertThat(petType1).isNotEqualTo(petType2);

        petType2.setId(petType1.getId());
        assertThat(petType1).isEqualTo(petType2);

        petType2 = getPetTypeSample2();
        assertThat(petType1).isNotEqualTo(petType2);
    }
}
