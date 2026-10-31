import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class DeviceService {
  private apiUrl = 'http://localhost:4200/api/devices';

  constructor(private http: HttpClient) {}

  createDevice(deviceData: any): Observable<any> {
    return this.http.post<any>(this.apiUrl, deviceData);
  }
}
