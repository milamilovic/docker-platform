import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Regulars } from './regulars';

describe('Regulars', () => {
  let component: Regulars;
  let fixture: ComponentFixture<Regulars>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [Regulars]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Regulars);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
