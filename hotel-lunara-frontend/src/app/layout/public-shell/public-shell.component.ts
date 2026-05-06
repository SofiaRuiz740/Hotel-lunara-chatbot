import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { AppFooterComponent } from '../../shared/components/app-footer/app-footer.component';
import { AppHeaderComponent } from '../../shared/components/app-header/app-header.component';

@Component({
  selector: 'app-public-shell',
  standalone: true,
  imports: [RouterOutlet, AppHeaderComponent, AppFooterComponent],
  templateUrl: './public-shell.component.html',
  styleUrl: './public-shell.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PublicShellComponent {}
