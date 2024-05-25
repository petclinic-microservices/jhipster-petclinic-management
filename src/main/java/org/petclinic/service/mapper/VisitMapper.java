package org.petclinic.service.mapper;

import org.mapstruct.*;
import org.petclinic.domain.Pet;
import org.petclinic.domain.Visit;
import org.petclinic.service.dto.PetDTO;
import org.petclinic.service.dto.VisitDTO;

/**
 * Mapper for the entity {@link Visit} and its DTO {@link VisitDTO}.
 */
@Mapper(componentModel = "spring")
public interface VisitMapper extends EntityMapper<VisitDTO, Visit> {
    @Mapping(target = "pet", source = "pet", qualifiedByName = "petName")
    VisitDTO toDto(Visit s);

    @Named("petName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    PetDTO toDtoPetName(Pet pet);
}
