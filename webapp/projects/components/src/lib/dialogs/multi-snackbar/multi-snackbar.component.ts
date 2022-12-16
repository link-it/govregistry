import { Component, OnDestroy, OnInit } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Component({
  selector: 'app-multi-snackbar',
  template: `
    <div *ngFor="let snackbar of MSC.MultiSnackbar; let idx = index" [ngClass]="idx !== (MSC.MultiSnackbar.length - 1)?'old-message':''"
        class="snackbar-message d-flex flex-column flex-sm-row align-items-sm-center justify-content-sm-between">
      <p class="m-0">{{ snackbar.message }}</p>
      <button mat-button *ngIf="snackbar.action" type="button" class="snackbar-action ms-3 text-right text-sm-center" color="accent" (click)="__cleanMessage(idx)">{{ snackbar.action }}</button>
    </div>
  `,
  styles: [`
    :host {
      display: block;
      position: fixed;
      bottom: 0;
      left: 0;
      right: 0;
      z-index: 1001;
      padding: .5rem;
      overflow: hidden;
    }

    .snackbar-message {
      margin-bottom: .5rem;
      border-radius: 4px;
      padding: 14px 16px;
      color: hsla(0, 0%, 100%, .7);
      background-color: #323232;
      box-shadow: 0 0 8px rgba(0, 0, 0, 0.2);
    }

    .old-message {
      background-color: rgba(50, 50, 50, .75);
    }

    .snackbar-message p {
      line-height: 1.5;
      text-overflow: ellipsis;
      overflow: hidden;
    }

    button.snackbar-action {
      height: initial;
      min-width: initial;
      padding-right: 0;
    }

    @media(min-width: 768px) {
      :host {
        left: 15%;
        right: 15%;
      }
    }
  `]
})
export class MultiSnackbarComponent implements OnInit, OnDestroy {

  MSC = MultiSnackbarComponent;

  public static Duration: number = 10000;
  public static MessageClean: BehaviorSubject<any> = new BehaviorSubject(null);

  static MultiSnackbar: any[] = [];
  static _Timer: any;

  constructor() { }

  ngOnInit(): void {
  }

  ngOnDestroy(): void {
    clearInterval(MultiSnackbarComponent._Timer);
  }

  public static PushMessage(_message: string, _action: boolean = true, _keep: boolean = false, _actionLabel: string | null = null, _uuid: string | null = null) {
    let _actions = null;
    if (_action) {
      _actions = _actionLabel?_actionLabel:'Chiudi';
    }
    if(_message) {
      if (!MultiSnackbarComponent.CheckMultiSnackbar(_uuid)) {
        MultiSnackbarComponent.MultiSnackbar.push({ message: _message, action: _actions, keep: _keep, time: new Date().getTime(), uuid: _uuid});
        if (!_keep) {
          MultiSnackbarComponent.__CleanMessageInterval();
        }
      }
    }
  }

  public static DestroyStickyMessage(uid: string) {
    let exit: boolean = false;
    MultiSnackbarComponent.MultiSnackbar.forEach((snackbar: any, index: number) => {
      if ((snackbar.uuid === uid && uid !== null) || !snackbar.action) {
        MultiSnackbarComponent.MultiSnackbar.splice(index, 1);
        exit = true;
      }
      if (exit) {
        return;
      }
    });
  }

  private static CheckMultiSnackbar(uuid: string | null = null): boolean {
    const _sb: any[] = MultiSnackbarComponent.MultiSnackbar.filter((sb: any) => {
      return (sb.uuid === uuid && uuid !== null);
    });
    return (_sb && _sb.length !== 0);
  }

  public static DestroyAllStickyMessages() {
    MultiSnackbarComponent.MultiSnackbar = [];
  }

  __cleanMessage(index: number) {
    MultiSnackbarComponent.MessageClean.next(MultiSnackbarComponent.MultiSnackbar[index]);
    MultiSnackbarComponent.MultiSnackbar.splice(index, 1);
  }

  private static __CleanMessageInterval() {
    clearInterval(MultiSnackbarComponent._Timer);
    MultiSnackbarComponent._Timer = setInterval(() => {
      if (MultiSnackbarComponent.MultiSnackbar && MultiSnackbarComponent.MultiSnackbar.length !== 0) {
        let exit: boolean = false;
        MultiSnackbarComponent.MultiSnackbar.forEach((snackbar: any, index: number) => {
          if (!snackbar.keep || (snackbar.keep && !snackbar.action)) {
            if ((new Date().getTime() - snackbar.time) >= MultiSnackbarComponent.Duration) {
              MultiSnackbarComponent.MultiSnackbar.splice(index, 1);
              exit = true;
            }
          }
          if (exit) {
            return;
          }
        });
      } else {
        clearInterval(MultiSnackbarComponent._Timer);
      }
    }, MultiSnackbarComponent.Duration);
  }

}
