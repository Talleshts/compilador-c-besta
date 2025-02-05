import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class CodeStoreService {
  // Endpoint fake pra testar o payload do POST
  // private apiUrl: string = 'https://jsonplaceholder.typicode.com/posts';

  private apiUrlLexico: string = 'http://localhost:8080/api/analyze-lexica';
  private apiUrlSintatico: string = 'http://localhost:8080/api/analyze-sintatica';

  constructor(private http: HttpClient) {}

  analisadorLexico(codeContent: string): Observable<any> {
    return this.http.post(this.apiUrlLexico, { code: codeContent });
  }

  analisadorSintatico(codeContent: string): Observable<SyntaxError[]>{
    return this.http.post<SyntaxError[]>(this.apiUrlSintatico, { code: codeContent });
  }
}
