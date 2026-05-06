import {
  AfterViewInit,
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  NgZone,
  OnDestroy,
  ViewChild,
  inject,
  input,
} from '@angular/core';
import * as THREE from 'three';

export type ThreeBadgeKind =
  | 'hotel'
  | 'stars'
  | 'restaurant'
  | 'spa'
  | 'tour'
  | 'robot'
  | 'room-service'
  | 'transfer';

@Component({
  selector: 'app-three-badge',
  standalone: true,
  templateUrl: './three-badge.component.html',
  styleUrl: './three-badge.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ThreeBadgeComponent implements AfterViewInit, OnDestroy {
  @ViewChild('canvas', { static: true }) private readonly canvasRef!: ElementRef<HTMLCanvasElement>;

  readonly kind = input<ThreeBadgeKind>('hotel');
  readonly label = input('');

  private readonly zone = inject(NgZone);

  private renderer?: THREE.WebGLRenderer;
  private scene?: THREE.Scene;
  private camera?: THREE.PerspectiveCamera;
  private mesh?: THREE.Mesh;
  private frameId?: number;
  private observer?: IntersectionObserver;
  private isVisible = true;

  ngAfterViewInit(): void {
    this.zone.runOutsideAngular(() => {
      this.buildScene();
      this.observeVisibility();
      this.animate();
      window.addEventListener('resize', this.handleResize, { passive: true });
    });
  }

  ngOnDestroy(): void {
    this.observer?.disconnect();
    if (this.frameId) {
      cancelAnimationFrame(this.frameId);
    }
    window.removeEventListener('resize', this.handleResize);
    this.renderer?.dispose();
    this.mesh?.geometry.dispose();
    if (Array.isArray(this.mesh?.material)) {
      this.mesh.material.forEach((material) => material.dispose());
    } else {
      this.mesh?.material.dispose();
    }
  }

  private buildScene(): void {
    const canvas = this.canvasRef.nativeElement;
    const width = canvas.clientWidth || 120;
    const height = canvas.clientHeight || 120;

    this.scene = new THREE.Scene();
    this.camera = new THREE.PerspectiveCamera(38, width / height, 0.1, 100);
    this.camera.position.set(0, 0, 6);

    this.renderer = new THREE.WebGLRenderer({
      canvas,
      alpha: true,
      antialias: true,
    });
    this.renderer.setPixelRatio(Math.min(window.devicePixelRatio || 1, 2));
    this.renderer.setSize(width, height, false);

    const ambient = new THREE.AmbientLight(0xf9e9c5, 1.1);
    const point = new THREE.PointLight(0xd4793a, 12, 20);
    point.position.set(3.2, 2.6, 5.4);
    this.scene.add(ambient, point);

    const geometry = this.createGeometry();
    const material = new THREE.MeshPhysicalMaterial({
      color: 0xc9a96e,
      emissive: 0x5c3d2e,
      roughness: 0.35,
      metalness: 0.58,
      clearcoat: 0.5,
      transparent: true,
      opacity: 0.98,
    });

    this.mesh = new THREE.Mesh(geometry, material);
    this.scene.add(this.mesh);
  }

  private createGeometry(): THREE.BufferGeometry {
    switch (this.kind()) {
      case 'stars':
        return new THREE.IcosahedronGeometry(1.42, 0);
      case 'restaurant':
        return new THREE.TorusKnotGeometry(0.92, 0.28, 120, 16, 2, 3);
      case 'spa':
        return new THREE.TorusGeometry(1.05, 0.28, 30, 100);
      case 'tour':
        return new THREE.OctahedronGeometry(1.28, 0);
      case 'robot':
        return new THREE.BoxGeometry(1.7, 1.45, 1.45, 3, 3, 3);
      case 'room-service':
        return new THREE.CylinderGeometry(0.85, 1.2, 1.7, 6, 1);
      case 'transfer':
        return new THREE.CapsuleGeometry(0.7, 1.7, 6, 12);
      case 'hotel':
      default:
        return new THREE.DodecahedronGeometry(1.35, 0);
    }
  }

  private observeVisibility(): void {
    this.observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          this.isVisible = entry.isIntersecting;
          if (this.isVisible) {
            this.animate();
          }
        });
      },
      { threshold: 0.2 },
    );
    this.observer.observe(this.canvasRef.nativeElement);
  }

  private animate = (): void => {
    if (!this.renderer || !this.scene || !this.camera || !this.mesh || !this.isVisible) {
      return;
    }

    this.frameId = requestAnimationFrame(this.animate);
    this.mesh.rotation.x += 0.005;
    this.mesh.rotation.y += 0.007;
    this.mesh.position.y = Math.sin(Date.now() / 650) * 0.08;
    this.renderer.render(this.scene, this.camera);
  };

  private handleResize = (): void => {
    const canvas = this.canvasRef.nativeElement;
    if (!this.renderer || !this.camera) {
      return;
    }

    const width = canvas.clientWidth || 120;
    const height = canvas.clientHeight || 120;
    this.camera.aspect = width / height;
    this.camera.updateProjectionMatrix();
    this.renderer.setSize(width, height, false);
  };
}
