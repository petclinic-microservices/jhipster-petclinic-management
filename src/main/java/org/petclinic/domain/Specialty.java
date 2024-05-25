package org.petclinic.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * A Specialty.
 */
@Entity
@Table(name = "specialty")
@org.springframework.data.elasticsearch.annotations.Document(indexName = "specialty")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Specialty implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Size(max = 80)
    @Column(name = "name", length = 80)
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Text)
    private String name;

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "specialties")
    @org.springframework.data.annotation.Transient
    @JsonIgnoreProperties(value = { "specialties" }, allowSetters = true)
    private Set<Vet> vets = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Specialty id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public Specialty name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Vet> getVets() {
        return this.vets;
    }

    public void setVets(Set<Vet> vets) {
        if (this.vets != null) {
            this.vets.forEach(i -> i.removeSpecialties(this));
        }
        if (vets != null) {
            vets.forEach(i -> i.addSpecialties(this));
        }
        this.vets = vets;
    }

    public Specialty vets(Set<Vet> vets) {
        this.setVets(vets);
        return this;
    }

    public Specialty addVets(Vet vet) {
        this.vets.add(vet);
        vet.getSpecialties().add(this);
        return this;
    }

    public Specialty removeVets(Vet vet) {
        this.vets.remove(vet);
        vet.getSpecialties().remove(this);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Specialty)) {
            return false;
        }
        return getId() != null && getId().equals(((Specialty) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Specialty{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            "}";
    }
}
