import { NgClass } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';

@Component({
  selector: 'app-status-badge',
  standalone: true,
  imports: [NgClass],
  template: `<span class="status-badge" [ngClass]="badgeClass()">{{ label() }}</span>`,
  styleUrl: './status-badge.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class StatusBadgeComponent {
  readonly status = input.required<string>();
  readonly label = computed(() => this.status().replaceAll('_', ' '));
  readonly badgeClass = computed(() => `status-${this.status().toLowerCase()}`);
}
