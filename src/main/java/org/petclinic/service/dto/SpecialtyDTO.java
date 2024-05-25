package org.petclinic.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A DTO for the {@link org.petclinic.domain.Specialty} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class SpecialtyDTO implements Serializable {

    private Long id;

    @Size(max = 80)
    private String name;

    private Set<VetDTO> vets = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<VetDTO> getVets() {
        return vets;
    }

    public void setVets(Set<VetDTO> vets) {
        this.vets = vets;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SpecialtyDTO)) {
            return false;
        }

        SpecialtyDTO specialtyDTO = (SpecialtyDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, specialtyDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "SpecialtyDTO{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", vets=" + getVets() +
            "}";
    }
}
