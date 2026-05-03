import { DatePipe, DecimalPipe, NgFor, NgIf } from '@angular/common';
import {
  Component,
  effect,
  inject,
  Injector,
  OnInit,
} from '@angular/core';
import { ToastController } from '@ionic/angular';
import {
  IonBadge,
  IonButton,
  IonButtons,
  IonContent,
  IonHeader,
  IonItem,
  IonLabel,
  IonList,
  IonNote,
  IonTitle,
  IonToolbar,
} from '@ionic/angular/standalone';

import { AffiliationFeedFacade } from '../../services/affiliation-feed.facade';

@Component({
  standalone: true,
  selector: 'app-admin-affiliations',
  imports: [
    DatePipe,
    DecimalPipe,
    IonBadge,
    IonButton,
    IonButtons,
    IonContent,
    IonHeader,
    IonItem,
    IonLabel,
    IonList,
    IonNote,
    IonTitle,
    IonToolbar,
    NgFor,
    NgIf,
  ],
  templateUrl: './admin-affiliations.page.html',
  styleUrls: ['./admin-affiliations.page.scss'],
})
export class AdminAffiliationsPage implements OnInit {
  readonly facade = inject(AffiliationFeedFacade);
  private readonly toast = inject(ToastController);
  private readonly injector = inject(Injector);

  ngOnInit(): void {
    this.facade.connect();

    let previousUnread = 0;
    effect(
      () => {
        const count = this.facade.unreadCount();
        if (count > previousUnread) {
          void this.showToast(count);
        }
        previousUnread = count;
      },
      { injector: this.injector },
    );
  }

  toggleStrategy(): void {
    this.facade.resetMeasurementSamples();
    this.facade.strategy.update(s => (s === 'polling' ? 'sse' : 'polling'));
  }

  private async showToast(count: number): Promise<void> {
    const toast = await this.toast.create({
      message: `${count} new affiliation${count > 1 ? 's' : ''}`,
      duration: 1500,
      position: 'top',
      color: 'success',
    });
    await toast.present();
  }
}
