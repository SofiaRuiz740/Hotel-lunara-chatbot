import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { getRoleDashboardRoute } from '../../../core/utils/media.utils';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './app-header.component.html',
  styleUrl: './app-header.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AppHeaderComponent {
  readonly authService = inject(AuthService);
  readonly menuOpen = signal(false);
  readonly dashboardRoute = computed(() => getRoleDashboardRoute(this.authService.currentRole() ?? undefined));

  toggleMenu(): void {
    this.menuOpen.update((value) => !value);
  }

  closeMenu(): void {
    this.menuOpen.set(false);
  }
}
