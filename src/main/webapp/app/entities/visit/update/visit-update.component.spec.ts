import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { of, Subject, from } from 'rxjs';

import { IPet } from 'app/entities/pet/pet.model';
import { PetService } from 'app/entities/pet/service/pet.service';
import { VisitService } from '../service/visit.service';
import { IVisit } from '../visit.model';
import { VisitFormService } from './visit-form.service';

import { VisitUpdateComponent } from './visit-update.component';

describe('Visit Management Update Component', () => {
  let comp: VisitUpdateComponent;
  let fixture: ComponentFixture<VisitUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let visitFormService: VisitFormService;
  let visitService: VisitService;
  let petService: PetService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, VisitUpdateComponent],
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
      .overrideTemplate(VisitUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(VisitUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    visitFormService = TestBed.inject(VisitFormService);
    visitService = TestBed.inject(VisitService);
    petService = TestBed.inject(PetService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('Should call Pet query and add missing value', () => {
      const visit: IVisit = { id: 456 };
      const pet: IPet = { id: 2890 };
      visit.pet = pet;

      const petCollection: IPet[] = [{ id: 26730 }];
      jest.spyOn(petService, 'query').mockReturnValue(of(new HttpResponse({ body: petCollection })));
      const additionalPets = [pet];
      const expectedCollection: IPet[] = [...additionalPets, ...petCollection];
      jest.spyOn(petService, 'addPetToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ visit });
      comp.ngOnInit();

      expect(petService.query).toHaveBeenCalled();
      expect(petService.addPetToCollectionIfMissing).toHaveBeenCalledWith(petCollection, ...additionalPets.map(expect.objectContaining));
      expect(comp.petsSharedCollection).toEqual(expectedCollection);
    });

    it('Should update editForm', () => {
      const visit: IVisit = { id: 456 };
      const pet: IPet = { id: 23303 };
      visit.pet = pet;

      activatedRoute.data = of({ visit });
      comp.ngOnInit();

      expect(comp.petsSharedCollection).toContain(pet);
      expect(comp.visit).toEqual(visit);
    });
  });

  describe('save', () => {
    it('Should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IVisit>>();
      const visit = { id: 123 };
      jest.spyOn(visitFormService, 'getVisit').mockReturnValue(visit);
      jest.spyOn(visitService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ visit });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: visit }));
      saveSubject.complete();

      // THEN
      expect(visitFormService.getVisit).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(visitService.update).toHaveBeenCalledWith(expect.objectContaining(visit));
      expect(comp.isSaving).toEqual(false);
    });

    it('Should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IVisit>>();
      const visit = { id: 123 };
      jest.spyOn(visitFormService, 'getVisit').mockReturnValue({ id: null });
      jest.spyOn(visitService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ visit: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: visit }));
      saveSubject.complete();

      // THEN
      expect(visitFormService.getVisit).toHaveBeenCalled();
      expect(visitService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('Should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IVisit>>();
      const visit = { id: 123 };
      jest.spyOn(visitService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ visit });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(visitService.update).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });

  describe('Compare relationships', () => {
    describe('comparePet', () => {
      it('Should forward to petService', () => {
        const entity = { id: 123 };
        const entity2 = { id: 456 };
        jest.spyOn(petService, 'comparePet');
        comp.comparePet(entity, entity2);
        expect(petService.comparePet).toHaveBeenCalledWith(entity, entity2);
      });
    });
  });
});
