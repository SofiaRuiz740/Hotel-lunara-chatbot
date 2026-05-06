import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';

@Component({
  selector: 'app-loading-skeleton',
  standalone: true,
  templateUrl: './loading-skeleton.component.html',
  styleUrl: './loading-skeleton.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoadingSkeletonComponent {
  readonly count = input(3);
  readonly cardHeight = input('18rem');
  readonly items = computed(() => Array.from({ length: this.count() }));
}
