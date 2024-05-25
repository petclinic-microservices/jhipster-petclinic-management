import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { of } from 'rxjs';

import { PetTypeDetailComponent } from './pet-type-detail.component';

describe('PetType Management Detail Component', () => {
  let comp: PetTypeDetailComponent;
  let fixture: ComponentFixture<PetTypeDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PetTypeDetailComponent],
      providers: [
        provideRouter(
          [
            {
              path: '**',
              component: PetTypeDetailComponent,
              resolve: { petType: () => of({ id: 123 }) },
            },
          ],
          withComponentInputBinding(),
        ),
      ],
    })
      .overrideTemplate(PetTypeDetailComponent, '')
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PetTypeDetailComponent);
    comp = fixture.componentInstance;
  });

  describe('OnInit', () => {
    it('Should load petType on init', async () => {
      const harness = await RouterTestingHarness.create();
      const instance = await harness.navigateByUrl('/', PetTypeDetailComponent);

      // THEN
      expect(instance.petType()).toEqual(expect.objectContaining({ id: 123 }));
    });
  });

  describe('PreviousState', () => {
    it('Should navigate to previous state', () => {
      jest.spyOn(window.history, 'back');
      comp.previousState();
      expect(window.history.back).toHaveBeenCalled();
    });
  });
});
