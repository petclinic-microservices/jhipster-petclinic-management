package org.petclinic.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.petclinic.web.rest.TestUtil;

class VetSpecialtyDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(VetSpecialtyDTO.class);
        VetSpecialtyDTO vetSpecialtyDTO1 = new VetSpecialtyDTO();
        vetSpecialtyDTO1.setId(1L);
        VetSpecialtyDTO vetSpecialtyDTO2 = new VetSpecialtyDTO();
        assertThat(vetSpecialtyDTO1).isNotEqualTo(vetSpecialtyDTO2);
        vetSpecialtyDTO2.setId(vetSpecialtyDTO1.getId());
        assertThat(vetSpecialtyDTO1).isEqualTo(vetSpecialtyDTO2);
        vetSpecialtyDTO2.setId(2L);
        assertThat(vetSpecialtyDTO1).isNotEqualTo(vetSpecialtyDTO2);
        vetSpecialtyDTO1.setId(null);
        assertThat(vetSpecialtyDTO1).isNotEqualTo(vetSpecialtyDTO2);
    }
}
