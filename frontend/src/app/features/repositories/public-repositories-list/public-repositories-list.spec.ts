import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PublicRepositoriesList } from './public-repositories-list';

describe('PublicRepositoriesList', () => {
  let component: PublicRepositoriesList;
  let fixture: ComponentFixture<PublicRepositoriesList>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [PublicRepositoriesList]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PublicRepositoriesList);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
