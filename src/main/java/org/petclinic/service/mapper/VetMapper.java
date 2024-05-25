package org.petclinic.service.mapper;

import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.*;
import org.petclinic.domain.Specialty;
import org.petclinic.domain.Vet;
import org.petclinic.service.dto.SpecialtyDTO;
import org.petclinic.service.dto.VetDTO;

/**
 * Mapper for the entity {@link Vet} and its DTO {@link VetDTO}.
 */
@Mapper(componentModel = "spring")
public interface VetMapper extends EntityMapper<VetDTO, Vet> {
    @Mapping(target = "specialties", source = "specialties", qualifiedByName = "specialtyNameSet")
    VetDTO toDto(Vet s);

    @Mapping(target = "removeSpecialties", ignore = true)
    Vet toEntity(VetDTO vetDTO);

    @Named("specialtyName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    SpecialtyDTO toDtoSpecialtyName(Specialty specialty);

    @Named("specialtyNameSet")
    default Set<SpecialtyDTO> toDtoSpecialtyNameSet(Set<Specialty> specialty) {
        return specialty.stream().map(this::toDtoSpecialtyName).collect(Collectors.toSet());
    }
}
