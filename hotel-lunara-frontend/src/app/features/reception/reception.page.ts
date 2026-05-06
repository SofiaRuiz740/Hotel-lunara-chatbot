import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { finalize, forkJoin } from 'rxjs';
import {
  Reservation,
  RestaurantReservation,
  Room,
  ServiceRequest,
  TodayOperationsResponse,
} from '../../core/models/api.models';
import { HotelServiceApiService } from '../../core/services/hotel-service-api.service';
import { ReservationApiService } from '../../core/services/reservation-api.service';
import { RestaurantApiService } from '../../core/services/restaurant-api.service';
import { RoomApiService } from '../../core/services/room-api.service';
import { ToastService } from '../../core/services/toast.service';
import { formatDate, formatTime } from '../../core/utils/media.utils';
import { LoadingSkeletonComponent } from '../../shared/components/loading-skeleton/loading-skeleton.component';
import { StatusBadgeComponent } from '../../shared/components/status-badge/status-badge.component';

@Component({
  selector: 'app-reception-page',
  standalone: true,
  imports: [ReactiveFormsModule, LoadingSkeletonComponent, StatusBadgeComponent],
  templateUrl: './reception.page.html',
  styleUrl: './reception.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ReceptionPageComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly reservationApi = inject(ReservationApiService);
  private readonly serviceApi = inject(HotelServiceApiService);
  private readonly restaurantApi = inject(RestaurantApiService);
  private readonly roomApi = inject(RoomApiService);
  private readonly toastService = inject(ToastService);

  readonly loading = signal(true);
  readonly todayOperations = signal<TodayOperationsResponse | null>(null);
  readonly reservations = signal<Reservation[]>([]);
  readonly serviceRequests = signal<ServiceRequest[]>([]);
  readonly restaurantReservations = signal<RestaurantReservation[]>([]);
  readonly rooms = signal<Room[]>([]);

  readonly filtersForm = this.fb.nonNullable.group({
    reservationQuery: '',
    reservationStatus: 'ALL',
    reservationDate: '',
    serviceQuery: '',
    restaurantQuery: '',
  });

  readonly occupancyRate = computed(() => {
    const rooms = this.rooms();
    if (!rooms.length) {
      return 0;
    }
    const occupied = rooms.filter((room) => room.estado === 'OCUPADA').length;
    return Math.round((occupied / rooms.length) * 100);
  });
  readonly pendingCheckins = computed(
    () => this.todayOperations()?.checkInsHoy.filter((reservation) => reservation.estado === 'CONFIRMADA') ?? [],
  );
  readonly checkoutCandidates = computed(() => this.todayOperations()?.checkOutsHoy ?? []);
  readonly pendingServiceRequests = computed(() =>
    this.serviceRequests().filter((request) => ['PENDIENTE', 'CONFIRMADO', 'EN_PROCESO'].includes(request.estado)),
  );
  readonly filteredReservations = computed(() => {
    const filters = this.filtersForm.getRawValue();
    return this.reservations().filter((reservation) => {
      const matchesStatus = filters.reservationStatus === 'ALL' || reservation.estado === filters.reservationStatus;
      const matchesDate = !filters.reservationDate || reservation.checkIn === filters.reservationDate;
      const query = filters.reservationQuery.toLowerCase();
      const matchesQuery =
        !query ||
        reservation.codigoReserva.toLowerCase().includes(query) ||
        reservation.guestNombreCompleto.toLowerCase().includes(query);
      return matchesStatus && matchesDate && matchesQuery;
    });
  });
  readonly filteredServiceRequests = computed(() => {
    const query = this.filtersForm.controls.serviceQuery.value.toLowerCase();
    return this.pendingServiceRequests().filter(
      (request) => !query || request.guestNombreCompleto.toLowerCase().includes(query) || request.serviceNombre.toLowerCase().includes(query),
    );
  });
  readonly filteredRestaurantReservations = computed(() => {
    const query = this.filtersForm.controls.restaurantQuery.value.toLowerCase();
    return this.restaurantReservations().filter(
      (reservation) => !query || reservation.guestNombreCompleto.toLowerCase().includes(query),
    );
  });

  readonly formatDate = formatDate;
  readonly formatTime = formatTime;

  ngOnInit(): void {
    this.reload();
  }

  runCheckin(reservationId: number): void {
    this.reservationApi.checkin(reservationId).subscribe(() => {
      this.toastService.success('Check-in realizado correctamente.');
      this.reload();
    });
  }

  runCheckout(reservationId: number): void {
    this.reservationApi.checkout(reservationId).subscribe(() => {
      this.toastService.success('Check-out realizado correctamente.');
      this.reload();
    });
  }

  updateServiceStatus(requestId: number, estado: 'CONFIRMADO' | 'EN_PROCESO' | 'COMPLETADO' | 'CANCELADO'): void {
    this.serviceApi.updateRequestStatus(requestId, estado).subscribe(() => {
      this.toastService.success('Estado de servicio actualizado.');
      this.reload();
    });
  }

  updateRestaurantStatus(id: number, estado: 'COMPLETADA' | 'NO_SHOW'): void {
    this.restaurantApi.updateReservationStatus(id, estado).subscribe(() => {
      this.toastService.success('Estado de restaurante actualizado.');
      this.reload();
    });
  }

  updateRoomStatus(roomId: number, status: 'LIMPIEZA' | 'MANTENIMIENTO'): void {
    this.roomApi.changeRoomStatus(roomId, status).subscribe(() => {
      this.toastService.success('Estado de habitacion actualizado.');
      this.reload();
    });
  }

  private reload(): void {
    this.loading.set(true);
    forkJoin({
      todayOps: this.reservationApi.getTodayOperations(),
      reservations: this.reservationApi.getAllReservations(),
      serviceRequests: this.serviceApi.getAllRequests(),
      restaurantReservations: this.restaurantApi.getAllReservations(),
      rooms: this.roomApi.getRooms(),
    })
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe(({ todayOps, reservations, serviceRequests, restaurantReservations, rooms }) => {
        this.todayOperations.set(todayOps);
        this.reservations.set(reservations);
        this.serviceRequests.set(serviceRequests);
        this.restaurantReservations.set(restaurantReservations);
        this.rooms.set(rooms);
      });
  }
}
