import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { ToastService } from '../../core/services/toast.service';
import { getAuthImage } from '../../core/utils/media.utils';

@Component({
  selector: 'app-login-page',
  standalone: true,
  imports: [RouterLink, ReactiveFormsModule],
  templateUrl: './login.page.html',
  styleUrl: './login.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoginPageComponent {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly toastService = inject(ToastService);

  readonly authService = inject(AuthService);
  readonly loading = signal(false);
  readonly authImage = getAuthImage();

  readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]],
    rememberMe: true,
  });

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const { rememberMe, ...payload } = this.form.getRawValue();
    const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl');

    this.loading.set(true);
    this.authService
      .login(payload, rememberMe)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe(() => {
        this.toastService.success('Sesion iniciada correctamente.');
        this.authService.redirectAfterLogin(returnUrl);
      });
  }

  isInvalid(controlName: 'email' | 'password'): boolean {
    const control = this.form.controls[controlName];
    return control.invalid && control.touched;
  }

  isValid(controlName: 'email' | 'password'): boolean {
    const control = this.form.controls[controlName];
    return control.valid && control.touched;
  }
}
