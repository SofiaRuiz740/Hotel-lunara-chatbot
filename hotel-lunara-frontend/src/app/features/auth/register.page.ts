import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { AbstractControl, ReactiveFormsModule, ValidationErrors, ValidatorFn, FormBuilder, Validators } from '@angular/forms';
import { finalize } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { RegisterPayload, UserLanguage } from '../../core/models/api.models';
import { ToastService } from '../../core/services/toast.service';
import { getAuthImage } from '../../core/utils/media.utils';
import { RouterLink } from '@angular/router';

const passwordsMatchValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const password = control.get('password')?.value;
  const confirmPassword = control.get('confirmPassword')?.value;
  return password && confirmPassword && password !== confirmPassword ? { passwordMismatch: true } : null;
};

@Component({
  selector: 'app-register-page',
  standalone: true,
  imports: [RouterLink, ReactiveFormsModule],
  templateUrl: './register.page.html',
  styleUrl: './register.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RegisterPageComponent {
  private readonly fb = inject(FormBuilder);
  private readonly toastService = inject(ToastService);

  readonly authService = inject(AuthService);
  readonly loading = signal(false);
  readonly authImage = getAuthImage();

  readonly form = this.fb.group(
    {
      nombre: ['', [Validators.required, Validators.maxLength(100)]],
      apellido: ['', [Validators.required, Validators.maxLength(100)]],
      email: ['', [Validators.required, Validators.email]],
      telefono: ['', [Validators.required]],
      password: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', [Validators.required]],
      nacionalidad: ['', [Validators.required]],
      documentoIdentidad: ['', [Validators.required]],
      idioma: 'ES',
      alergias: '',
      preferenciasCama: '',
      peticionesEspeciales: '',
      terms: [false, Validators.requiredTrue],
    },
    { validators: passwordsMatchValidator },
  );

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const rawValue = this.form.getRawValue();
    const payload: RegisterPayload = {
      nombre: rawValue.nombre ?? '',
      apellido: rawValue.apellido ?? '',
      email: rawValue.email ?? '',
      telefono: rawValue.telefono ?? '',
      password: rawValue.password ?? '',
      nacionalidad: rawValue.nacionalidad ?? '',
      documentoIdentidad: rawValue.documentoIdentidad ?? '',
      idioma: (rawValue.idioma ?? 'ES') as UserLanguage,
      alergias: rawValue.alergias ?? '',
      preferenciasCama: rawValue.preferenciasCama ?? '',
      peticionesEspeciales: rawValue.peticionesEspeciales ?? '',
    };

    this.loading.set(true);
    this.authService
      .register(payload)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe(() => {
        this.toastService.success('Cuenta creada correctamente.');
        this.authService.redirectAfterLogin();
      });
  }

  isInvalid(controlName: string): boolean {
    const control = this.form.get(controlName);
    return !!control && control.invalid && control.touched;
  }

  isValid(controlName: string): boolean {
    const control = this.form.get(controlName);
    return !!control && control.valid && control.touched;
  }
}
