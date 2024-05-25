import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { of, Subject, from } from 'rxjs';

import { VetSpecialtyService } from '../service/vet-specialty.service';
import { IVetSpecialty } from '../vet-specialty.model';
import { VetSpecialtyFormService } from './vet-specialty-form.service';

import { VetSpecialtyUpdateComponent } from './vet-specialty-update.component';

describe('VetSpecialty Management Update Component', () => {
  let comp: VetSpecialtyUpdateComponent;
  let fixture: ComponentFixture<VetSpecialtyUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let vetSpecialtyFormService: VetSpecialtyFormService;
  let vetSpecialtyService: VetSpecialtyService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, VetSpecialtyUpdateComponent],
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
      .overrideTemplate(VetSpecialtyUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(VetSpecialtyUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    vetSpecialtyFormService = TestBed.inject(VetSpecialtyFormService);
    vetSpecialtyService = TestBed.inject(VetSpecialtyService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('Should update editForm', () => {
      const vetSpecialty: IVetSpecialty = { id: 456 };

      activatedRoute.data = of({ vetSpecialty });
      comp.ngOnInit();

      expect(comp.vetSpecialty).toEqual(vetSpecialty);
    });
  });

  describe('save', () => {
    it('Should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IVetSpecialty>>();
      const vetSpecialty = { id: 123 };
      jest.spyOn(vetSpecialtyFormService, 'getVetSpecialty').mockReturnValue({ id: null });
      jest.spyOn(vetSpecialtyService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ vetSpecialty: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: vetSpecialty }));
      saveSubject.complete();

      // THEN
      expect(vetSpecialtyFormService.getVetSpecialty).toHaveBeenCalled();
      expect(vetSpecialtyService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });
  });
});
