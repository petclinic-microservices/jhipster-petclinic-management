package org.petclinic.service.mapper;

import org.mapstruct.*;
import org.petclinic.domain.VetSpecialty;
import org.petclinic.service.dto.VetSpecialtyDTO;

/**
 * Mapper for the entity {@link VetSpecialty} and its DTO {@link VetSpecialtyDTO}.
 */
@Mapper(componentModel = "spring")
public interface VetSpecialtyMapper extends EntityMapper<VetSpecialtyDTO, VetSpecialty> {}
