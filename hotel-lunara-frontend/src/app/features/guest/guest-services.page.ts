import { ChangeDetectionStrategy, Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { debounceTime, finalize, forkJoin, of, startWith, switchMap } from 'rxjs';
import { HotelServiceItem, Reservation, ServiceAvailability, ServiceRequest } from '../../core/models/api.models';
import { HotelServiceApiService } from '../../core/services/hotel-service-api.service';
import { ReservationApiService } from '../../core/services/reservation-api.service';
import { ToastService } from '../../core/services/toast.service';
import { currency, formatDate, formatTime } from '../../core/utils/media.utils';
import { LoadingSkeletonComponent } from '../../shared/components/loading-skeleton/loading-skeleton.component';
import { StatusBadgeComponent } from '../../shared/components/status-badge/status-badge.component';

@Component({
  selector: 'app-guest-services-page',
  standalone: true,
  imports: [ReactiveFormsModule, LoadingSkeletonComponent, StatusBadgeComponent],
  templateUrl: './guest-services.page.html',
  styleUrl: './guest-services.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GuestServicesPageComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly hotelServiceApi = inject(HotelServiceApiService);
  private readonly reservationApi = inject(ReservationApiService);
  private readonly toastService = inject(ToastService);
  private readonly destroyRef = inject(DestroyRef);

  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly services = signal<HotelServiceItem[]>([]);
  readonly requests = signal<ServiceRequest[]>([]);
  readonly reservations = signal<Reservation[]>([]);
  readonly availability = signal<ServiceAvailability | null>(null);

  readonly today = new Date().toISOString().slice(0, 10);
  readonly currency = currency;
  readonly formatDate = formatDate;
  readonly formatTime = formatTime;

  readonly form = this.fb.nonNullable.group({
    serviceId: 0,
    reservationId: 0,
    fechaSolicitada: '',
    horaSolicitada: '',
    notas: '',
  });

  readonly activeReservations = computed(() =>
    this.reservations().filter((reservation) => ['ACTIVA', 'CONFIRMADA'].includes(reservation.estado)),
  );
  readonly selectedService = computed(() =>
    this.services().find((service) => service.id === this.form.controls.serviceId.value) ?? null,
  );

  ngOnInit(): void {
    this.reload();

    this.form.valueChanges
      .pipe(
        startWith(this.form.getRawValue()),
        debounceTime(250),
        switchMap((value) => {
          if (!value.serviceId || !value.fechaSolicitada || !value.horaSolicitada) {
            this.availability.set(null);
            return of(null);
          }

          return this.hotelServiceApi.getServiceAvailability(value.serviceId, value.fechaSolicitada, value.horaSolicitada);
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((availability) => this.availability.set(availability));
  }

  chooseService(serviceId: number): void {
    this.form.patchValue({ serviceId });
  }

  submit(): void {
    const value = this.form.getRawValue();
    if (!value.serviceId || !value.fechaSolicitada || !value.horaSolicitada) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    this.hotelServiceApi
      .requestService({
        serviceId: value.serviceId,
        reservationId: value.reservationId || undefined,
        fechaSolicitada: value.fechaSolicitada,
        horaSolicitada: value.horaSolicitada,
        notas: value.notas,
      })
      .pipe(finalize(() => this.saving.set(false)))
      .subscribe(() => {
        this.toastService.success('Solicitud de servicio creada correctamente.');
        this.form.patchValue({ notas: '' });
        this.reload();
      });
  }

  private reload(): void {
    this.loading.set(true);
    forkJoin({
      services: this.hotelServiceApi.getServices(),
      requests: this.hotelServiceApi.getMyRequests(),
      reservations: this.reservationApi.getMyReservations(),
    })
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe(({ services, requests, reservations }) => {
        this.services.set(services);
        this.requests.set(requests);
        this.reservations.set(reservations);
      });
  }
}
