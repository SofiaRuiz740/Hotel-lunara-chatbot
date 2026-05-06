import { ChangeDetectionStrategy, Component, output } from '@angular/core';

@Component({
  selector: 'app-modal-shell',
  standalone: true,
  templateUrl: './modal-shell.component.html',
  styleUrl: './modal-shell.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ModalShellComponent {
  readonly closed = output<void>();
}
