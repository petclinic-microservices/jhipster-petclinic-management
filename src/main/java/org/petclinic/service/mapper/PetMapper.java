package org.petclinic.service.mapper;

import org.mapstruct.*;
import org.petclinic.domain.Owner;
import org.petclinic.domain.Pet;
import org.petclinic.domain.PetType;
import org.petclinic.service.dto.OwnerDTO;
import org.petclinic.service.dto.PetDTO;
import org.petclinic.service.dto.PetTypeDTO;

/**
 * Mapper for the entity {@link Pet} and its DTO {@link PetDTO}.
 */
@Mapper(componentModel = "spring")
public interface PetMapper extends EntityMapper<PetDTO, Pet> {
    @Mapping(target = "type", source = "type", qualifiedByName = "petTypeName")
    @Mapping(target = "owner", source = "owner", qualifiedByName = "ownerLastName")
    PetDTO toDto(Pet s);

    @Named("petTypeName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    PetTypeDTO toDtoPetTypeName(PetType petType);

    @Named("ownerLastName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "lastName", source = "lastName")
    OwnerDTO toDtoOwnerLastName(Owner owner);
}
