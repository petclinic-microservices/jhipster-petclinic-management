package org.petclinic.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.petclinic.domain.PetTestSamples.*;
import static org.petclinic.domain.VisitTestSamples.*;

import org.junit.jupiter.api.Test;
import org.petclinic.web.rest.TestUtil;

class VisitTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Visit.class);
        Visit visit1 = getVisitSample1();
        Visit visit2 = new Visit();
        assertThat(visit1).isNotEqualTo(visit2);

        visit2.setId(visit1.getId());
        assertThat(visit1).isEqualTo(visit2);

        visit2 = getVisitSample2();
        assertThat(visit1).isNotEqualTo(visit2);
    }

    @Test
    void petTest() throws Exception {
        Visit visit = getVisitRandomSampleGenerator();
        Pet petBack = getPetRandomSampleGenerator();

        visit.setPet(petBack);
        assertThat(visit.getPet()).isEqualTo(petBack);

        visit.pet(null);
        assertThat(visit.getPet()).isNull();
    }
}
