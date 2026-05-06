import { TitleCasePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';
import { Reservation } from '../../core/models/api.models';
import { ReservationApiService } from '../../core/services/reservation-api.service';
import { ToastService } from '../../core/services/toast.service';
import { currency, formatDate, getRoomTypeLabel } from '../../core/utils/media.utils';
import { LoadingSkeletonComponent } from '../../shared/components/loading-skeleton/loading-skeleton.component';
import { ModalShellComponent } from '../../shared/components/modal-shell/modal-shell.component';
import { StatusBadgeComponent } from '../../shared/components/status-badge/status-badge.component';

type ReservationTab = 'proximas' | 'activas' | 'pasadas' | 'canceladas';

@Component({
  selector: 'app-guest-reservations-page',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    TitleCasePipe,
    LoadingSkeletonComponent,
    ModalShellComponent,
    StatusBadgeComponent,
  ],
  templateUrl: './guest-reservations.page.html',
  styleUrl: './guest-reservations.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GuestReservationsPageComponent implements OnInit {
  private readonly reservationApi = inject(ReservationApiService);
  private readonly fb = inject(FormBuilder);
  private readonly toastService = inject(ToastService);

  readonly loading = signal(true);
  readonly cancelling = signal(false);
  readonly activeTab = signal<ReservationTab>('proximas');
  readonly reservations = signal<Reservation[]>([]);
  readonly reservationToCancel = signal<Reservation | null>(null);
  readonly tabs: ReservationTab[] = ['proximas', 'activas', 'pasadas', 'canceladas'];

  readonly cancelForm = this.fb.nonNullable.group({
    motivo: ['', [Validators.required, Validators.minLength(5)]],
  });

  readonly filteredReservations = computed(() => {
    const tab = this.activeTab();
    switch (tab) {
      case 'activas':
        return this.reservations().filter((reservation) => reservation.estado === 'ACTIVA');
      case 'pasadas':
        return this.reservations().filter((reservation) => reservation.estado === 'COMPLETADA');
      case 'canceladas':
        return this.reservations().filter((reservation) => reservation.estado === 'CANCELADA');
      case 'proximas':
      default:
        return this.reservations().filter((reservation) => reservation.estado === 'CONFIRMADA');
    }
  });

  readonly formatDate = formatDate;
  readonly currency = currency;
  readonly getRoomTypeLabel = getRoomTypeLabel;

  ngOnInit(): void {
    this.reload();
  }

  setTab(tab: ReservationTab): void {
    this.activeTab.set(tab);
  }

  openCancelModal(reservation: Reservation): void {
    this.reservationToCancel.set(reservation);
    this.cancelForm.reset({ motivo: '' });
  }

  closeCancelModal(): void {
    this.reservationToCancel.set(null);
  }

  confirmCancellation(): void {
    const reservation = this.reservationToCancel();
    if (!reservation) {
      return;
    }

    if (this.cancelForm.invalid) {
      this.cancelForm.markAllAsTouched();
      return;
    }

    this.cancelling.set(true);
    this.reservationApi
      .cancelReservation(reservation.id, this.cancelForm.getRawValue())
      .pipe(finalize(() => this.cancelling.set(false)))
      .subscribe(() => {
        this.toastService.success('Reserva cancelada correctamente.');
        this.closeCancelModal();
        this.reload();
      });
  }

  private reload(): void {
    this.loading.set(true);
    this.reservationApi
      .getMyReservations()
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe((reservations) => this.reservations.set(reservations));
  }
}
