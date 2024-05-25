import { Injectable } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';

import { IVetSpecialty, NewVetSpecialty } from '../vet-specialty.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IVetSpecialty for edit and NewVetSpecialtyFormGroupInput for create.
 */
type VetSpecialtyFormGroupInput = IVetSpecialty | PartialWithRequiredKeyOf<NewVetSpecialty>;

type VetSpecialtyFormDefaults = Pick<NewVetSpecialty, 'id'>;

type VetSpecialtyFormGroupContent = {
  id: FormControl<IVetSpecialty['id'] | NewVetSpecialty['id']>;
};

export type VetSpecialtyFormGroup = FormGroup<VetSpecialtyFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class VetSpecialtyFormService {
  createVetSpecialtyFormGroup(vetSpecialty: VetSpecialtyFormGroupInput = { id: null }): VetSpecialtyFormGroup {
    const vetSpecialtyRawValue = {
      ...this.getFormDefaults(),
      ...vetSpecialty,
    };
    return new FormGroup<VetSpecialtyFormGroupContent>({
      id: new FormControl(
        { value: vetSpecialtyRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
    });
  }

  getVetSpecialty(form: VetSpecialtyFormGroup): NewVetSpecialty {
    return form.getRawValue() as NewVetSpecialty;
  }

  resetForm(form: VetSpecialtyFormGroup, vetSpecialty: VetSpecialtyFormGroupInput): void {
    const vetSpecialtyRawValue = { ...this.getFormDefaults(), ...vetSpecialty };
    form.reset(
      {
        ...vetSpecialtyRawValue,
        id: { value: vetSpecialtyRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
    );
  }

  private getFormDefaults(): VetSpecialtyFormDefaults {
    return {
      id: null,
    };
  }
}
