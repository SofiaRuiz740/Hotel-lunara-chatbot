import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';
import { UpdateProfilePayload, UserLanguage, UserProfile } from '../../core/models/api.models';
import { AuthService } from '../../core/services/auth.service';
import { ToastService } from '../../core/services/toast.service';
import { UserApiService } from '../../core/services/user-api.service';
import { LoadingSkeletonComponent } from '../../shared/components/loading-skeleton/loading-skeleton.component';

@Component({
  selector: 'app-guest-profile-page',
  standalone: true,
  imports: [ReactiveFormsModule, LoadingSkeletonComponent],
  templateUrl: './guest-profile.page.html',
  styleUrl: './guest-profile.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GuestProfilePageComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly userApi = inject(UserApiService);
  private readonly toastService = inject(ToastService);

  readonly authService = inject(AuthService);
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly profile = signal<UserProfile | null>(null);

  readonly form = this.fb.nonNullable.group({
    nombre: ['', [Validators.required, Validators.maxLength(100)]],
    apellido: ['', [Validators.required, Validators.maxLength(100)]],
    telefono: '',
    nacionalidad: '',
    documentoIdentidad: '',
    idioma: 'ES',
    alergias: '',
    preferenciasCama: '',
    peticionesEspeciales: '',
  });

  ngOnInit(): void {
    this.userApi
      .getMyProfile()
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe((profile) => {
        this.profile.set(profile);
        this.form.patchValue({
          nombre: profile.nombre,
          apellido: profile.apellido,
          telefono: profile.telefono ?? '',
          nacionalidad: profile.nacionalidad ?? '',
          documentoIdentidad: profile.documentoIdentidad ?? '',
          idioma: profile.idioma,
          alergias: profile.alergias ?? '',
          preferenciasCama: profile.preferenciasCama ?? '',
          peticionesEspeciales: profile.peticionesEspeciales ?? '',
        });
      });
  }

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const rawValue = this.form.getRawValue();
    const payload: UpdateProfilePayload = {
      nombre: rawValue.nombre,
      apellido: rawValue.apellido,
      telefono: rawValue.telefono,
      nacionalidad: rawValue.nacionalidad,
      documentoIdentidad: rawValue.documentoIdentidad,
      idioma: rawValue.idioma as UserLanguage,
      alergias: rawValue.alergias,
      preferenciasCama: rawValue.preferenciasCama,
      peticionesEspeciales: rawValue.peticionesEspeciales,
    };

    this.saving.set(true);
    this.userApi
      .updateMyProfile(payload)
      .pipe(finalize(() => this.saving.set(false)))
      .subscribe((profile) => {
        this.profile.set(profile);
        this.authService.patchCurrentUser(profile);
        this.toastService.success('Perfil actualizado correctamente.');
      });
  }
}
