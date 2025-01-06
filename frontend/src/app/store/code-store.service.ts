import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class CodeStoreService {

  // Endpoint fake pra testar o payload do POST
  // private apiUrl: string = 'https://jsonplaceholder.typicode.com/posts';

  private apiUrl: string = 'http://localhost:8080/api/post';

  constructor(private http: HttpClient) {}

  postCode(codeContent: string): Observable<any> {
    return this.http.post(this.apiUrl, { code: codeContent });
  }
}
