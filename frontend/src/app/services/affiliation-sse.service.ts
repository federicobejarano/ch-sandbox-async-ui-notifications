import { inject, Injectable, NgZone } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import { AffiliationDto } from '../models/affiliation.interface';

@Injectable({ providedIn: 'root' })
export class AffiliationSseService {
  private readonly ngZone = inject(NgZone);
  private readonly url = `${environment.apiUrl}/affiliations/stream`;

  /**
   * Cold Observable: each subscription opens a dedicated EventSource and
   * teardown closes it, letting the backend clean up the paired emitter.
   */
  stream$(): Observable<AffiliationDto> {
    return new Observable<AffiliationDto>(subscriber => {
      let source!: EventSource;

      this.ngZone.runOutsideAngular(() => {
        source = new EventSource(this.url);

        source.addEventListener('affiliation-created', (event: Event) => {
          const messageEvent = event as MessageEvent<string>;
          const payload = JSON.parse(messageEvent.data) as AffiliationDto;

          subscriber.next(payload);
        });

        source.onerror = () => {
          // Native EventSource retries automatically; only surface terminal close.
          if (source.readyState === EventSource.CLOSED) {
            subscriber.error(new Error('SSE stream permanently closed'));
          }
        };
      });

      return () => {
        source.close();
      };
    });
  }
}
