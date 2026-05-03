import {
  computed,
  DestroyRef,
  effect,
  EffectRef,
  inject,
  Injectable,
  Injector,
  signal,
  Signal,
} from '@angular/core';

import { AffiliationDto } from '../models/affiliation.interface';
import { AffiliationPollingService } from './affiliation-polling.service';
import { AffiliationSseService } from './affiliation-sse.service';

export type FeedStrategy = 'polling' | 'sse';

@Injectable({ providedIn: 'root' })
export class AffiliationFeedFacade {
  private readonly polling = inject(AffiliationPollingService);
  private readonly sse = inject(AffiliationSseService);
  private readonly injector = inject(Injector);
  private readonly destroyRef = inject(DestroyRef);

  private readonly writableNewAffiliations = signal<AffiliationDto[]>([]);
  private readonly writableLatestArrivalLatencyMs = signal<number | null>(null);
  /** Session samples for Actividad 30 empirical protocol (mean/median). */
  private readonly writableLatencySamplesMs = signal<number[]>([]);

  private connectionEffectRef: EffectRef | null = null;

  readonly strategy = signal<FeedStrategy>('polling');
  readonly newAffiliations: Signal<AffiliationDto[]> =
    this.writableNewAffiliations.asReadonly();
  readonly unreadCount = computed(() => this.writableNewAffiliations().length);
  readonly latestArrivalLatencyMs: Signal<number | null> =
    this.writableLatestArrivalLatencyMs.asReadonly();

  readonly latencySampleCount = computed(
    () => this.writableLatencySamplesMs().length,
  );

  readonly latencyMeanMs = computed(() => {
    const xs = this.writableLatencySamplesMs();
    if (xs.length === 0) {
      return null;
    }
    return xs.reduce((sum, x) => sum + x, 0) / xs.length;
  });

  readonly latencyMedianMs = computed(() => {
    const xs = this.writableLatencySamplesMs();
    if (xs.length === 0) {
      return null;
    }
    const sorted = [...xs].sort((a, b) => a - b);
    const mid = Math.floor(sorted.length / 2);
    return sorted.length % 2 === 1
      ? sorted[mid]!
      : (sorted[mid - 1]! + sorted[mid]!) / 2;
  });

  constructor() {
    this.destroyRef.onDestroy(() => {
      this.connectionEffectRef?.destroy();
    });
  }

  /** Called once from the smart component; resubscribes when strategy changes. */
  connect(): void {
    if (this.connectionEffectRef) {
      return;
    }

    this.connectionEffectRef = effect(
      onCleanup => {
        const current = this.strategy();
        const subscription =
          current === 'polling'
            ? this.polling
                .pollChanges$()
                .subscribe(rows => this.ingestBatch(rows))
            : this.sse.stream$().subscribe(row => this.ingestBatch([row]));

        onCleanup(() => subscription.unsubscribe());
      },
      { injector: this.injector, manualCleanup: true },
    );
  }

  markAllRead(): void {
    this.writableNewAffiliations.set([]);
  }

  /** Clears accumulated latency samples (e.g. after switching transport strategy). */
  resetMeasurementSamples(): void {
    this.writableLatencySamplesMs.set([]);
    this.writableLatestArrivalLatencyMs.set(null);
  }

  private ingestBatch(rows: AffiliationDto[]): void {
    if (rows.length === 0) {
      return;
    }

    const arrivedAt = Date.now();
    const newest = rows[rows.length - 1]!;

    this.writableLatestArrivalLatencyMs.set(
      arrivedAt - new Date(newest.createdAt).getTime(),
    );

    this.writableLatencySamplesMs.update(previous => {
      const next = [...previous];
      for (const row of rows) {
        next.push(arrivedAt - new Date(row.createdAt).getTime());
      }
      const cap = 512;
      return next.length > cap ? next.slice(-cap) : next;
    });

    this.writableNewAffiliations.update(previous => [...rows, ...previous]);
  }
}
