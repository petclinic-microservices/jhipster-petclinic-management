package org.petclinic.service.mapper;

import static org.petclinic.domain.VetSpecialtyAsserts.*;
import static org.petclinic.domain.VetSpecialtyTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class VetSpecialtyMapperTest {

    private VetSpecialtyMapper vetSpecialtyMapper;

    @BeforeEach
    void setUp() {
        vetSpecialtyMapper = new VetSpecialtyMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getVetSpecialtySample1();
        var actual = vetSpecialtyMapper.toEntity(vetSpecialtyMapper.toDto(expected));
        assertVetSpecialtyAllPropertiesEquals(expected, actual);
    }
}
