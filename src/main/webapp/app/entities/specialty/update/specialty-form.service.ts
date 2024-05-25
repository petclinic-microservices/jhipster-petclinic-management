import { Injectable } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';

import { ISpecialty, NewSpecialty } from '../specialty.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts ISpecialty for edit and NewSpecialtyFormGroupInput for create.
 */
type SpecialtyFormGroupInput = ISpecialty | PartialWithRequiredKeyOf<NewSpecialty>;

type SpecialtyFormDefaults = Pick<NewSpecialty, 'id' | 'vets'>;

type SpecialtyFormGroupContent = {
  id: FormControl<ISpecialty['id'] | NewSpecialty['id']>;
  name: FormControl<ISpecialty['name']>;
  vets: FormControl<ISpecialty['vets']>;
};

export type SpecialtyFormGroup = FormGroup<SpecialtyFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class SpecialtyFormService {
  createSpecialtyFormGroup(specialty: SpecialtyFormGroupInput = { id: null }): SpecialtyFormGroup {
    const specialtyRawValue = {
      ...this.getFormDefaults(),
      ...specialty,
    };
    return new FormGroup<SpecialtyFormGroupContent>({
      id: new FormControl(
        { value: specialtyRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      name: new FormControl(specialtyRawValue.name, {
        validators: [Validators.maxLength(80)],
      }),
      vets: new FormControl(specialtyRawValue.vets ?? []),
    });
  }

  getSpecialty(form: SpecialtyFormGroup): ISpecialty | NewSpecialty {
    return form.getRawValue() as ISpecialty | NewSpecialty;
  }

  resetForm(form: SpecialtyFormGroup, specialty: SpecialtyFormGroupInput): void {
    const specialtyRawValue = { ...this.getFormDefaults(), ...specialty };
    form.reset(
      {
        ...specialtyRawValue,
        id: { value: specialtyRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
    );
  }

  private getFormDefaults(): SpecialtyFormDefaults {
    return {
      id: null,
      vets: [],
    };
  }
}
