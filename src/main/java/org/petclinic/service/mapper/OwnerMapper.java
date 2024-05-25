package org.petclinic.service.mapper;

import org.mapstruct.*;
import org.petclinic.domain.Owner;
import org.petclinic.service.dto.OwnerDTO;

/**
 * Mapper for the entity {@link Owner} and its DTO {@link OwnerDTO}.
 */
@Mapper(componentModel = "spring")
public interface OwnerMapper extends EntityMapper<OwnerDTO, Owner> {}
