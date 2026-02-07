import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OfficialRepositories } from './official-repositories';

describe('OfficialRepositories', () => {
  let component: OfficialRepositories;
  let fixture: ComponentFixture<OfficialRepositories>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [OfficialRepositories]
    })
    .compileComponents();

    fixture = TestBed.createComponent(OfficialRepositories);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
