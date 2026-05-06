import { animate, group, query, style, transition, trigger } from '@angular/animations';

export const routeFadeSlideAnimation = trigger('routeFadeSlideAnimation', [
  transition('* <=> *', [
    query(
      ':enter, :leave',
      [
        style({
          position: 'absolute',
          inset: 0,
          width: '100%',
        }),
      ],
      { optional: true },
    ),
    group([
      query(
        ':leave',
        [
          animate(
            '220ms ease',
            style({
              opacity: 0,
              transform: 'translateY(18px)',
              filter: 'blur(6px)',
            }),
          ),
        ],
        { optional: true },
      ),
      query(
        ':enter',
        [
          style({
            opacity: 0,
            transform: 'translateY(18px)',
            filter: 'blur(6px)',
          }),
          animate(
            '420ms cubic-bezier(0.2, 0.8, 0.2, 1)',
            style({
              opacity: 1,
              transform: 'translateY(0)',
              filter: 'blur(0)',
            }),
          ),
        ],
        { optional: true },
      ),
    ]),
  ]),
]);
