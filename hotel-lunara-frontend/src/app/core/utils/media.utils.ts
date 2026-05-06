import { Room, RoomStatus, RoomType, UserRole } from '../models/api.models';

const roomImageCatalog: Record<RoomType, string[]> = {
  SIMPLE: [
    'https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?auto=format&fit=crop&w=1200&q=80',
    'https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?auto=format&fit=crop&w=1200&q=80',
  ],
  DOBLE: [
    'https://images.unsplash.com/photo-1512918728675-ed5a9ecdebfd?auto=format&fit=crop&w=1200&q=80',
    'https://images.unsplash.com/photo-1505692952047-1a78307da8f2?auto=format&fit=crop&w=1200&q=80',
  ],
  SUITE: [
    'https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?auto=format&fit=crop&w=1200&q=80',
    'https://images.unsplash.com/photo-1566665797739-1674de7a421a?auto=format&fit=crop&w=1200&q=80',
  ],
  PENTHOUSE: [
    'https://images.unsplash.com/photo-1445019980597-93fa8acb246c?auto=format&fit=crop&w=1200&q=80',
    'https://images.unsplash.com/photo-1496417263034-38ec4f0b665a?auto=format&fit=crop&w=1200&q=80',
  ],
};

export function getRoomGallery(room: Room): string[] {
  const rawImages = room.imagenes
    ?.split(',')
    .map((image) => image.trim())
    .filter((image) => image && !image.includes('example.com'));

  if (rawImages?.length) {
    return rawImages;
  }

  return roomImageCatalog[room.tipo] ?? roomImageCatalog.SUITE;
}

export function getHeroWelcomeImage(): string {
  return 'https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=1400&q=80';
}

export function getRestaurantImage(): string {
  return 'https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?auto=format&fit=crop&w=1600&q=80';
}

export function getAuthImage(): string {
  return 'https://images.unsplash.com/photo-1519167758481-83f29c6d1f3c?auto=format&fit=crop&w=1400&q=80';
}

export function getRoleDashboardRoute(role: UserRole | undefined): string {
  switch (role) {
    case 'ADMIN':
      return '/admin';
    case 'RECEPTIONIST':
      return '/recepcion';
    case 'GUEST':
    default:
      return '/mi-cuenta';
  }
}

export function splitCommaValues(value?: string | null): string[] {
  return (value ?? '')
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean);
}

export function getRoomTypeLabel(type: RoomType): string {
  switch (type) {
    case 'SIMPLE':
      return 'Simple';
    case 'DOBLE':
      return 'Doble';
    case 'SUITE':
      return 'Suite';
    case 'PENTHOUSE':
      return 'Penthouse';
    default:
      return type;
  }
}

export function getRoomStatusLabel(status: RoomStatus): string {
  switch (status) {
    case 'DISPONIBLE':
      return 'Disponible';
    case 'OCUPADA':
      return 'Ocupada';
    case 'MANTENIMIENTO':
      return 'Mantenimiento';
    case 'LIMPIEZA':
      return 'Limpieza';
    default:
      return status;
  }
}

export function currency(value: number | string): string {
  return new Intl.NumberFormat('es-CO', {
    style: 'currency',
    currency: 'USD',
    maximumFractionDigits: 0,
  }).format(Number(value) || 0);
}

export function formatDate(dateIso: string, options?: Intl.DateTimeFormatOptions): string {
  return new Intl.DateTimeFormat('es-CO', {
    dateStyle: 'medium',
    ...options,
  }).format(new Date(dateIso));
}

export function formatDateTime(dateIso: string): string {
  return new Intl.DateTimeFormat('es-CO', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(dateIso));
}

export function formatTime(time: string): string {
  return time.slice(0, 5);
}

export function pluralize(value: number, singular: string, plural = `${singular}s`): string {
  return `${value} ${value === 1 ? singular : plural}`;
}

export function daysBetween(start: string, end: string): number {
  const startDate = new Date(start);
  const endDate = new Date(end);
  const difference = endDate.getTime() - startDate.getTime();
  return Math.max(0, Math.round(difference / 86400000));
}

export function truncateText(value: string, length = 120): string {
  if (value.length <= length) {
    return value;
  }

  return `${value.slice(0, Math.max(0, length - 1)).trim()}...`;
}
