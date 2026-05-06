import { TitleCasePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize, forkJoin } from 'rxjs';
import {
  AuditLog,
  DashboardSummary,
  HotelServiceAdminPayload,
  HotelServiceItem,
  RegisterPayload,
  Room,
  RoomAdminPayload,
  RoomStatus,
  RoomType,
  UserProfile,
  UserRole,
} from '../../core/models/api.models';
import { AdminApiService } from '../../core/services/admin-api.service';
import { HotelServiceApiService } from '../../core/services/hotel-service-api.service';
import { RoomApiService } from '../../core/services/room-api.service';
import { ToastService } from '../../core/services/toast.service';
import { currency, formatDateTime, getRoomTypeLabel } from '../../core/utils/media.utils';
import { LoadingSkeletonComponent } from '../../shared/components/loading-skeleton/loading-skeleton.component';
import { ModalShellComponent } from '../../shared/components/modal-shell/modal-shell.component';
import { StatusBadgeComponent } from '../../shared/components/status-badge/status-badge.component';

type AdminSection = 'overview' | 'rooms' | 'users' | 'services' | 'audit';

@Component({
  selector: 'app-admin-page',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    TitleCasePipe,
    LoadingSkeletonComponent,
    ModalShellComponent,
    StatusBadgeComponent,
  ],
  templateUrl: './admin.page.html',
  styleUrl: './admin.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminPageComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly adminApi = inject(AdminApiService);
  private readonly roomApi = inject(RoomApiService);
  private readonly serviceApi = inject(HotelServiceApiService);
  private readonly toastService = inject(ToastService);

  readonly loading = signal(true);
  readonly activeSection = signal<AdminSection>('overview');
  readonly sections: AdminSection[] = ['overview', 'rooms', 'users', 'services', 'audit'];

  readonly dashboard = signal<DashboardSummary | null>(null);
  readonly rooms = signal<Room[]>([]);
  readonly users = signal<UserProfile[]>([]);
  readonly services = signal<HotelServiceItem[]>([]);
  readonly auditLogs = signal<AuditLog[]>([]);

  readonly selectedRoom = signal<Room | null>(null);
  readonly selectedService = signal<HotelServiceItem | null>(null);
  readonly roomModalOpen = signal(false);
  readonly serviceModalOpen = signal(false);
  readonly receptionistModalOpen = signal(false);

  readonly filterForm = this.fb.nonNullable.group({
    userQuery: '',
    auditQuery: '',
  });

  readonly roomForm = this.fb.nonNullable.group({
    numero: ['', Validators.required],
    piso: [1, Validators.required],
    tipo: 'SIMPLE' as RoomType,
    capacidadAdultos: [2, Validators.required],
    capacidadNinos: [0, Validators.required],
    precioPorNoche: [100, Validators.required],
    descripcion: ['', Validators.required],
    amenities: ['WiFi, AC, TV', Validators.required],
    estado: 'DISPONIBLE' as RoomStatus,
    imagenes: '',
    activa: true,
  });

  readonly serviceForm = this.fb.nonNullable.group({
    nombre: ['', Validators.required],
    descripcion: ['', Validators.required],
    categoria: ['', Validators.required],
    precio: [0, Validators.required],
    duracion: [60, Validators.required],
    horarioApertura: ['09:00', Validators.required],
    horarioCierre: ['18:00', Validators.required],
    requiereReserva: false,
    disponibleParaExternos: false,
    capacidadMaximaPorSlot: [1, Validators.required],
    activo: true,
  });

  readonly receptionistForm = this.fb.nonNullable.group({
    nombre: ['', Validators.required],
    apellido: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]],
    telefono: ['', Validators.required],
    nacionalidad: ['', Validators.required],
    documentoIdentidad: ['', Validators.required],
  });

  readonly filteredUsers = computed(() => {
    const query = this.filterForm.controls.userQuery.value.toLowerCase();
    return this.users().filter(
      (user) =>
        !query ||
        user.nombre.toLowerCase().includes(query) ||
        user.apellido.toLowerCase().includes(query) ||
        user.email.toLowerCase().includes(query),
    );
  });
  readonly filteredAuditLogs = computed(() => {
    const query = this.filterForm.controls.auditQuery.value.toLowerCase();
    return this.auditLogs().filter(
      (log) =>
        !query ||
        log.accion.toLowerCase().includes(query) ||
        log.entidad.toLowerCase().includes(query) ||
        (log.usuarioEmail ?? '').toLowerCase().includes(query),
    );
  });

  readonly currency = currency;
  readonly formatDateTime = formatDateTime;
  readonly getRoomTypeLabel = getRoomTypeLabel;

  ngOnInit(): void {
    this.reload();
  }

  setSection(section: AdminSection): void {
    this.activeSection.set(section);
  }

  openRoomModal(room?: Room): void {
    this.selectedRoom.set(room ?? null);
    if (room) {
      this.roomForm.reset({
        numero: room.numero,
        piso: room.piso,
        tipo: room.tipo,
        capacidadAdultos: room.capacidadAdultos,
        capacidadNinos: room.capacidadNinos,
        precioPorNoche: room.precioPorNoche,
        descripcion: room.descripcion,
        amenities: room.amenities,
        estado: room.estado,
        imagenes: room.imagenes,
        activa: room.activa,
      });
    } else {
      this.roomForm.reset({
        numero: '',
        piso: 1,
        tipo: 'SIMPLE',
        capacidadAdultos: 2,
        capacidadNinos: 0,
        precioPorNoche: 100,
        descripcion: '',
        amenities: 'WiFi, AC, TV',
        estado: 'DISPONIBLE',
        imagenes: '',
        activa: true,
      });
    }
    this.roomModalOpen.set(true);
  }

  saveRoom(): void {
    const payload = this.roomForm.getRawValue() as RoomAdminPayload;
    const room = this.selectedRoom();

    const request$ = room ? this.roomApi.updateRoom(room.id, payload) : this.roomApi.createRoom(payload);
    request$.subscribe(() => {
      this.toastService.success(`Habitacion ${room ? 'actualizada' : 'creada'} correctamente.`);
      this.roomModalOpen.set(false);
      this.reload();
    });
  }

  deactivateRoom(roomId: number): void {
    this.roomApi.deactivateRoom(roomId).subscribe(() => {
      this.toastService.success('Habitacion desactivada.');
      this.reload();
    });
  }

  openServiceModal(service?: HotelServiceItem): void {
    this.selectedService.set(service ?? null);
    if (service) {
      this.serviceForm.reset({
        nombre: service.nombre,
        descripcion: service.descripcion,
        categoria: service.categoria,
        precio: service.precio,
        duracion: service.duracion,
        horarioApertura: service.horarioApertura.slice(0, 5),
        horarioCierre: service.horarioCierre.slice(0, 5),
        requiereReserva: service.requiereReserva,
        disponibleParaExternos: service.disponibleParaExternos,
        capacidadMaximaPorSlot: service.capacidadMaximaPorSlot,
        activo: service.activo,
      });
    } else {
      this.serviceForm.reset({
        nombre: '',
        descripcion: '',
        categoria: '',
        precio: 0,
        duracion: 60,
        horarioApertura: '09:00',
        horarioCierre: '18:00',
        requiereReserva: false,
        disponibleParaExternos: false,
        capacidadMaximaPorSlot: 1,
        activo: true,
      });
    }
    this.serviceModalOpen.set(true);
  }

  saveService(): void {
    const payload = this.serviceForm.getRawValue() as HotelServiceAdminPayload;
    const service = this.selectedService();
    const request$ = service ? this.serviceApi.updateService(service.id, payload) : this.serviceApi.createService(payload);
    request$.subscribe(() => {
      this.toastService.success(`Servicio ${service ? 'actualizado' : 'creado'} correctamente.`);
      this.serviceModalOpen.set(false);
      this.reload();
    });
  }

  toggleService(service: HotelServiceItem): void {
    this.serviceApi.changeServiceStatus(service.id, !service.activo).subscribe(() => {
      this.toastService.success('Estado del servicio actualizado.');
      this.reload();
    });
  }

  changeUserRole(userId: string, role: UserRole): void {
    this.adminApi.changeUserRole(userId, role).subscribe(() => {
      this.toastService.success('Rol de usuario actualizado.');
      this.reload();
    });
  }

  changeUserStatus(userId: string, activo: boolean): void {
    this.adminApi.changeUserStatus(userId, activo).subscribe(() => {
      this.toastService.success('Estado de usuario actualizado.');
      this.reload();
    });
  }

  createReceptionist(): void {
    if (this.receptionistForm.invalid) {
      this.receptionistForm.markAllAsTouched();
      return;
    }

    const payload: RegisterPayload = this.receptionistForm.getRawValue();
    this.adminApi.createReceptionist(payload).subscribe(() => {
      this.toastService.success('Cuenta de recepcion creada correctamente.');
      this.receptionistModalOpen.set(false);
      this.receptionistForm.reset({
        nombre: '',
        apellido: '',
        email: '',
        password: '',
        telefono: '',
        nacionalidad: '',
        documentoIdentidad: '',
      });
      this.reload();
    });
  }

  private reload(): void {
    this.loading.set(true);
    forkJoin({
      dashboard: this.adminApi.getDashboard(),
      rooms: this.roomApi.getRooms(),
      users: this.adminApi.getUsers(),
      services: this.serviceApi.getServices(),
      auditLogs: this.adminApi.getAuditLogs(),
    })
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe(({ dashboard, rooms, users, services, auditLogs }) => {
        this.dashboard.set(dashboard);
        this.rooms.set(rooms);
        this.users.set(users);
        this.services.set(services);
        this.auditLogs.set(auditLogs);
      });
  }
}
