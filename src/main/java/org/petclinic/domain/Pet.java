package org.petclinic.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * A Pet.
 */
@Entity
@Table(name = "pet")
@org.springframework.data.elasticsearch.annotations.Document(indexName = "pet")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Pet implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Size(max = 30)
    @Column(name = "name", length = 30)
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Text)
    private String name;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @ManyToOne(fetch = FetchType.LAZY)
    private PetType type;

    @ManyToOne(fetch = FetchType.LAZY)
    private Owner owner;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Pet id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public Pet name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getBirthDate() {
        return this.birthDate;
    }

    public Pet birthDate(LocalDate birthDate) {
        this.setBirthDate(birthDate);
        return this;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public PetType getType() {
        return this.type;
    }

    public void setType(PetType petType) {
        this.type = petType;
    }

    public Pet type(PetType petType) {
        this.setType(petType);
        return this;
    }

    public Owner getOwner() {
        return this.owner;
    }

    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    public Pet owner(Owner owner) {
        this.setOwner(owner);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Pet)) {
            return false;
        }
        return getId() != null && getId().equals(((Pet) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Pet{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", birthDate='" + getBirthDate() + "'" +
            "}";
    }
}
