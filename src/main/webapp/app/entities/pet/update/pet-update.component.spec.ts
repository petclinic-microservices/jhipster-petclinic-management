import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { of, Subject, from } from 'rxjs';

import { IPetType } from 'app/entities/pet-type/pet-type.model';
import { PetTypeService } from 'app/entities/pet-type/service/pet-type.service';
import { IOwner } from 'app/entities/owner/owner.model';
import { OwnerService } from 'app/entities/owner/service/owner.service';
import { IPet } from '../pet.model';
import { PetService } from '../service/pet.service';
import { PetFormService } from './pet-form.service';

import { PetUpdateComponent } from './pet-update.component';

describe('Pet Management Update Component', () => {
  let comp: PetUpdateComponent;
  let fixture: ComponentFixture<PetUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let petFormService: PetFormService;
  let petService: PetService;
  let petTypeService: PetTypeService;
  let ownerService: OwnerService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, PetUpdateComponent],
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
      .overrideTemplate(PetUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(PetUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    petFormService = TestBed.inject(PetFormService);
    petService = TestBed.inject(PetService);
    petTypeService = TestBed.inject(PetTypeService);
    ownerService = TestBed.inject(OwnerService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('Should call PetType query and add missing value', () => {
      const pet: IPet = { id: 456 };
      const type: IPetType = { id: 7169 };
      pet.type = type;

      const petTypeCollection: IPetType[] = [{ id: 32205 }];
      jest.spyOn(petTypeService, 'query').mockReturnValue(of(new HttpResponse({ body: petTypeCollection })));
      const additionalPetTypes = [type];
      const expectedCollection: IPetType[] = [...additionalPetTypes, ...petTypeCollection];
      jest.spyOn(petTypeService, 'addPetTypeToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ pet });
      comp.ngOnInit();

      expect(petTypeService.query).toHaveBeenCalled();
      expect(petTypeService.addPetTypeToCollectionIfMissing).toHaveBeenCalledWith(
        petTypeCollection,
        ...additionalPetTypes.map(expect.objectContaining),
      );
      expect(comp.petTypesSharedCollection).toEqual(expectedCollection);
    });

    it('Should call Owner query and add missing value', () => {
      const pet: IPet = { id: 456 };
      const owner: IOwner = { id: 9795 };
      pet.owner = owner;

      const ownerCollection: IOwner[] = [{ id: 28167 }];
      jest.spyOn(ownerService, 'query').mockReturnValue(of(new HttpResponse({ body: ownerCollection })));
      const additionalOwners = [owner];
      const expectedCollection: IOwner[] = [...additionalOwners, ...ownerCollection];
      jest.spyOn(ownerService, 'addOwnerToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ pet });
      comp.ngOnInit();

      expect(ownerService.query).toHaveBeenCalled();
      expect(ownerService.addOwnerToCollectionIfMissing).toHaveBeenCalledWith(
        ownerCollection,
        ...additionalOwners.map(expect.objectContaining),
      );
      expect(comp.ownersSharedCollection).toEqual(expectedCollection);
    });

    it('Should update editForm', () => {
      const pet: IPet = { id: 456 };
      const type: IPetType = { id: 26008 };
      pet.type = type;
      const owner: IOwner = { id: 12995 };
      pet.owner = owner;

      activatedRoute.data = of({ pet });
      comp.ngOnInit();

      expect(comp.petTypesSharedCollection).toContain(type);
      expect(comp.ownersSharedCollection).toContain(owner);
      expect(comp.pet).toEqual(pet);
    });
  });

  describe('save', () => {
    it('Should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IPet>>();
      const pet = { id: 123 };
      jest.spyOn(petFormService, 'getPet').mockReturnValue(pet);
      jest.spyOn(petService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ pet });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: pet }));
      saveSubject.complete();

      // THEN
      expect(petFormService.getPet).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(petService.update).toHaveBeenCalledWith(expect.objectContaining(pet));
      expect(comp.isSaving).toEqual(false);
    });

    it('Should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IPet>>();
      const pet = { id: 123 };
      jest.spyOn(petFormService, 'getPet').mockReturnValue({ id: null });
      jest.spyOn(petService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ pet: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: pet }));
      saveSubject.complete();

      // THEN
      expect(petFormService.getPet).toHaveBeenCalled();
      expect(petService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('Should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IPet>>();
      const pet = { id: 123 };
      jest.spyOn(petService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ pet });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(petService.update).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });

  describe('Compare relationships', () => {
    describe('comparePetType', () => {
      it('Should forward to petTypeService', () => {
        const entity = { id: 123 };
        const entity2 = { id: 456 };
        jest.spyOn(petTypeService, 'comparePetType');
        comp.comparePetType(entity, entity2);
        expect(petTypeService.comparePetType).toHaveBeenCalledWith(entity, entity2);
      });
    });

    describe('compareOwner', () => {
      it('Should forward to ownerService', () => {
        const entity = { id: 123 };
        const entity2 = { id: 456 };
        jest.spyOn(ownerService, 'compareOwner');
        comp.compareOwner(entity, entity2);
        expect(ownerService.compareOwner).toHaveBeenCalledWith(entity, entity2);
      });
    });
  });
});
