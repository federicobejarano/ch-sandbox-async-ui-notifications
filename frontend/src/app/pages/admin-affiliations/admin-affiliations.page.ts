import {
  Component,
  effect,
  inject,
  Injector,
  OnInit,
} from '@angular/core';
import { ToastController } from '@ionic/angular';
import {
  IonContent,
  IonHeader,
  IonTitle,
  IonToolbar,
} from '@ionic/angular/standalone';

import { AffiliationFeedFacade } from '../../services/affiliation-feed.facade';

@Component({
  standalone: true,
  selector: 'app-admin-affiliations',
  imports: [IonContent, IonHeader, IonTitle, IonToolbar],
  template: `
    <ion-header>
      <ion-toolbar>
        <ion-title>Admin · Affiliations</ion-title>
      </ion-toolbar>
    </ion-header>
    <ion-content class="ion-padding"></ion-content>
  `,
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
