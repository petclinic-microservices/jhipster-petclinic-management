import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { of } from 'rxjs';

import { SpecialtyDetailComponent } from './specialty-detail.component';

describe('Specialty Management Detail Component', () => {
  let comp: SpecialtyDetailComponent;
  let fixture: ComponentFixture<SpecialtyDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SpecialtyDetailComponent],
      providers: [
        provideRouter(
          [
            {
              path: '**',
              component: SpecialtyDetailComponent,
              resolve: { specialty: () => of({ id: 123 }) },
            },
          ],
          withComponentInputBinding(),
        ),
      ],
    })
      .overrideTemplate(SpecialtyDetailComponent, '')
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SpecialtyDetailComponent);
    comp = fixture.componentInstance;
  });

  describe('OnInit', () => {
    it('Should load specialty on init', async () => {
      const harness = await RouterTestingHarness.create();
      const instance = await harness.navigateByUrl('/', SpecialtyDetailComponent);

      // THEN
      expect(instance.specialty()).toEqual(expect.objectContaining({ id: 123 }));
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
