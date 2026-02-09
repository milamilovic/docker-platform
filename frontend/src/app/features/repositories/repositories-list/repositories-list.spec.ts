import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RepositoriesList } from './repositories-list';

describe('RepositoriesList', () => {
  let component: RepositoriesList;
  let fixture: ComponentFixture<RepositoriesList>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [RepositoriesList]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RepositoriesList);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
