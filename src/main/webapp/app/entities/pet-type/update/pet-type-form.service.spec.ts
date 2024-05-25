import { TestBed } from '@angular/core/testing';

import { sampleWithRequiredData, sampleWithNewData } from '../pet-type.test-samples';

import { PetTypeFormService } from './pet-type-form.service';

describe('PetType Form Service', () => {
  let service: PetTypeFormService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(PetTypeFormService);
  });

  describe('Service methods', () => {
    describe('createPetTypeFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createPetTypeFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            name: expect.any(Object),
          }),
        );
      });

      it('passing IPetType should create a new form with FormGroup', () => {
        const formGroup = service.createPetTypeFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            name: expect.any(Object),
          }),
        );
      });
    });

    describe('getPetType', () => {
      it('should return NewPetType for default PetType initial value', () => {
        const formGroup = service.createPetTypeFormGroup(sampleWithNewData);

        const petType = service.getPetType(formGroup) as any;

        expect(petType).toMatchObject(sampleWithNewData);
      });

      it('should return NewPetType for empty PetType initial value', () => {
        const formGroup = service.createPetTypeFormGroup();

        const petType = service.getPetType(formGroup) as any;

        expect(petType).toMatchObject({});
      });

      it('should return IPetType', () => {
        const formGroup = service.createPetTypeFormGroup(sampleWithRequiredData);

        const petType = service.getPetType(formGroup) as any;

        expect(petType).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing IPetType should not enable id FormControl', () => {
        const formGroup = service.createPetTypeFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewPetType should disable id FormControl', () => {
        const formGroup = service.createPetTypeFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
