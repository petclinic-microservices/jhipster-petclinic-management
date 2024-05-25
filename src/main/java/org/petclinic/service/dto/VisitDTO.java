package org.petclinic.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A DTO for the {@link org.petclinic.domain.Visit} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class VisitDTO implements Serializable {

    private Long id;

    private LocalDate visitDate;

    @Size(max = 255)
    private String description;

    private PetDTO pet;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getVisitDate() {
        return visitDate;
    }

    public void setVisitDate(LocalDate visitDate) {
        this.visitDate = visitDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public PetDTO getPet() {
        return pet;
    }

    public void setPet(PetDTO pet) {
        this.pet = pet;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof VisitDTO)) {
            return false;
        }

        VisitDTO visitDTO = (VisitDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, visitDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "VisitDTO{" +
            "id=" + getId() +
            ", visitDate='" + getVisitDate() + "'" +
            ", description='" + getDescription() + "'" +
            ", pet=" + getPet() +
            "}";
    }
}
