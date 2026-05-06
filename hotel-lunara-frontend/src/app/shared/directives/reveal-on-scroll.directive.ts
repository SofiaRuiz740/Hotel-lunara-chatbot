import { AfterViewInit, Directive, ElementRef, OnDestroy, Renderer2, inject } from '@angular/core';

@Directive({
  selector: '[appRevealOnScroll]',
})
export class RevealOnScrollDirective implements AfterViewInit, OnDestroy {
  private readonly elementRef = inject(ElementRef<HTMLElement>);
  private readonly renderer = inject(Renderer2);
  private observer?: IntersectionObserver;

  ngAfterViewInit(): void {
    this.renderer.addClass(this.elementRef.nativeElement, 'reveal-ready');

    this.observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            this.renderer.addClass(this.elementRef.nativeElement, 'revealed');
          }
        });
      },
      { threshold: 0.2 },
    );

    this.observer.observe(this.elementRef.nativeElement);
  }

  ngOnDestroy(): void {
    this.observer?.disconnect();
  }
}
