import { Component } from '@angular/core';
import { FormBuilder } from '@angular/forms';
import { FormGroup } from '@angular/forms';
import { ReactiveFormsModule } from '@angular/forms';
import { Validators } from '@angular/forms';
import { NgClass } from '@angular/common';

@Component({
  imports: [ReactiveFormsModule, NgClass],
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
      location: [''],
    });
  }

  onSubmit() {
    if (this.deviceForm.valid) {
      console.log('Dados do dispositivo:', this.deviceForm.value);
      alert('Dispositivo cadastrado com sucesso!');
    } else {
      console.log('Formulário inválido. Preencha os campos obrigatórios.');
      alert('Por favor, preencha todos os campos obrigatórios (*).');
      this.deviceForm.markAllAsTouched();
    }
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.deviceForm.get(fieldName);
    return !!(field && field.invalid && (field.touched || field.dirty));
  }
}
