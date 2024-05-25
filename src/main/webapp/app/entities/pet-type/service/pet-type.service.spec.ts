import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { IPetType } from '../pet-type.model';
import { sampleWithRequiredData, sampleWithNewData, sampleWithPartialData, sampleWithFullData } from '../pet-type.test-samples';

import { PetTypeService } from './pet-type.service';

const requireRestSample: IPetType = {
  ...sampleWithRequiredData,
};

describe('PetType Service', () => {
  let service: PetTypeService;
  let httpMock: HttpTestingController;
  let expectedResult: IPetType | IPetType[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
    });
    expectedResult = null;
    service = TestBed.inject(PetTypeService);
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

    it('should create a PetType', () => {
      const petType = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(petType).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a PetType', () => {
      const petType = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(petType).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a PetType', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of PetType', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a PetType', () => {
      const expected = true;

      service.delete(123).subscribe(resp => (expectedResult = resp.ok));

      const req = httpMock.expectOne({ method: 'DELETE' });
      req.flush({ status: 200 });
      expect(expectedResult).toBe(expected);
    });

    it('should handle exceptions for searching a PetType', () => {
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

    describe('addPetTypeToCollectionIfMissing', () => {
      it('should add a PetType to an empty array', () => {
        const petType: IPetType = sampleWithRequiredData;
        expectedResult = service.addPetTypeToCollectionIfMissing([], petType);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(petType);
      });

      it('should not add a PetType to an array that contains it', () => {
        const petType: IPetType = sampleWithRequiredData;
        const petTypeCollection: IPetType[] = [
          {
            ...petType,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addPetTypeToCollectionIfMissing(petTypeCollection, petType);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a PetType to an array that doesn't contain it", () => {
        const petType: IPetType = sampleWithRequiredData;
        const petTypeCollection: IPetType[] = [sampleWithPartialData];
        expectedResult = service.addPetTypeToCollectionIfMissing(petTypeCollection, petType);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(petType);
      });

      it('should add only unique PetType to an array', () => {
        const petTypeArray: IPetType[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const petTypeCollection: IPetType[] = [sampleWithRequiredData];
        expectedResult = service.addPetTypeToCollectionIfMissing(petTypeCollection, ...petTypeArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const petType: IPetType = sampleWithRequiredData;
        const petType2: IPetType = sampleWithPartialData;
        expectedResult = service.addPetTypeToCollectionIfMissing([], petType, petType2);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(petType);
        expect(expectedResult).toContain(petType2);
      });

      it('should accept null and undefined values', () => {
        const petType: IPetType = sampleWithRequiredData;
        expectedResult = service.addPetTypeToCollectionIfMissing([], null, petType, undefined);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(petType);
      });

      it('should return initial array if no PetType is added', () => {
        const petTypeCollection: IPetType[] = [sampleWithRequiredData];
        expectedResult = service.addPetTypeToCollectionIfMissing(petTypeCollection, undefined, null);
        expect(expectedResult).toEqual(petTypeCollection);
      });
    });

    describe('comparePetType', () => {
      it('Should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.comparePetType(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('Should return false if one entity is null', () => {
        const entity1 = { id: 123 };
        const entity2 = null;

        const compareResult1 = service.comparePetType(entity1, entity2);
        const compareResult2 = service.comparePetType(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('Should return false if primaryKey differs', () => {
        const entity1 = { id: 123 };
        const entity2 = { id: 456 };

        const compareResult1 = service.comparePetType(entity1, entity2);
        const compareResult2 = service.comparePetType(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('Should return false if primaryKey matches', () => {
        const entity1 = { id: 123 };
        const entity2 = { id: 123 };

        const compareResult1 = service.comparePetType(entity1, entity2);
        const compareResult2 = service.comparePetType(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
