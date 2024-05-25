package org.petclinic.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.petclinic.domain.OwnerTestSamples.*;

import org.junit.jupiter.api.Test;
import org.petclinic.web.rest.TestUtil;

class OwnerTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Owner.class);
        Owner owner1 = getOwnerSample1();
        Owner owner2 = new Owner();
        assertThat(owner1).isNotEqualTo(owner2);

        owner2.setId(owner1.getId());
        assertThat(owner1).isEqualTo(owner2);

        owner2 = getOwnerSample2();
        assertThat(owner1).isNotEqualTo(owner2);
    }
}
