package org.petclinic.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A VetSpecialty.
 */
@Entity
@Table(name = "vet_specialty")
@org.springframework.data.elasticsearch.annotations.Document(indexName = "vetspecialty")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class VetSpecialty implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public VetSpecialty id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof VetSpecialty)) {
            return false;
        }
        return getId() != null && getId().equals(((VetSpecialty) o).getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "VetSpecialty{" +
            "id=" + getId() +
            "}";
    }
}
