import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { of, Subject, from } from 'rxjs';

import { ISpecialty } from 'app/entities/specialty/specialty.model';
import { SpecialtyService } from 'app/entities/specialty/service/specialty.service';
import { VetService } from '../service/vet.service';
import { IVet } from '../vet.model';
import { VetFormService } from './vet-form.service';

import { VetUpdateComponent } from './vet-update.component';

describe('Vet Management Update Component', () => {
  let comp: VetUpdateComponent;
  let fixture: ComponentFixture<VetUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let vetFormService: VetFormService;
  let vetService: VetService;
  let specialtyService: SpecialtyService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, VetUpdateComponent],
      providers: [
        FormBuilder,
        {
          provide: ActivatedRoute,
          useValue: {
            params: from([{}]),
          },
        },
      ],
    })
      .overrideTemplate(VetUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(VetUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    vetFormService = TestBed.inject(VetFormService);
    vetService = TestBed.inject(VetService);
    specialtyService = TestBed.inject(SpecialtyService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('Should call Specialty query and add missing value', () => {
      const vet: IVet = { id: 456 };
      const specialties: ISpecialty[] = [{ id: 31518 }];
      vet.specialties = specialties;

      const specialtyCollection: ISpecialty[] = [{ id: 6853 }];
      jest.spyOn(specialtyService, 'query').mockReturnValue(of(new HttpResponse({ body: specialtyCollection })));
      const additionalSpecialties = [...specialties];
      const expectedCollection: ISpecialty[] = [...additionalSpecialties, ...specialtyCollection];
      jest.spyOn(specialtyService, 'addSpecialtyToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ vet });
      comp.ngOnInit();

      expect(specialtyService.query).toHaveBeenCalled();
      expect(specialtyService.addSpecialtyToCollectionIfMissing).toHaveBeenCalledWith(
        specialtyCollection,
        ...additionalSpecialties.map(expect.objectContaining),
      );
      expect(comp.specialtiesSharedCollection).toEqual(expectedCollection);
    });

    it('Should update editForm', () => {
      const vet: IVet = { id: 456 };
      const specialties: ISpecialty = { id: 30754 };
      vet.specialties = [specialties];

      activatedRoute.data = of({ vet });
      comp.ngOnInit();

      expect(comp.specialtiesSharedCollection).toContain(specialties);
      expect(comp.vet).toEqual(vet);
    });
  });

  describe('save', () => {
    it('Should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IVet>>();
      const vet = { id: 123 };
      jest.spyOn(vetFormService, 'getVet').mockReturnValue(vet);
      jest.spyOn(vetService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ vet });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: vet }));
      saveSubject.complete();

      // THEN
      expect(vetFormService.getVet).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(vetService.update).toHaveBeenCalledWith(expect.objectContaining(vet));
      expect(comp.isSaving).toEqual(false);
    });

    it('Should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IVet>>();
      const vet = { id: 123 };
      jest.spyOn(vetFormService, 'getVet').mockReturnValue({ id: null });
      jest.spyOn(vetService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ vet: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: vet }));
      saveSubject.complete();

      // THEN
      expect(vetFormService.getVet).toHaveBeenCalled();
      expect(vetService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('Should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IVet>>();
      const vet = { id: 123 };
      jest.spyOn(vetService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ vet });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(vetService.update).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });

  describe('Compare relationships', () => {
    describe('compareSpecialty', () => {
      it('Should forward to specialtyService', () => {
        const entity = { id: 123 };
        const entity2 = { id: 456 };
        jest.spyOn(specialtyService, 'compareSpecialty');
        comp.compareSpecialty(entity, entity2);
        expect(specialtyService.compareSpecialty).toHaveBeenCalledWith(entity, entity2);
      });
    });
  });
});
