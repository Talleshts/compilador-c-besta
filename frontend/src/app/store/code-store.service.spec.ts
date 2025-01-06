import { TestBed } from '@angular/core/testing';

import { CodeStoreService } from './code-store.service';

describe('CodeStoreService', () => {
  let service: CodeStoreService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(CodeStoreService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
