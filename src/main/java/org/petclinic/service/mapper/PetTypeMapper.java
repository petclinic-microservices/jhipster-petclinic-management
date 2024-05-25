package org.petclinic.service.mapper;

import org.mapstruct.*;
import org.petclinic.domain.PetType;
import org.petclinic.service.dto.PetTypeDTO;

/**
 * Mapper for the entity {@link PetType} and its DTO {@link PetTypeDTO}.
 */
@Mapper(componentModel = "spring")
public interface PetTypeMapper extends EntityMapper<PetTypeDTO, PetType> {}
