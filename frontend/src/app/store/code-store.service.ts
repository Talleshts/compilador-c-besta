import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ErroSintatico } from '../interface/erro-sintatico.interface';
import { CustomToken } from '../interface/token.interface';

@Injectable({
  providedIn: 'root',
})
export class CodeStoreService {
  private apiUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  analyzeLexica(code: string): Observable<CustomToken[]> {
    return this.http.post<CustomToken[]>(`${this.apiUrl}/analyze-lexica`, {
      code,
    });
  }

  analyzeSintatica(code: string): Observable<ErroSintatico[]> {
    return this.http.post<ErroSintatico[]>(`${this.apiUrl}/analyze-sintatica`, {
      code,
    });
  }
}
