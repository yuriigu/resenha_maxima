import { Component } from '@angular/core';
import { FormBuilder } from '@angular/forms';
import { FormGroup } from '@angular/forms';
import { ReactiveFormsModule } from '@angular/forms';
import { Validators } from '@angular/forms';

@Component({
  imports: [ReactiveFormsModule],
  selector: 'app-device-form',
  styleUrl: './device-form.css',
  templateUrl: './device-form.html',
})
export class DeviceForm {
  deviceForm: FormGroup;

  constructor(private fb: FormBuilder) {
    this.deviceForm = this.fb.group({
      name: ['', Validators.required],
      brand: ['', Validators.required],
      model: [''],
      ipAddress: ['', Validators.required],
      macAddress: [''],
      permissions: ['', Validators.required],
      location: ['']
    })
  }
}
