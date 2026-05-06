import {
  AfterViewInit,
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  ElementRef,
  NgZone,
  OnDestroy,
  ViewChild,
  computed,
  inject,
  input,
  signal,
} from '@angular/core';
import { gsap } from 'gsap';
import { ScrollTrigger } from 'gsap/ScrollTrigger';

gsap.registerPlugin(ScrollTrigger);

@Component({
  selector: 'app-hero-sequence',
  standalone: true,
  templateUrl: './hero-sequence.component.html',
  styleUrl: './hero-sequence.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HeroSequenceComponent implements AfterViewInit, OnDestroy {
  @ViewChild('canvas', { static: true }) private readonly canvasRef!: ElementRef<HTMLCanvasElement>;
  @ViewChild('host', { static: true }) private readonly hostRef!: ElementRef<HTMLElement>;

  readonly frameCount = input(12);
  readonly fallbackImage = input('assets/images/hero-fallback.svg');

  readonly isReady = signal(false);
  readonly loadProgress = signal(0);
  readonly frameLabel = computed(() => `${Math.round(this.loadProgress() * 100)}%`);

  private readonly zone = inject(NgZone);
  private readonly destroyRef = inject(DestroyRef);

  private readonly images: HTMLImageElement[] = [];
  private observer?: IntersectionObserver;
  private animationFrameId?: number;
  private scrollTrigger?: ScrollTrigger;
  private currentFrame = 0;
  private targetFrame = 0;
  private lastRenderedFrame = -1;
  private isVisible = false;

  ngAfterViewInit(): void {
    this.zone.runOutsideAngular(() => {
      this.prepareFrames();
      this.observeVisibility();
      this.createScrollTrigger();
      this.resizeCanvas();
      window.addEventListener('resize', this.resizeCanvas, { passive: true });
      this.destroyRef.onDestroy(() => window.removeEventListener('resize', this.resizeCanvas));
    });
  }

  ngOnDestroy(): void {
    this.observer?.disconnect();
    this.scrollTrigger?.kill();
    if (this.animationFrameId) {
      cancelAnimationFrame(this.animationFrameId);
    }
  }

  private prepareFrames(): void {
    for (let index = 1; index <= this.frameCount(); index += 1) {
      this.images.push(this.createImage(this.buildFrameUrl(index), index - 1));
    }
  }

  private createImage(src: string, index: number): HTMLImageElement {
    const image = new Image();
    image.decoding = 'async';
    image.loading = index === 0 ? 'eager' : 'lazy';
    image.src = src;
    image.onload = () => {
      const loadedFrames = this.images.filter((item) => item.complete).length;
      this.zone.run(() => {
        this.loadProgress.set(loadedFrames / this.frameCount());
        if (!this.isReady() && loadedFrames >= 1) {
          this.isReady.set(true);
        }
      });

      if (index === 0 || Math.abs(this.currentFrame - index) <= 1) {
        this.renderFrame(Math.round(this.currentFrame));
      }
    };
    image.onerror = () => {
      if (index === 0) {
        const fallback = new Image();
        fallback.src = this.fallbackImage();
        fallback.onload = () => {
          this.images[0] = fallback;
          this.zone.run(() => this.isReady.set(true));
          this.renderFrame(Math.round(this.currentFrame));
        };
      }
    };
    return image;
  }

  private buildFrameUrl(frameNumber: number): string {
    return `assets/hero-sequence/frame_${frameNumber.toString().padStart(4, '0')}.svg`;
  }

  private observeVisibility(): void {
    this.observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          this.isVisible = entry.isIntersecting;
          if (entry.isIntersecting) {
            this.startRenderLoop();
          }
        });
      },
      { threshold: 0.2 },
    );

    this.observer.observe(this.hostRef.nativeElement);
  }

  private createScrollTrigger(): void {
    this.scrollTrigger = ScrollTrigger.create({
      trigger: this.hostRef.nativeElement,
      start: 'top top',
      end: 'bottom top',
      scrub: 0.7,
      onUpdate: (self) => {
        this.targetFrame = self.progress * (this.frameCount() - 1);
        this.startRenderLoop();
      },
    });
  }

  private startRenderLoop(): void {
    if (this.animationFrameId || !this.isVisible) {
      return;
    }

    const animateFrame = () => {
      this.animationFrameId = undefined;
      if (!this.isVisible) {
        return;
      }

      this.currentFrame += (this.targetFrame - this.currentFrame) * 0.12;
      this.renderFrame(Math.round(this.currentFrame));

      if (Math.abs(this.targetFrame - this.currentFrame) > 0.01) {
        this.animationFrameId = requestAnimationFrame(animateFrame);
      }
    };

    this.animationFrameId = requestAnimationFrame(animateFrame);
  }

  private renderFrame(frameIndex: number): void {
    const canvas = this.canvasRef.nativeElement;
    const context = canvas.getContext('2d');
    const image = this.images[frameIndex];

    if (!context || !image?.complete || this.lastRenderedFrame === frameIndex) {
      return;
    }

    this.lastRenderedFrame = frameIndex;
    context.clearRect(0, 0, canvas.width, canvas.height);
    context.drawImage(image, 0, 0, canvas.width, canvas.height);
  }

  private resizeCanvas = (): void => {
    const canvas = this.canvasRef.nativeElement;
    const host = this.hostRef.nativeElement;
    const width = Math.max(1, Math.floor(host.clientWidth));
    const height = Math.max(1, Math.floor(host.clientHeight));
    const ratio = window.devicePixelRatio || 1;

    canvas.width = width * ratio;
    canvas.height = height * ratio;
    canvas.style.width = `${width}px`;
    canvas.style.height = `${height}px`;

    const context = canvas.getContext('2d');
    if (context) {
      context.setTransform(ratio, 0, 0, ratio, 0, 0);
    }

    this.lastRenderedFrame = -1;
    this.renderFrame(Math.round(this.currentFrame));
  };
}
