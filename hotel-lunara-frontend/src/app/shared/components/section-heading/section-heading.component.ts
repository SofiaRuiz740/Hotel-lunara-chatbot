import { ChangeDetectionStrategy, Component, input } from '@angular/core';

@Component({
  selector: 'app-section-heading',
  standalone: true,
  imports: [],
  templateUrl: './section-heading.component.html',
  styleUrl: './section-heading.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SectionHeadingComponent {
  readonly eyebrow = input('');
  readonly title = input.required<string>();
  readonly subtitle = input('');
  readonly centered = input(false);
}
