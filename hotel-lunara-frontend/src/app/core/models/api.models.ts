export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

export type UserRole = 'GUEST' | 'RECEPTIONIST' | 'ADMIN';
export type UserLanguage = 'ES' | 'EN' | 'FR';
export type RoomType = 'SIMPLE' | 'DOBLE' | 'SUITE' | 'PENTHOUSE';
export type RoomStatus = 'DISPONIBLE' | 'OCUPADA' | 'MANTENIMIENTO' | 'LIMPIEZA';
export type ReservationStatus = 'PENDIENTE' | 'CONFIRMADA' | 'ACTIVA' | 'COMPLETADA' | 'CANCELADA';
export type RestaurantReservationStatus = 'CONFIRMADA' | 'CANCELADA' | 'COMPLETADA' | 'NO_SHOW';
export type ServiceRequestStatus = 'PENDIENTE' | 'CONFIRMADO' | 'EN_PROCESO' | 'COMPLETADO' | 'CANCELADO';
export type ConciergeActionSuggested = 'BOOK_RESTAURANT' | 'REQUEST_SERVICE' | 'NONE';
export type RestaurantOccasion = 'NINGUNA' | 'CUMPLEANOS' | 'ANIVERSARIO' | 'NEGOCIOS';

export interface UserProfile {
  id: string;
  nombre: string;
  apellido: string;
  email: string;
  telefono?: string | null;
  nacionalidad?: string | null;
  documentoIdentidad?: string | null;
  role: UserRole;
  idioma: UserLanguage;
  alergias?: string | null;
  preferenciasCama?: string | null;
  peticionesEspeciales?: string | null;
  activo: boolean;
  emailVerificado: boolean;
  fechaRegistro: string;
  ultimoLogin?: string | null;
}

export interface Room {
  id: number;
  numero: string;
  piso: number;
  tipo: RoomType;
  capacidadAdultos: number;
  capacidadNinos: number;
  precioPorNoche: number;
  descripcion: string;
  amenities: string;
  estado: RoomStatus;
  imagenes: string;
  activa: boolean;
}

export interface Reservation {
  id: number;
  codigoReserva: string;
  guestId: string;
  guestNombreCompleto: string;
  roomId: number;
  roomNumero: string;
  roomTipo: RoomType;
  checkIn: string;
  checkOut: string;
  cantidadNoches: number;
  cantidadAdultos: number;
  cantidadNinos: number;
  precioNoche: number;
  precioTotal: number;
  estado: ReservationStatus;
  motivoCancelacion?: string | null;
  fechaCancelacion?: string | null;
  peticionesEspeciales?: string | null;
  creadaPor: 'GUEST' | 'RECEPTIONIST';
  fechaCreacion: string;
}

export interface TodayOperationsResponse {
  checkInsHoy: Reservation[];
  checkOutsHoy: Reservation[];
  reservasActivas: Reservation[];
}

export interface CheckoutSummaryResponse {
  reserva: Reservation;
  serviciosConsumidos: ServiceRequest[];
  reservasRestaurante: RestaurantReservation[];
}

export interface HotelPublicInfo {
  nombre: string;
  estrellas: number;
  pisos: number;
  totalHabitaciones: number;
  descripcion: string;
  direccion: string;
  telefono: string;
  email: string;
  horarioCheckIn: string;
  horarioCheckOut: string;
  politicaCancelacion: string;
  imagenes: string[];
  horariosRestaurante: Record<string, string>;
  serviciosDestacados: string[];
  habitacionesPorTipo: Record<string, number>;
}

export interface HotelServiceItem {
  id: number;
  nombre: string;
  descripcion: string;
  categoria: string;
  precio: number;
  duracion: number;
  horarioApertura: string;
  horarioCierre: string;
  requiereReserva: boolean;
  disponibleParaExternos: boolean;
  capacidadMaximaPorSlot: number;
  activo: boolean;
}

export interface ServiceAvailability {
  serviceId: number;
  serviceNombre: string;
  fecha: string;
  hora: string;
  capacidadMaxima: number;
  reservasConfirmadas: number;
  cuposDisponibles: number;
  disponible: boolean;
}

export interface ServiceRequest {
  id: number;
  guestId: string;
  guestNombreCompleto: string;
  reservationId?: number | null;
  serviceId: number;
  serviceNombre: string;
  serviceCategoria: string;
  fechaSolicitada: string;
  horaSolicitada: string;
  estado: ServiceRequestStatus;
  notas?: string | null;
  precioAplicado: number;
  atendidoPorId?: string | null;
  atendidoPorNombre?: string | null;
  creadaEn: string;
}

export interface RestaurantReservation {
  id: number;
  guestId: string;
  guestNombreCompleto: string;
  tableId: number;
  tableNumero: number;
  fecha: string;
  hora: string;
  cantidadPersonas: number;
  ocasionEspecial: RestaurantOccasion;
  peticiones?: string | null;
  estado: RestaurantReservationStatus;
  reservationHotelId?: number | null;
  creadaEn: string;
}

export interface RestaurantAvailability {
  fecha: string;
  hora: string;
  cantidadPersonas: number;
  mesasDisponibles: RestaurantTable[];
}

export interface RestaurantTable {
  id: number;
  numero: number;
  capacidad: number;
  ubicacion: 'INTERIOR' | 'TERRAZA' | 'BAR';
  estado: 'LIBRE' | 'RESERVADA' | 'OCUPADA';
  activa: boolean;
}

export interface ConversationMessage {
  id: string;
  sessionId: string;
  role: 'USER' | 'ASSISTANT';
  contenido: string;
  timestamp: string;
  tokensUsados: number;
  contextoSnapshot?: string | null;
}

export interface ConciergeChatResponse {
  respuesta: string;
  actionSuggested: ConciergeActionSuggested;
  sessionToken: string;
}

export interface DashboardSummary {
  totalHabitaciones: number;
  habitacionesOcupadas: number;
  habitacionesDisponibles: number;
  habitacionesMantenimiento: number;
  reservasHoy: number;
  checkoutsHoy: number;
  reservasActivasTotales: number;
  ingresosMesActual: number;
  totalHuespedesRegistrados: number;
  solicitudesPendientes: number;
}

export interface AuditLog {
  id: number;
  usuarioId?: string | null;
  usuarioEmail?: string | null;
  accion: string;
  entidad: string;
  entidadId: string;
  detalles?: string | null;
  ip?: string | null;
  timestamp: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: UserProfile;
}

export interface JwtClaims {
  userId: string;
  email: string;
  role: UserRole;
  exp: number;
  iat: number;
  type?: string;
}

export interface AuthSession {
  accessToken: string;
  refreshToken: string;
  claims: JwtClaims;
  user: UserProfile;
  storage: 'local' | 'session';
}

export interface RoomAvailabilityFilters {
  checkIn: string;
  checkOut: string;
  adults?: number;
  children?: number;
  type?: RoomType;
}

export interface ReservationFilters {
  status?: ReservationStatus;
  checkInFrom?: string;
  checkInTo?: string;
  guestQuery?: string;
}

export interface RestaurantReservationFilters {
  fecha?: string;
  status?: RestaurantReservationStatus;
  guestQuery?: string;
}

export interface ServiceRequestFilters {
  status?: ServiceRequestStatus;
  fecha?: string;
  guestQuery?: string;
}

export interface LoginPayload {
  email: string;
  password: string;
}

export interface RegisterPayload {
  nombre: string;
  apellido: string;
  email: string;
  password: string;
  telefono: string;
  nacionalidad: string;
  documentoIdentidad: string;
  idioma?: UserLanguage;
  alergias?: string;
  preferenciasCama?: string;
  peticionesEspeciales?: string;
}

export interface RoomReservationPayload {
  roomId: number;
  checkIn: string;
  checkOut: string;
  cantidadAdultos: number;
  cantidadNinos: number;
  peticionesEspeciales?: string;
}

export interface CancelReservationPayload {
  motivo: string;
}

export interface RestaurantReservationPayload {
  fecha: string;
  hora: string;
  cantidadPersonas: number;
  ocasionEspecial: RestaurantOccasion;
  peticiones?: string;
  reservationHotelId?: number;
}

export interface ServiceRequestPayload {
  serviceId: number;
  reservationId?: number;
  fechaSolicitada: string;
  horaSolicitada: string;
  notas?: string;
}

export interface ConciergeRequestPayload {
  mensaje: string;
  sessionToken?: string;
}

export interface RoomAdminPayload {
  numero: string;
  piso: number;
  tipo: RoomType;
  capacidadAdultos: number;
  capacidadNinos: number;
  precioPorNoche: number;
  descripcion: string;
  amenities: string;
  estado: RoomStatus;
  imagenes: string;
  activa: boolean;
}

export interface HotelServiceAdminPayload {
  nombre: string;
  descripcion: string;
  categoria: string;
  precio: number;
  duracion: number;
  horarioApertura: string;
  horarioCierre: string;
  requiereReserva: boolean;
  disponibleParaExternos: boolean;
  capacidadMaximaPorSlot: number;
  activo: boolean;
}

export interface UserStatusPayload {
  activo: boolean;
}

export interface UserRolePayload {
  role: UserRole;
}

export interface UpdateProfilePayload {
  nombre: string;
  apellido: string;
  telefono?: string | null;
  nacionalidad?: string | null;
  documentoIdentidad?: string | null;
  idioma?: UserLanguage | null;
  alergias?: string | null;
  preferenciasCama?: string | null;
  peticionesEspeciales?: string | null;
}
