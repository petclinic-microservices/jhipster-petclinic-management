package org.petclinic.service.dto;

import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link org.petclinic.domain.VetSpecialty} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class VetSpecialtyDTO implements Serializable {

    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof VetSpecialtyDTO)) {
            return false;
        }

        VetSpecialtyDTO vetSpecialtyDTO = (VetSpecialtyDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, vetSpecialtyDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "VetSpecialtyDTO{" +
            "id=" + getId() +
            "}";
    }
}
