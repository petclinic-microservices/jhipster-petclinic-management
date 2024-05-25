import { TestBed } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ActivatedRouteSnapshot, ActivatedRoute, Router, convertToParamMap } from '@angular/router';
import { of } from 'rxjs';

import { IVetSpecialty } from '../vet-specialty.model';
import { VetSpecialtyService } from '../service/vet-specialty.service';

import vetSpecialtyResolve from './vet-specialty-routing-resolve.service';

describe('VetSpecialty routing resolve service', () => {
  let mockRouter: Router;
  let mockActivatedRouteSnapshot: ActivatedRouteSnapshot;
  let service: VetSpecialtyService;
  let resultVetSpecialty: IVetSpecialty | null | undefined;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: convertToParamMap({}),
            },
          },
        },
      ],
    });
    mockRouter = TestBed.inject(Router);
    jest.spyOn(mockRouter, 'navigate').mockImplementation(() => Promise.resolve(true));
    mockActivatedRouteSnapshot = TestBed.inject(ActivatedRoute).snapshot;
    service = TestBed.inject(VetSpecialtyService);
    resultVetSpecialty = undefined;
  });

  describe('resolve', () => {
    it('should return IVetSpecialty returned by find', () => {
      // GIVEN
      service.find = jest.fn(id => of(new HttpResponse({ body: { id } })));
      mockActivatedRouteSnapshot.params = { id: 123 };

      // WHEN
      TestBed.runInInjectionContext(() => {
        vetSpecialtyResolve(mockActivatedRouteSnapshot).subscribe({
          next(result) {
            resultVetSpecialty = result;
          },
        });
      });

      // THEN
      expect(service.find).toBeCalledWith(123);
      expect(resultVetSpecialty).toEqual({ id: 123 });
    });

    it('should return null if id is not provided', () => {
      // GIVEN
      service.find = jest.fn();
      mockActivatedRouteSnapshot.params = {};

      // WHEN
      TestBed.runInInjectionContext(() => {
        vetSpecialtyResolve(mockActivatedRouteSnapshot).subscribe({
          next(result) {
            resultVetSpecialty = result;
          },
        });
      });

      // THEN
      expect(service.find).not.toBeCalled();
      expect(resultVetSpecialty).toEqual(null);
    });

    it('should route to 404 page if data not found in server', () => {
      // GIVEN
      jest.spyOn(service, 'find').mockReturnValue(of(new HttpResponse<IVetSpecialty>({ body: null })));
      mockActivatedRouteSnapshot.params = { id: 123 };

      // WHEN
      TestBed.runInInjectionContext(() => {
        vetSpecialtyResolve(mockActivatedRouteSnapshot).subscribe({
          next(result) {
            resultVetSpecialty = result;
          },
        });
      });

      // THEN
      expect(service.find).toBeCalledWith(123);
      expect(resultVetSpecialty).toEqual(undefined);
      expect(mockRouter.navigate).toHaveBeenCalledWith(['404']);
    });
  });
});
