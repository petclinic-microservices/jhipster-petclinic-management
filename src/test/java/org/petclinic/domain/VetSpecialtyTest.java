package org.petclinic.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.petclinic.domain.VetSpecialtyTestSamples.*;

import org.junit.jupiter.api.Test;
import org.petclinic.web.rest.TestUtil;

class VetSpecialtyTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(VetSpecialty.class);
        VetSpecialty vetSpecialty1 = getVetSpecialtySample1();
        VetSpecialty vetSpecialty2 = new VetSpecialty();
        assertThat(vetSpecialty1).isNotEqualTo(vetSpecialty2);

        vetSpecialty2.setId(vetSpecialty1.getId());
        assertThat(vetSpecialty1).isEqualTo(vetSpecialty2);

        vetSpecialty2 = getVetSpecialtySample2();
        assertThat(vetSpecialty1).isNotEqualTo(vetSpecialty2);
    }

    @Test
    void hashCodeVerifier() throws Exception {
        VetSpecialty vetSpecialty = new VetSpecialty();
        assertThat(vetSpecialty.hashCode()).isZero();

        VetSpecialty vetSpecialty1 = getVetSpecialtySample1();
        vetSpecialty.setId(vetSpecialty1.getId());
        assertThat(vetSpecialty).hasSameHashCodeAs(vetSpecialty1);
    }
}
