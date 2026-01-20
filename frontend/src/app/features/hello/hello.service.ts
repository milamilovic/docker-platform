import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { env } from '../../shared/env';

@Injectable({
    providedIn: 'root',
})
export class HelloService {
    constructor(private http: HttpClient) {}

    hello(): Observable<string> {
        return this.http.get(`${env.apiUrl}/hello`, {
            responseType: 'text',
        });
    }

}
