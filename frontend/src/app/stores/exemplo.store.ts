import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable()
export class ExemploStore {
  // Usar sempre nossa URL de do backend (bom seria deixar isso setado em algum lugar)
  private apiUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  getHello(): Observable<string> {
    return this.http.get(`${this.apiUrl}/hello`, { responseType: 'text' });
  }
}
