import { TestBed } from '@angular/core/testing';

import { sampleWithRequiredData, sampleWithNewData } from '../vet.test-samples';

import { VetFormService } from './vet-form.service';

describe('Vet Form Service', () => {
  let service: VetFormService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(VetFormService);
  });

  describe('Service methods', () => {
    describe('createVetFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createVetFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            firstName: expect.any(Object),
            lastName: expect.any(Object),
            specialties: expect.any(Object),
          }),
        );
      });

      it('passing IVet should create a new form with FormGroup', () => {
        const formGroup = service.createVetFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            firstName: expect.any(Object),
            lastName: expect.any(Object),
            specialties: expect.any(Object),
          }),
        );
      });
    });

    describe('getVet', () => {
      it('should return NewVet for default Vet initial value', () => {
        const formGroup = service.createVetFormGroup(sampleWithNewData);

        const vet = service.getVet(formGroup) as any;

        expect(vet).toMatchObject(sampleWithNewData);
      });

      it('should return NewVet for empty Vet initial value', () => {
        const formGroup = service.createVetFormGroup();

        const vet = service.getVet(formGroup) as any;

        expect(vet).toMatchObject({});
      });

      it('should return IVet', () => {
        const formGroup = service.createVetFormGroup(sampleWithRequiredData);

        const vet = service.getVet(formGroup) as any;

        expect(vet).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing IVet should not enable id FormControl', () => {
        const formGroup = service.createVetFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewVet should disable id FormControl', () => {
        const formGroup = service.createVetFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
