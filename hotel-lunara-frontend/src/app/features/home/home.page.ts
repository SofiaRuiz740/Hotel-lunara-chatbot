import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { RouterLink } from '@angular/router';
import { finalize, forkJoin } from 'rxjs';
import { HotelPublicInfo, HotelServiceItem, Room } from '../../core/models/api.models';
import { AuthService } from '../../core/services/auth.service';
import { HotelApiService } from '../../core/services/hotel-api.service';
import { HotelServiceApiService } from '../../core/services/hotel-service-api.service';
import { RoomApiService } from '../../core/services/room-api.service';
import {
  currency,
  getHeroWelcomeImage,
  getRestaurantImage,
  getRoomGallery,
  getRoomStatusLabel,
  getRoomTypeLabel,
  pluralize,
  truncateText,
} from '../../core/utils/media.utils';
import { LoadingSkeletonComponent } from '../../shared/components/loading-skeleton/loading-skeleton.component';
import { StatusBadgeComponent } from '../../shared/components/status-badge/status-badge.component';
import { ThreeBadgeKind } from '../../shared/components/three-badge/three-badge.component';

interface Testimonial {
  nombre: string;
  pais: string;
  comentario: string;
  foto: string;
}

@Component({
  selector: 'app-home-page',
  standalone: true,
  imports: [
    RouterLink,
    LoadingSkeletonComponent,
    StatusBadgeComponent,
  ],
  templateUrl: './home.page.html',
  styleUrl: './home.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HomePageComponent implements OnInit {
  private readonly hotelApi = inject(HotelApiService);
  private readonly roomApi = inject(RoomApiService);
  private readonly serviceApi = inject(HotelServiceApiService);
  private readonly sanitizer = inject(DomSanitizer);

  readonly authService = inject(AuthService);

  readonly loading = signal(true);
  readonly hotelInfo = signal<HotelPublicInfo | null>(null);
  readonly rooms = signal<Room[]>([]);
  readonly services = signal<HotelServiceItem[]>([]);

  readonly mapUrl: SafeResourceUrl = this.sanitizer.bypassSecurityTrustResourceUrl(
    'https://www.google.com/maps?q=Av.%20Principal%20123%2C%20Ciudad&output=embed',
  );
  readonly restaurantImage = getRestaurantImage();
  readonly roomSwiperBreakpoints = '{"640":{"slidesPerView":1.35},"900":{"slidesPerView":2.15},"1200":{"slidesPerView":3}}';
  readonly testimonialBreakpoints = '{"768":{"slidesPerView":1.1},"1120":{"slidesPerView":2.15}}';
  readonly getRoomGallery = getRoomGallery;
  readonly getRoomStatusLabel = getRoomStatusLabel;
  readonly getRoomTypeLabel = getRoomTypeLabel;
  readonly currency = currency;
  readonly truncateText = truncateText;

  readonly featuredRooms = computed(() => this.rooms().slice(0, 6));
  readonly showcasedServices = computed(() => this.services().slice(0, 6));
  readonly welcomeImage = computed(() => this.hotelInfo()?.imagenes?.[0] ?? getHeroWelcomeImage());
  readonly bookStayRoute = computed(() => (this.authService.isLoggedIn() ? '/habitaciones' : '/auth/registro'));
  readonly bookTableRoute = computed(() => (this.authService.isLoggedIn() ? '/mi-cuenta/restaurante' : '/auth/login'));
  readonly guestRoute = computed(() => (this.authService.isLoggedIn() ? '/mi-cuenta' : '/auth/login'));

  readonly stats = computed(() => {
    const info = this.hotelInfo();
    return [
      { value: info?.totalHabitaciones ?? 60, label: 'Habitaciones' },
      { value: info?.estrellas ?? 4, label: 'Estrellas' },
      { value: info?.pisos ?? 8, label: 'Pisos' },
      { value: 15, label: 'Anios' },
    ];
  });

  readonly testimonials: Testimonial[] = [
    {
      nombre: 'Elena Rossi',
      pais: 'Italia',
      comentario:
        'La experiencia se sintio personalizada desde el check-in. El concierge IA resolvio la reserva del spa y la cena en segundos.',
      foto: 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=240&q=80',
    },
    {
      nombre: 'Martin Vega',
      pais: 'Argentina',
      comentario:
        'El equipo de recepcion opero con una precision impecable. Habitacion amplia, excelente desayuno y muy buena ubicacion.',
      foto: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=240&q=80',
    },
    {
      nombre: 'Sophia Bennett',
      pais: 'Canada',
      comentario:
        'El hotel combina diseno calido con una operacion moderna. La web y el chat hacen que todo sea mas facil antes y durante la estadia.',
      foto: 'https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=240&q=80',
    },
  ];

  ngOnInit(): void {
    forkJoin({
      hotel: this.hotelApi.getHotelInfo(),
      rooms: this.roomApi.getRooms(),
      services: this.serviceApi.getServices(),
    })
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe(({ hotel, rooms, services }) => {
        this.hotelInfo.set(hotel);
        this.rooms.set(rooms);
        this.services.set(services);
      });
  }

  getServiceKind(service: HotelServiceItem): ThreeBadgeKind {
    const name = service.nombre.toLowerCase();
    if (name.includes('spa') || name.includes('masaje')) {
      return 'spa';
    }
    if (name.includes('tour')) {
      return 'tour';
    }
    if (name.includes('room')) {
      return 'room-service';
    }
    if (name.includes('traslado')) {
      return 'transfer';
    }
    return 'hotel';
  }

  getStatLabel(value: number, label: string): string {
    if (label === 'Estrellas') {
      return `${value} ${label}`;
    }

    return pluralize(value, label.slice(0, -1), label);
  }
}
