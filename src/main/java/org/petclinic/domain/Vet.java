package org.petclinic.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * A Vet.
 */
@Entity
@Table(name = "vet")
@org.springframework.data.elasticsearch.annotations.Document(indexName = "vet")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Vet implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Size(max = 30)
    @Column(name = "first_name", length = 30)
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Text)
    private String firstName;

    @Size(max = 30)
    @Column(name = "last_name", length = 30)
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Text)
    private String lastName;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "rel_vet__specialties",
        joinColumns = @JoinColumn(name = "vet_id"),
        inverseJoinColumns = @JoinColumn(name = "specialties_id")
    )
    @JsonIgnoreProperties(value = { "vets" }, allowSetters = true)
    private Set<Specialty> specialties = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Vet id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public Vet firstName(String firstName) {
        this.setFirstName(firstName);
        return this;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public Vet lastName(String lastName) {
        this.setLastName(lastName);
        return this;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Set<Specialty> getSpecialties() {
        return this.specialties;
    }

    public void setSpecialties(Set<Specialty> specialties) {
        this.specialties = specialties;
    }

    public Vet specialties(Set<Specialty> specialties) {
        this.setSpecialties(specialties);
        return this;
    }

    public Vet addSpecialties(Specialty specialty) {
        this.specialties.add(specialty);
        return this;
    }

    public Vet removeSpecialties(Specialty specialty) {
        this.specialties.remove(specialty);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Vet)) {
            return false;
        }
        return getId() != null && getId().equals(((Vet) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Vet{" +
            "id=" + getId() +
            ", firstName='" + getFirstName() + "'" +
            ", lastName='" + getLastName() + "'" +
            "}";
    }
}
