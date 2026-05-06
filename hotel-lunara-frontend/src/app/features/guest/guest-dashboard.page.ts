import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { finalize, forkJoin } from 'rxjs';
import { Reservation, RestaurantReservation, ServiceRequest, UserProfile } from '../../core/models/api.models';
import { HotelServiceApiService } from '../../core/services/hotel-service-api.service';
import { ReservationApiService } from '../../core/services/reservation-api.service';
import { RestaurantApiService } from '../../core/services/restaurant-api.service';
import { UserApiService } from '../../core/services/user-api.service';
import { currency, formatDate, formatTime, getRoomTypeLabel } from '../../core/utils/media.utils';
import { LoadingSkeletonComponent } from '../../shared/components/loading-skeleton/loading-skeleton.component';
import { StatusBadgeComponent } from '../../shared/components/status-badge/status-badge.component';
import { ThreeBadgeKind } from '../../shared/components/three-badge/three-badge.component';

interface QuickAction {
  label: string;
  route: string;
  kind: ThreeBadgeKind;
}

@Component({
  selector: 'app-guest-dashboard-page',
  standalone: true,
  imports: [
    RouterLink,
    LoadingSkeletonComponent,
    StatusBadgeComponent,
  ],
  templateUrl: './guest-dashboard.page.html',
  styleUrl: './guest-dashboard.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GuestDashboardPageComponent implements OnInit {
  private readonly userApi = inject(UserApiService);
  private readonly reservationApi = inject(ReservationApiService);
  private readonly serviceApi = inject(HotelServiceApiService);
  private readonly restaurantApi = inject(RestaurantApiService);

  readonly loading = signal(true);
  readonly profile = signal<UserProfile | null>(null);
  readonly reservations = signal<Reservation[]>([]);
  readonly serviceRequests = signal<ServiceRequest[]>([]);
  readonly restaurantReservations = signal<RestaurantReservation[]>([]);

  readonly activeReservation = computed(() =>
    this.reservations().find((reservation) => reservation.estado === 'ACTIVA') ??
    this.reservations().find((reservation) => reservation.estado === 'CONFIRMADA'),
  );
  readonly pendingServices = computed(() =>
    this.serviceRequests().filter((request) => ['PENDIENTE', 'CONFIRMADO', 'EN_PROCESO'].includes(request.estado)),
  );
  readonly nextCheckIn = computed(() =>
    [...this.reservations()]
      .filter((reservation) => reservation.estado === 'CONFIRMADA')
      .sort((left, right) => left.checkIn.localeCompare(right.checkIn))[0] ?? null,
  );
  readonly upcomingRestaurant = computed(() =>
    [...this.restaurantReservations()]
      .filter((reservation) => reservation.estado === 'CONFIRMADA')
      .sort((left, right) => `${left.fecha}${left.hora}`.localeCompare(`${right.fecha}${right.hora}`))
      .slice(0, 3),
  );

  readonly quickActions: QuickAction[] = [
    { label: 'Mis reservas', route: '/mi-cuenta/reservas', kind: 'hotel' },
    { label: 'Reservar mesa', route: '/mi-cuenta/restaurante', kind: 'restaurant' },
    { label: 'Solicitar servicio', route: '/mi-cuenta/servicios', kind: 'spa' },
    { label: 'Hablar con IA', route: '/concierge', kind: 'robot' },
    { label: 'Editar perfil', route: '/mi-cuenta/perfil', kind: 'stars' },
  ];

  readonly formatDate = formatDate;
  readonly formatTime = formatTime;
  readonly currency = currency;
  readonly getRoomTypeLabel = getRoomTypeLabel;

  ngOnInit(): void {
    this.reload();
  }

  private reload(): void {
    forkJoin({
      profile: this.userApi.getMyProfile(),
      reservations: this.reservationApi.getMyReservations(),
      requests: this.serviceApi.getMyRequests(),
      restaurant: this.restaurantApi.getMyReservations(),
    })
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe(({ profile, reservations, requests, restaurant }) => {
        this.profile.set(profile);
        this.reservations.set(reservations);
        this.serviceRequests.set(requests);
        this.restaurantReservations.set(restaurant);
      });
  }
}
