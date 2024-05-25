import { TestBed } from '@angular/core/testing';

import { sampleWithRequiredData, sampleWithNewData } from '../vet-specialty.test-samples';

import { VetSpecialtyFormService } from './vet-specialty-form.service';

describe('VetSpecialty Form Service', () => {
  let service: VetSpecialtyFormService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(VetSpecialtyFormService);
  });

  describe('Service methods', () => {
    describe('createVetSpecialtyFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createVetSpecialtyFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
          }),
        );
      });

      it('passing IVetSpecialty should create a new form with FormGroup', () => {
        const formGroup = service.createVetSpecialtyFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
          }),
        );
      });
    });

    describe('getVetSpecialty', () => {
      it('should return NewVetSpecialty for default VetSpecialty initial value', () => {
        const formGroup = service.createVetSpecialtyFormGroup(sampleWithNewData);

        const vetSpecialty = service.getVetSpecialty(formGroup) as any;

        expect(vetSpecialty).toMatchObject(sampleWithNewData);
      });

      it('should return NewVetSpecialty for empty VetSpecialty initial value', () => {
        const formGroup = service.createVetSpecialtyFormGroup();

        const vetSpecialty = service.getVetSpecialty(formGroup) as any;

        expect(vetSpecialty).toMatchObject({});
      });

      it('should return IVetSpecialty', () => {
        const formGroup = service.createVetSpecialtyFormGroup(sampleWithRequiredData);

        const vetSpecialty = service.getVetSpecialty(formGroup) as any;

        expect(vetSpecialty).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing IVetSpecialty should not enable id FormControl', () => {
        const formGroup = service.createVetSpecialtyFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewVetSpecialty should disable id FormControl', () => {
        const formGroup = service.createVetSpecialtyFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
