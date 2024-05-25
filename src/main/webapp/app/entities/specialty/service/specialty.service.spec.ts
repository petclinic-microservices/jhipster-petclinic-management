import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { ISpecialty } from '../specialty.model';
import { sampleWithRequiredData, sampleWithNewData, sampleWithPartialData, sampleWithFullData } from '../specialty.test-samples';

import { SpecialtyService } from './specialty.service';

const requireRestSample: ISpecialty = {
  ...sampleWithRequiredData,
};

describe('Specialty Service', () => {
  let service: SpecialtyService;
  let httpMock: HttpTestingController;
  let expectedResult: ISpecialty | ISpecialty[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
    });
    expectedResult = null;
    service = TestBed.inject(SpecialtyService);
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

    it('should create a Specialty', () => {
      const specialty = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(specialty).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a Specialty', () => {
      const specialty = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(specialty).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a Specialty', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of Specialty', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a Specialty', () => {
      const expected = true;

      service.delete(123).subscribe(resp => (expectedResult = resp.ok));

      const req = httpMock.expectOne({ method: 'DELETE' });
      req.flush({ status: 200 });
      expect(expectedResult).toBe(expected);
    });

    it('should handle exceptions for searching a Specialty', () => {
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

    describe('addSpecialtyToCollectionIfMissing', () => {
      it('should add a Specialty to an empty array', () => {
        const specialty: ISpecialty = sampleWithRequiredData;
        expectedResult = service.addSpecialtyToCollectionIfMissing([], specialty);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(specialty);
      });

      it('should not add a Specialty to an array that contains it', () => {
        const specialty: ISpecialty = sampleWithRequiredData;
        const specialtyCollection: ISpecialty[] = [
          {
            ...specialty,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addSpecialtyToCollectionIfMissing(specialtyCollection, specialty);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a Specialty to an array that doesn't contain it", () => {
        const specialty: ISpecialty = sampleWithRequiredData;
        const specialtyCollection: ISpecialty[] = [sampleWithPartialData];
        expectedResult = service.addSpecialtyToCollectionIfMissing(specialtyCollection, specialty);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(specialty);
      });

      it('should add only unique Specialty to an array', () => {
        const specialtyArray: ISpecialty[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const specialtyCollection: ISpecialty[] = [sampleWithRequiredData];
        expectedResult = service.addSpecialtyToCollectionIfMissing(specialtyCollection, ...specialtyArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const specialty: ISpecialty = sampleWithRequiredData;
        const specialty2: ISpecialty = sampleWithPartialData;
        expectedResult = service.addSpecialtyToCollectionIfMissing([], specialty, specialty2);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(specialty);
        expect(expectedResult).toContain(specialty2);
      });

      it('should accept null and undefined values', () => {
        const specialty: ISpecialty = sampleWithRequiredData;
        expectedResult = service.addSpecialtyToCollectionIfMissing([], null, specialty, undefined);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(specialty);
      });

      it('should return initial array if no Specialty is added', () => {
        const specialtyCollection: ISpecialty[] = [sampleWithRequiredData];
        expectedResult = service.addSpecialtyToCollectionIfMissing(specialtyCollection, undefined, null);
        expect(expectedResult).toEqual(specialtyCollection);
      });
    });

    describe('compareSpecialty', () => {
      it('Should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareSpecialty(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('Should return false if one entity is null', () => {
        const entity1 = { id: 123 };
        const entity2 = null;

        const compareResult1 = service.compareSpecialty(entity1, entity2);
        const compareResult2 = service.compareSpecialty(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('Should return false if primaryKey differs', () => {
        const entity1 = { id: 123 };
        const entity2 = { id: 456 };

        const compareResult1 = service.compareSpecialty(entity1, entity2);
        const compareResult2 = service.compareSpecialty(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('Should return false if primaryKey matches', () => {
        const entity1 = { id: 123 };
        const entity2 = { id: 123 };

        const compareResult1 = service.compareSpecialty(entity1, entity2);
        const compareResult2 = service.compareSpecialty(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
