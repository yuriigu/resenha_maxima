import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class DeviceService {
  private readonly http = inject(HttpClient);

  // URL relativa: o navegador chama http://localhost:4000/api/devices (mesma origem do web container)
  // e o Express/SSR faz proxy para http://api:8080 via rede Docker.
  private readonly apiUrl = '/api/devices';

  constructor() {}

  createDevice(deviceData: any): Observable<any> {
    return this.http.post<any>(this.apiUrl, deviceData);
  }
}
