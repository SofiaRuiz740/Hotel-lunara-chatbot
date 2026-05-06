import { ChangeDetectionStrategy, Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { debounceTime, finalize, startWith, switchMap } from 'rxjs';
import { Room, RoomType } from '../../core/models/api.models';
import { RoomApiService } from '../../core/services/room-api.service';
import {
  currency,
  getRoomGallery,
  getRoomStatusLabel,
  getRoomTypeLabel,
  splitCommaValues,
  truncateText,
} from '../../core/utils/media.utils';
import { LoadingSkeletonComponent } from '../../shared/components/loading-skeleton/loading-skeleton.component';
import { StatusBadgeComponent } from '../../shared/components/status-badge/status-badge.component';

type RoomsFilterType = RoomType | 'ALL';

@Component({
  selector: 'app-rooms-page',
  standalone: true,
  imports: [
    RouterLink,
    ReactiveFormsModule,
    LoadingSkeletonComponent,
    StatusBadgeComponent,
  ],
  templateUrl: './rooms.page.html',
  styleUrl: './rooms.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RoomsPageComponent implements OnInit {
  private readonly roomApi = inject(RoomApiService);
  private readonly fb = inject(FormBuilder);
  private readonly destroyRef = inject(DestroyRef);

  readonly loading = signal(true);
  readonly rooms = signal<Room[]>([]);
  readonly today = new Date().toISOString().slice(0, 10);
  readonly roomTypes: RoomsFilterType[] = ['ALL', 'SIMPLE', 'DOBLE', 'SUITE', 'PENTHOUSE'];

  readonly filtersForm = this.fb.nonNullable.group({
    checkIn: '',
    checkOut: '',
    adults: 2,
    children: 0,
    type: 'ALL' as RoomsFilterType,
    minPrice: 0,
    maxPrice: 600,
  });

  readonly filterValues = toSignal(this.filtersForm.valueChanges.pipe(startWith(this.filtersForm.getRawValue())), {
    initialValue: this.filtersForm.getRawValue(),
  });
  readonly getRoomGallery = getRoomGallery;
  readonly getRoomTypeLabel = getRoomTypeLabel;
  readonly getRoomStatusLabel = getRoomStatusLabel;
  readonly splitCommaValues = splitCommaValues;
  readonly truncateText = truncateText;
  readonly currency = currency;

  readonly visibleRooms = computed(() => {
    const filters = this.filterValues();
    const minPrice = Number(filters.minPrice ?? 0);
    const maxPrice = Number(filters.maxPrice ?? 600);
    const type = filters.type ?? 'ALL';

    return this.rooms().filter((room) => {
      const matchesType = type === 'ALL' || room.tipo === type;
      const matchesPrice = room.precioPorNoche >= minPrice && room.precioPorNoche <= maxPrice;
      return matchesType && matchesPrice;
    });
  });

  private seededPriceRange = false;

  ngOnInit(): void {
    this.filtersForm.valueChanges
      .pipe(
        startWith(this.filtersForm.getRawValue()),
        debounceTime(250),
        switchMap((filters) => {
          this.loading.set(true);
          const type = filters.type === 'ALL' ? undefined : filters.type;
          const shouldUseAvailability = !!filters.checkIn && !!filters.checkOut;
          const checkIn = filters.checkIn ?? '';
          const checkOut = filters.checkOut ?? '';

          const request$ = shouldUseAvailability
            ? this.roomApi.getAvailability({
                checkIn,
                checkOut,
                adults: filters.adults,
                children: filters.children,
                type,
              })
            : this.roomApi.getRooms();

          return request$.pipe(finalize(() => this.loading.set(false)));
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((rooms) => {
        this.rooms.set(rooms);
        this.seedPriceBounds(rooms);
      });
  }

  selectType(type: RoomsFilterType): void {
    this.filtersForm.patchValue({ type });
  }

  adjustCounter(controlName: 'adults' | 'children', delta: number): void {
    const currentValue = this.filtersForm.controls[controlName].value;
    const nextValue = Math.max(0, currentValue + delta);
    this.filtersForm.controls[controlName].setValue(controlName === 'adults' ? Math.max(1, nextValue) : nextValue);
  }

  getFilterTypeLabel(type: RoomsFilterType): string {
    return type === 'ALL' ? 'Todas' : getRoomTypeLabel(type);
  }

  normalizePriceRange(mode: 'min' | 'max'): void {
    const min = this.filtersForm.controls.minPrice.value;
    const max = this.filtersForm.controls.maxPrice.value;
    if (mode === 'min' && min > max) {
      this.filtersForm.controls.maxPrice.setValue(min);
    }
    if (mode === 'max' && max < min) {
      this.filtersForm.controls.minPrice.setValue(max);
    }
  }

  private seedPriceBounds(rooms: Room[]): void {
    if (!rooms.length) {
      return;
    }

    const min = Math.floor(Math.min(...rooms.map((room) => room.precioPorNoche)));
    const max = Math.ceil(Math.max(...rooms.map((room) => room.precioPorNoche)));

    if (!this.seededPriceRange) {
      this.seededPriceRange = true;
      this.filtersForm.patchValue(
        {
          minPrice: min,
          maxPrice: max,
        },
        { emitEvent: false },
      );
      return;
    }

    if (this.filtersForm.controls.minPrice.value < min || this.filtersForm.controls.maxPrice.value > max) {
      this.filtersForm.patchValue(
        {
          minPrice: Math.max(min, this.filtersForm.controls.minPrice.value),
          maxPrice: Math.min(max, this.filtersForm.controls.maxPrice.value),
        },
        { emitEvent: false },
      );
    }
  }
}
