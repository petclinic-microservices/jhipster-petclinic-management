package org.petclinic.service.mapper;

import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.*;
import org.petclinic.domain.Specialty;
import org.petclinic.domain.Vet;
import org.petclinic.service.dto.SpecialtyDTO;
import org.petclinic.service.dto.VetDTO;

/**
 * Mapper for the entity {@link Specialty} and its DTO {@link SpecialtyDTO}.
 */
@Mapper(componentModel = "spring")
public interface SpecialtyMapper extends EntityMapper<SpecialtyDTO, Specialty> {
    @Mapping(target = "vets", source = "vets", qualifiedByName = "vetFirstNameSet")
    SpecialtyDTO toDto(Specialty s);

    @Mapping(target = "vets", ignore = true)
    @Mapping(target = "removeVets", ignore = true)
    Specialty toEntity(SpecialtyDTO specialtyDTO);

    @Named("vetFirstName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "firstName", source = "firstName")
    VetDTO toDtoVetFirstName(Vet vet);

    @Named("vetFirstNameSet")
    default Set<VetDTO> toDtoVetFirstNameSet(Set<Vet> vet) {
        return vet.stream().map(this::toDtoVetFirstName).collect(Collectors.toSet());
    }
}
