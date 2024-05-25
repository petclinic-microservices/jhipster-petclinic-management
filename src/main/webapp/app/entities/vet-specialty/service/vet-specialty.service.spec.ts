import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { IVetSpecialty } from '../vet-specialty.model';
import { sampleWithRequiredData, sampleWithNewData, sampleWithPartialData, sampleWithFullData } from '../vet-specialty.test-samples';

import { VetSpecialtyService } from './vet-specialty.service';

const requireRestSample: IVetSpecialty = {
  ...sampleWithRequiredData,
};

describe('VetSpecialty Service', () => {
  let service: VetSpecialtyService;
  let httpMock: HttpTestingController;
  let expectedResult: IVetSpecialty | IVetSpecialty[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
    });
    expectedResult = null;
    service = TestBed.inject(VetSpecialtyService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  describe('Service methods', () => {
    it('should find an element', () => {
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.find(123).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should create a VetSpecialty', () => {
      const vetSpecialty = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(vetSpecialty).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of VetSpecialty', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a VetSpecialty', () => {
      const expected = true;

      service.delete(123).subscribe(resp => (expectedResult = resp.ok));

      const req = httpMock.expectOne({ method: 'DELETE' });
      req.flush({ status: 200 });
      expect(expectedResult).toBe(expected);
    });

    it('should handle exceptions for searching a VetSpecialty', () => {
      const queryObject: any = {
        page: 0,
        size: 20,
        query: '',
        sort: [],
      };
      service.search(queryObject).subscribe(() => expectedResult);

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush(null, { status: 500, statusText: 'Internal Server Error' });
      expect(expectedResult).toBe(null);
    });

    describe('addVetSpecialtyToCollectionIfMissing', () => {
      it('should add a VetSpecialty to an empty array', () => {
        const vetSpecialty: IVetSpecialty = sampleWithRequiredData;
        expectedResult = service.addVetSpecialtyToCollectionIfMissing([], vetSpecialty);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(vetSpecialty);
      });

      it('should not add a VetSpecialty to an array that contains it', () => {
        const vetSpecialty: IVetSpecialty = sampleWithRequiredData;
        const vetSpecialtyCollection: IVetSpecialty[] = [
          {
            ...vetSpecialty,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addVetSpecialtyToCollectionIfMissing(vetSpecialtyCollection, vetSpecialty);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a VetSpecialty to an array that doesn't contain it", () => {
        const vetSpecialty: IVetSpecialty = sampleWithRequiredData;
        const vetSpecialtyCollection: IVetSpecialty[] = [sampleWithPartialData];
        expectedResult = service.addVetSpecialtyToCollectionIfMissing(vetSpecialtyCollection, vetSpecialty);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(vetSpecialty);
      });

      it('should add only unique VetSpecialty to an array', () => {
        const vetSpecialtyArray: IVetSpecialty[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const vetSpecialtyCollection: IVetSpecialty[] = [sampleWithRequiredData];
        expectedResult = service.addVetSpecialtyToCollectionIfMissing(vetSpecialtyCollection, ...vetSpecialtyArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const vetSpecialty: IVetSpecialty = sampleWithRequiredData;
        const vetSpecialty2: IVetSpecialty = sampleWithPartialData;
        expectedResult = service.addVetSpecialtyToCollectionIfMissing([], vetSpecialty, vetSpecialty2);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(vetSpecialty);
        expect(expectedResult).toContain(vetSpecialty2);
      });

      it('should accept null and undefined values', () => {
        const vetSpecialty: IVetSpecialty = sampleWithRequiredData;
        expectedResult = service.addVetSpecialtyToCollectionIfMissing([], null, vetSpecialty, undefined);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(vetSpecialty);
      });

      it('should return initial array if no VetSpecialty is added', () => {
        const vetSpecialtyCollection: IVetSpecialty[] = [sampleWithRequiredData];
        expectedResult = service.addVetSpecialtyToCollectionIfMissing(vetSpecialtyCollection, undefined, null);
        expect(expectedResult).toEqual(vetSpecialtyCollection);
      });
    });

    describe('compareVetSpecialty', () => {
      it('Should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareVetSpecialty(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('Should return false if one entity is null', () => {
        const entity1 = { id: 123 };
        const entity2 = null;

        const compareResult1 = service.compareVetSpecialty(entity1, entity2);
        const compareResult2 = service.compareVetSpecialty(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('Should return false if primaryKey differs', () => {
        const entity1 = { id: 123 };
        const entity2 = { id: 456 };

        const compareResult1 = service.compareVetSpecialty(entity1, entity2);
        const compareResult2 = service.compareVetSpecialty(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('Should return false if primaryKey matches', () => {
        const entity1 = { id: 123 };
        const entity2 = { id: 123 };

        const compareResult1 = service.compareVetSpecialty(entity1, entity2);
        const compareResult2 = service.compareVetSpecialty(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
