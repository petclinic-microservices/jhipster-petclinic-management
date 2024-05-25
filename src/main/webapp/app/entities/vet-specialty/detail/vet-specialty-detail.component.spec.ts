import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { of } from 'rxjs';

import { VetSpecialtyDetailComponent } from './vet-specialty-detail.component';

describe('VetSpecialty Management Detail Component', () => {
  let comp: VetSpecialtyDetailComponent;
  let fixture: ComponentFixture<VetSpecialtyDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [VetSpecialtyDetailComponent],
      providers: [
        provideRouter(
          [
            {
              path: '**',
              component: VetSpecialtyDetailComponent,
              resolve: { vetSpecialty: () => of({ id: 123 }) },
            },
          ],
          withComponentInputBinding(),
        ),
      ],
    })
      .overrideTemplate(VetSpecialtyDetailComponent, '')
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(VetSpecialtyDetailComponent);
    comp = fixture.componentInstance;
  });

  describe('OnInit', () => {
    it('Should load vetSpecialty on init', async () => {
      const harness = await RouterTestingHarness.create();
      const instance = await harness.navigateByUrl('/', VetSpecialtyDetailComponent);

      // THEN
      expect(instance.vetSpecialty()).toEqual(expect.objectContaining({ id: 123 }));
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
