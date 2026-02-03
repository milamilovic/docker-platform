import { Injectable, inject } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
} from '@angular/common/http';
import {EMPTY, Observable} from 'rxjs';
import { AuthService } from './auth.service';
import { Router } from '@angular/router';


@Injectable()
export class Interceptor implements HttpInterceptor {

  private router = inject(Router);
  private authService = inject(AuthService);
  intercept(req: HttpRequest<any>, handler: HttpHandler): Observable<HttpEvent<any>> {


    const accessToken: any = localStorage.getItem('token');
    console.log("INTERCEPT");
    if (this.authService.isAccessTokenExpired()){
      this.authService.logOut();
      return EMPTY;
    }
    // if (req.headers.get('skip')) return handler.handle(req);

    if (accessToken) {
      const cloned = req.clone({
        headers: req.headers.set('Authorization', "Bearer " + accessToken)
      });

      return handler.handle(cloned);
    } else {
      return handler.handle(req);
    }

  }
}
