import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { of, Subject, from } from 'rxjs';

import { OwnerService } from '../service/owner.service';
import { IOwner } from '../owner.model';
import { OwnerFormService } from './owner-form.service';

import { OwnerUpdateComponent } from './owner-update.component';

describe('Owner Management Update Component', () => {
  let comp: OwnerUpdateComponent;
  let fixture: ComponentFixture<OwnerUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let ownerFormService: OwnerFormService;
  let ownerService: OwnerService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, OwnerUpdateComponent],
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
      .overrideTemplate(OwnerUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(OwnerUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    ownerFormService = TestBed.inject(OwnerFormService);
    ownerService = TestBed.inject(OwnerService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('Should update editForm', () => {
      const owner: IOwner = { id: 456 };

      activatedRoute.data = of({ owner });
      comp.ngOnInit();

      expect(comp.owner).toEqual(owner);
    });
  });

  describe('save', () => {
    it('Should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IOwner>>();
      const owner = { id: 123 };
      jest.spyOn(ownerFormService, 'getOwner').mockReturnValue(owner);
      jest.spyOn(ownerService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ owner });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: owner }));
      saveSubject.complete();

      // THEN
      expect(ownerFormService.getOwner).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(ownerService.update).toHaveBeenCalledWith(expect.objectContaining(owner));
      expect(comp.isSaving).toEqual(false);
    });

    it('Should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IOwner>>();
      const owner = { id: 123 };
      jest.spyOn(ownerFormService, 'getOwner').mockReturnValue({ id: null });
      jest.spyOn(ownerService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ owner: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: owner }));
      saveSubject.complete();

      // THEN
      expect(ownerFormService.getOwner).toHaveBeenCalled();
      expect(ownerService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('Should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IOwner>>();
      const owner = { id: 123 };
      jest.spyOn(ownerService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ owner });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(ownerService.update).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });
});
