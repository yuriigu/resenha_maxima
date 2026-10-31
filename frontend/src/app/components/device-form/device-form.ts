import { Component } from '@angular/core';
import { FormBuilder } from '@angular/forms';
import { FormGroup } from '@angular/forms';
import { ReactiveFormsModule } from '@angular/forms';
import { Validators } from '@angular/forms';
import { NgClass } from '@angular/common';
import { DeviceService } from '../../services/device.service';

@Component({
  imports: [ReactiveFormsModule, NgClass],
  selector: 'app-device-form',
  styleUrl: './device-form.css',
  templateUrl: './device-form.html',
})
export class DeviceForm {
  deviceForm: FormGroup;

  constructor(
    private fb: FormBuilder,
    private deviceService: DeviceService
  ) {
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
      this.deviceService.createDevice(this.deviceForm.value).subscribe({
        next: (response) => {
          console.log('Dispositivo cadastrado com sucesso!', response);
          alert('Dispositivo cadastrado com sucesso!');
          this.deviceForm.reset();
        },
        error: (err) =>{
          console. error('Erro ao cadastrar dispositivo:', err)
          alert('Erro ao conectar com o servidor.');
        }
      });
      } else{
        console.log('Formulário inválido.');
        alert('Por favor, preencha todos os campos obrigatórios (*).');
        this.deviceForm.markAllAsTouched();
    }
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.deviceForm.get(fieldName);
    return !!(field && field.invalid && (field.touched || field.dirty));
  }
}
