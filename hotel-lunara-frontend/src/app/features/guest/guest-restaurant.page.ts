import { ChangeDetectionStrategy, Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { debounceTime, finalize, forkJoin, of, startWith, switchMap } from 'rxjs';
import { Reservation, RestaurantAvailability, RestaurantOccasion, RestaurantReservation } from '../../core/models/api.models';
import { ReservationApiService } from '../../core/services/reservation-api.service';
import { RestaurantApiService } from '../../core/services/restaurant-api.service';
import { ToastService } from '../../core/services/toast.service';
import { formatDate, formatTime, getRoomTypeLabel } from '../../core/utils/media.utils';
import { LoadingSkeletonComponent } from '../../shared/components/loading-skeleton/loading-skeleton.component';
import { StatusBadgeComponent } from '../../shared/components/status-badge/status-badge.component';

@Component({
  selector: 'app-guest-restaurant-page',
  standalone: true,
  imports: [ReactiveFormsModule, LoadingSkeletonComponent, StatusBadgeComponent],
  templateUrl: './guest-restaurant.page.html',
  styleUrl: './guest-restaurant.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GuestRestaurantPageComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly reservationApi = inject(ReservationApiService);
  private readonly restaurantApi = inject(RestaurantApiService);
  private readonly toastService = inject(ToastService);
  private readonly destroyRef = inject(DestroyRef);

  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly reservations = signal<RestaurantReservation[]>([]);
  readonly hotelReservations = signal<Reservation[]>([]);
  readonly availability = signal<RestaurantAvailability | null>(null);

  readonly occasionOptions: RestaurantOccasion[] = ['NINGUNA', 'CUMPLEANOS', 'ANIVERSARIO', 'NEGOCIOS'];
  readonly today = new Date().toISOString().slice(0, 10);
  readonly formatDate = formatDate;
  readonly formatTime = formatTime;
  readonly getRoomTypeLabel = getRoomTypeLabel;

  readonly form = this.fb.nonNullable.group({
    fecha: ['', Validators.required],
    hora: ['19:00', Validators.required],
    cantidadPersonas: [2, [Validators.required, Validators.min(1)]],
    ocasionEspecial: 'NINGUNA' as RestaurantOccasion,
    peticiones: '',
    reservationHotelId: 0,
  });

  readonly upcomingReservations = computed(() =>
    [...this.reservations()]
      .filter((reservation) => reservation.estado === 'CONFIRMADA')
      .sort((left, right) => `${left.fecha}${left.hora}`.localeCompare(`${right.fecha}${right.hora}`)),
  );
  readonly linkableReservations = computed(() =>
    this.hotelReservations().filter((reservation) => ['CONFIRMADA', 'ACTIVA'].includes(reservation.estado)),
  );

  ngOnInit(): void {
    this.reload();

    this.form.valueChanges
      .pipe(
        startWith(this.form.getRawValue()),
        debounceTime(250),
        switchMap((value) => {
          if (!value.fecha || !value.hora || !value.cantidadPersonas) {
            this.availability.set(null);
            return of(null);
          }

          return this.restaurantApi.getAvailability(value.fecha, value.hora, value.cantidadPersonas);
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((availability) => this.availability.set(availability));
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const value = this.form.getRawValue();
    this.saving.set(true);
    this.restaurantApi
      .createReservation({
        fecha: value.fecha,
        hora: value.hora,
        cantidadPersonas: value.cantidadPersonas,
        ocasionEspecial: value.ocasionEspecial,
        peticiones: value.peticiones,
        reservationHotelId: value.reservationHotelId || undefined,
      })
      .pipe(finalize(() => this.saving.set(false)))
      .subscribe(() => {
        this.toastService.success('Reserva de restaurante creada correctamente.');
        this.form.patchValue({ peticiones: '' });
        this.reload();
      });
  }

  cancelReservation(reservationId: number): void {
    this.restaurantApi.cancelReservation(reservationId).subscribe(() => {
      this.toastService.success('Reserva de restaurante cancelada.');
      this.reload();
    });
  }

  private reload(): void {
    this.loading.set(true);
    forkJoin({
      reservations: this.restaurantApi.getMyReservations(),
      hotelReservations: this.reservationApi.getMyReservations(),
    })
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe(({ reservations, hotelReservations }) => {
        this.reservations.set(reservations);
        this.hotelReservations.set(hotelReservations);
      });
  }
}
