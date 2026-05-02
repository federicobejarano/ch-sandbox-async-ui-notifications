import { HttpClient, HttpHeaders } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { catchError, exhaustMap, filter, interval, map, Observable, of, startWith, tap, throwError } from 'rxjs';

import { environment } from '../../environments/environment';
import { AffiliationDto } from '../models/affiliation.interface';

@Injectable({ providedIn: 'root' })
export class AffiliationPollingService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/affiliations`;

  private lastSeenInstant = new Date(0).toISOString();
  private lastEtag: string | null = null;

  /**
   * Poll the delta endpoint and emit only when the backend confirms new rows.
   */
  pollChanges$(intervalMs = 5_000): Observable<AffiliationDto[]> {
    return interval(intervalMs).pipe(
      startWith(0),
      exhaustMap(() => this.fetchChanges()),
      filter((rows): rows is AffiliationDto[] => rows.length > 0),
      tap(rows => {
        this.lastSeenInstant = rows[rows.length - 1]!.createdAt;
      }),
    );
  }

  private fetchChanges(): Observable<AffiliationDto[]> {
    const headers = this.lastEtag
      ? new HttpHeaders({ 'If-None-Match': this.lastEtag })
      : undefined;

    return this.http
      .get<AffiliationDto[]>(`${this.url}/changes`, {
        params: { since: this.lastSeenInstant },
        headers,
        observe: 'response',
      })
      .pipe(
        tap(response => {
          this.lastEtag = response.headers.get('ETag') ?? this.lastEtag;
        }),
        map(response => response.body ?? []),
        catchError(error =>
          error.status === 304
            ? of([] as AffiliationDto[])
            : throwError(() => error),
        ),
      );
  }
}
