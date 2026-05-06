import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ActivatedRoute, Router } from '@angular/router';
import { finalize, forkJoin } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { ReservationApiService } from '../../core/services/reservation-api.service';
import { ToastService } from '../../core/services/toast.service';
import { RoomApiService } from '../../core/services/room-api.service';
import { Room } from '../../core/models/api.models';
import {
  currency,
  daysBetween,
  getRoomGallery,
  getRoomStatusLabel,
  getRoomTypeLabel,
  splitCommaValues,
} from '../../core/utils/media.utils';
import { LoadingSkeletonComponent } from '../../shared/components/loading-skeleton/loading-skeleton.component';
import { ModalShellComponent } from '../../shared/components/modal-shell/modal-shell.component';
import { StatusBadgeComponent } from '../../shared/components/status-badge/status-badge.component';

@Component({
  selector: 'app-room-detail-page',
  standalone: true,
  imports: [
    RouterLink,
    ReactiveFormsModule,
    LoadingSkeletonComponent,
    ModalShellComponent,
    StatusBadgeComponent,
  ],
  templateUrl: './room-detail.page.html',
  styleUrl: './room-detail.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RoomDetailPageComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly roomApi = inject(RoomApiService);
  private readonly reservationApi = inject(ReservationApiService);
  private readonly fb = inject(FormBuilder);
  private readonly toastService = inject(ToastService);

  readonly authService = inject(AuthService);

  readonly loading = signal(true);
  readonly savingReservation = signal(false);
  readonly room = signal<Room | null>(null);
  readonly allRooms = signal<Room[]>([]);
  readonly lightboxImage = signal<string | null>(null);
  readonly today = new Date().toISOString().slice(0, 10);

  readonly reservationForm = this.fb.nonNullable.group({
    checkIn: ['', Validators.required],
    checkOut: ['', Validators.required],
    cantidadAdultos: [2, [Validators.required, Validators.min(1)]],
    cantidadNinos: [0, [Validators.min(0)]],
    peticionesEspeciales: '',
  });

  readonly gallery = computed(() => (this.room() ? getRoomGallery(this.room() as Room) : []));
  readonly amenities = computed(() => splitCommaValues(this.room()?.amenities));
  readonly nights = computed(() => {
    const { checkIn, checkOut } = this.reservationForm.getRawValue();
    return checkIn && checkOut ? daysBetween(checkIn, checkOut) : 0;
  });
  readonly total = computed(() => this.nights() * (this.room()?.precioPorNoche ?? 0));
  readonly similarRooms = computed(() => {
    const currentRoom = this.room();
    if (!currentRoom) {
      return [];
    }

    return this.allRooms()
      .filter((room) => room.id !== currentRoom.id && room.tipo === currentRoom.tipo)
      .slice(0, 3);
  });

  readonly getRoomGallery = getRoomGallery;
  readonly getRoomTypeLabel = getRoomTypeLabel;
  readonly getRoomStatusLabel = getRoomStatusLabel;
  readonly currency = currency;

  ngOnInit(): void {
    const roomId = Number(this.route.snapshot.paramMap.get('id'));
    if (!roomId) {
      void this.router.navigateByUrl('/habitaciones');
      return;
    }

    forkJoin({
      room: this.roomApi.getRoomById(roomId),
      rooms: this.roomApi.getRooms(),
    })
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe(({ room, rooms }) => {
        this.room.set(room);
        this.allRooms.set(rooms);
      });
  }

  adjustCounter(controlName: 'cantidadAdultos' | 'cantidadNinos', delta: number): void {
    const currentValue = this.reservationForm.controls[controlName].value;
    const nextValue = Math.max(controlName === 'cantidadAdultos' ? 1 : 0, currentValue + delta);
    this.reservationForm.controls[controlName].setValue(nextValue);
  }

  openLightbox(image: string): void {
    this.lightboxImage.set(image);
  }

  closeLightbox(): void {
    this.lightboxImage.set(null);
  }

  submitReservation(): void {
    const room = this.room();
    if (!room) {
      return;
    }

    if (!this.authService.isLoggedIn()) {
      void this.router.navigate(['/auth/login'], { queryParams: { returnUrl: this.router.url } });
      return;
    }

    if (this.reservationForm.invalid) {
      this.reservationForm.markAllAsTouched();
      return;
    }

    const payload = {
      roomId: room.id,
      ...this.reservationForm.getRawValue(),
    };

    this.savingReservation.set(true);
    this.reservationApi
      .createReservation(payload)
      .pipe(finalize(() => this.savingReservation.set(false)))
      .subscribe(() => {
        this.toastService.success('Reserva creada correctamente.');
        void this.router.navigateByUrl('/mi-cuenta/reservas');
      });
  }
}
