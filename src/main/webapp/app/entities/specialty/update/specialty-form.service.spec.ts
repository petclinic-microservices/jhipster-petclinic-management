import { TestBed } from '@angular/core/testing';

import { sampleWithRequiredData, sampleWithNewData } from '../specialty.test-samples';

import { SpecialtyFormService } from './specialty-form.service';

describe('Specialty Form Service', () => {
  let service: SpecialtyFormService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(SpecialtyFormService);
  });

  describe('Service methods', () => {
    describe('createSpecialtyFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createSpecialtyFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            name: expect.any(Object),
            vets: expect.any(Object),
          }),
        );
      });

      it('passing ISpecialty should create a new form with FormGroup', () => {
        const formGroup = service.createSpecialtyFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            name: expect.any(Object),
            vets: expect.any(Object),
          }),
        );
      });
    });

    describe('getSpecialty', () => {
      it('should return NewSpecialty for default Specialty initial value', () => {
        const formGroup = service.createSpecialtyFormGroup(sampleWithNewData);

        const specialty = service.getSpecialty(formGroup) as any;

        expect(specialty).toMatchObject(sampleWithNewData);
      });

      it('should return NewSpecialty for empty Specialty initial value', () => {
        const formGroup = service.createSpecialtyFormGroup();

        const specialty = service.getSpecialty(formGroup) as any;

        expect(specialty).toMatchObject({});
      });

      it('should return ISpecialty', () => {
        const formGroup = service.createSpecialtyFormGroup(sampleWithRequiredData);

        const specialty = service.getSpecialty(formGroup) as any;

        expect(specialty).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing ISpecialty should not enable id FormControl', () => {
        const formGroup = service.createSpecialtyFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewSpecialty should disable id FormControl', () => {
        const formGroup = service.createSpecialtyFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
