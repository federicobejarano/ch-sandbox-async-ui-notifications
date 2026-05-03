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

  private connectionEffectRef: EffectRef | null = null;

  readonly strategy = signal<FeedStrategy>('polling');
  readonly newAffiliations: Signal<AffiliationDto[]> =
    this.writableNewAffiliations.asReadonly();
  readonly unreadCount = computed(() => this.writableNewAffiliations().length);
  readonly latestArrivalLatencyMs: Signal<number | null> =
    this.writableLatestArrivalLatencyMs.asReadonly();

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

  private ingestBatch(rows: AffiliationDto[]): void {
    if (rows.length === 0) {
      return;
    }

    const arrivedAt = Date.now();
    const newest = rows[rows.length - 1]!;

    this.writableLatestArrivalLatencyMs.set(
      arrivedAt - new Date(newest.createdAt).getTime(),
    );
    this.writableNewAffiliations.update(previous => [...rows, ...previous]);
  }
}
