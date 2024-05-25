import { Injectable } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';

import { IPetType, NewPetType } from '../pet-type.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IPetType for edit and NewPetTypeFormGroupInput for create.
 */
type PetTypeFormGroupInput = IPetType | PartialWithRequiredKeyOf<NewPetType>;

type PetTypeFormDefaults = Pick<NewPetType, 'id'>;

type PetTypeFormGroupContent = {
  id: FormControl<IPetType['id'] | NewPetType['id']>;
  name: FormControl<IPetType['name']>;
};

export type PetTypeFormGroup = FormGroup<PetTypeFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class PetTypeFormService {
  createPetTypeFormGroup(petType: PetTypeFormGroupInput = { id: null }): PetTypeFormGroup {
    const petTypeRawValue = {
      ...this.getFormDefaults(),
      ...petType,
    };
    return new FormGroup<PetTypeFormGroupContent>({
      id: new FormControl(
        { value: petTypeRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      name: new FormControl(petTypeRawValue.name, {
        validators: [Validators.maxLength(80)],
      }),
    });
  }

  getPetType(form: PetTypeFormGroup): IPetType | NewPetType {
    return form.getRawValue() as IPetType | NewPetType;
  }

  resetForm(form: PetTypeFormGroup, petType: PetTypeFormGroupInput): void {
    const petTypeRawValue = { ...this.getFormDefaults(), ...petType };
    form.reset(
      {
        ...petTypeRawValue,
        id: { value: petTypeRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
    );
  }

  private getFormDefaults(): PetTypeFormDefaults {
    return {
      id: null,
    };
  }
}
