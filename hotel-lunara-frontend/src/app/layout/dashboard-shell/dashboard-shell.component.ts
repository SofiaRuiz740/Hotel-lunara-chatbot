import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

interface NavItem {
  label: string;
  route: string;
  icon: SafeHtml;
  exact?: boolean;
}

const SVG = {
  dashboard: `<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/></svg>`,
  concierge: `<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M18 20V10"/><path d="M12 20V4"/><path d="M6 20v-6"/></svg>`,
  reception: `<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 3h6a4 4 0 014 4v14a3 3 0 00-3-3H2z"/><path d="M22 3h-6a4 4 0 00-4 4v14a3 3 0 013-3h7z"/></svg>`,
  account: `<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>`,
  reservas: `<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>`,
  restaurant: `<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 2v7c0 1.1.9 2 2 2h4a2 2 0 002-2V2"/><path d="M7 2v20"/><path d="M21 15V2a5 5 0 00-5 5v6c0 1.1.9 2 2 2h3zm0 0v7"/></svg>`,
  services: `<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="3"/><path d="M19.07 4.93a10 10 0 010 14.14M4.93 4.93a10 10 0 000 14.14"/></svg>`,
  profile: `<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>`,
  back: `<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="15,18 9,12 15,6"/></svg>`,
};

@Component({
  selector: 'app-dashboard-shell',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './dashboard-shell.component.html',
  styleUrl: './dashboard-shell.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DashboardShellComponent {
  readonly authService = inject(AuthService);
  readonly mobileSidebarOpen = signal(false);

  private readonly sanitizer = inject(DomSanitizer);

  readonly roleLabel = computed(() => {
    switch (this.authService.currentRole()) {
      case 'ADMIN':
        return 'Administrador';
      case 'RECEPTIONIST':
        return 'Recepcion';
      case 'GUEST':
      default:
        return 'Huesped';
    }
  });

  readonly shellTitle = computed(() => {
    switch (this.authService.currentRole()) {
      case 'ADMIN':
        return 'Centro de control';
      case 'RECEPTIONIST':
        return 'Operacion de recepcion';
      case 'GUEST':
      default:
        return 'Mi estancia';
    }
  });

  readonly shellSubtitle = computed(() => {
    switch (this.authService.currentRole()) {
      case 'ADMIN':
        return 'Habitaciones, usuarios, servicios y auditoria en un solo lugar.';
      case 'RECEPTIONIST':
        return 'Check-in, check-out, solicitudes activas y seguimiento diario.';
      case 'GUEST':
      default:
        return 'Reservas, restaurante, servicios y perfil personal.';
    }
  });

  readonly backIcon: SafeHtml = this.safe(SVG.back);

  readonly navItems = computed<NavItem[]>(() => {
    const s = this.safe.bind(this);

    switch (this.authService.currentRole()) {
      case 'ADMIN':
        return [
          { label: 'Resumen', route: '/admin', icon: s(SVG.dashboard), exact: true },
          { label: 'Concierge', route: '/concierge', icon: s(SVG.concierge) },
        ];
      case 'RECEPTIONIST':
        return [
          { label: 'Recepcion', route: '/recepcion', icon: s(SVG.reception), exact: true },
          { label: 'Concierge', route: '/concierge', icon: s(SVG.concierge) },
        ];
      case 'GUEST':
      default:
        return [
          { label: 'Resumen', route: '/mi-cuenta', icon: s(SVG.account), exact: true },
          { label: 'Reservas', route: '/mi-cuenta/reservas', icon: s(SVG.reservas) },
          { label: 'Restaurante', route: '/mi-cuenta/restaurante', icon: s(SVG.restaurant) },
          { label: 'Servicios', route: '/mi-cuenta/servicios', icon: s(SVG.services) },
          { label: 'Perfil', route: '/mi-cuenta/perfil', icon: s(SVG.profile) },
          { label: 'Concierge', route: '/concierge', icon: s(SVG.concierge) },
        ];
    }
  });

  private safe(svg: string): SafeHtml {
    return this.sanitizer.bypassSecurityTrustHtml(svg);
  }
}
