import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { DeviceForm } from './components/device-form/device-form';

@Component({
  imports: [RouterOutlet, DeviceForm],
  selector: 'app-root',
  styleUrl: './app.css',
  templateUrl: './app.html',
})
export class App {
  protected readonly title = signal('device-checker-front');
}
