import { Component, HostListener, Input, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { AbstractControl, UntypedFormControl, UntypedFormGroup } from '@angular/forms';

import { TranslateService } from '@ngx-translate/core';
import { BsModalService, BsModalRef } from 'ngx-bootstrap/modal';

import { YesnoDialogBsComponent } from 'projects/components/src/lib/dialogs/yesno-dialog-bs/yesno-dialog-bs.component';

import { ConfigService } from 'projects/tools/src/lib/config.service';
import { EventsManagerService } from 'projects/tools/src/lib/eventsmanager.service';
import { OpenAPIService } from '@services/openAPI.service';

@Component({
  selector: 'app-user-authorizations',
  templateUrl: 'user-authorizations.component.html',
  styleUrls: ['user-authorizations.component.scss']
})
export class UserAuthorizationsComponent implements OnInit, OnDestroy {

  @Input() id: number | null = null;

  config: any;

  userAuthorizations: any[] = [];
  page: any = {};
  _links: any = {};

  _isEdit: boolean = false;
  _isNew: boolean = false;
  _editAuthorizations: boolean = false;

  _formGroup: UntypedFormGroup = new UntypedFormGroup({});

  _spin: boolean = false;

  _message: string = 'APP.MESSAGE.NoResults';
  _messageHelp: string = 'APP.MESSAGE.NoResultsHelp';

  _error: boolean = false;

  authorizationsConfig: any = null;

  _modalConfirmRef!: BsModalRef;

  _flexList: boolean = true;

  constructor(
    private translate: TranslateService,
    private modalService: BsModalService,
    private configService: ConfigService,
    private eventsManagerService: EventsManagerService,
    public apiService: OpenAPIService
  ) {
  }

  ngOnInit() {
    this.configService.getConfig('authorizations').subscribe(
      (config: any) => {
        this.authorizationsConfig = config;

        this._loadUserAuthorizations();
      }
    );
  }

  ngOnDestroy() {
  }

  refresh() {
    this._loadUserAuthorizations();
  }

  _setErrorMessages(error: boolean) {
    this._error = error;
    if (this._error) {
      this._message = 'APP.MESSAGE.ERROR.Default';
      this._messageHelp = 'APP.MESSAGE.ERROR.DefaultHelp';
    } else {
      this._message = 'APP.MESSAGE.NoResults';
      this._messageHelp = 'APP.MESSAGE.NoResultsHelp';
    }
  }

  _loadUserAuthorizations(query: any = null, url: string = '') {
    this._setErrorMessages(false);
    if (this.id) {
      this._spin = true;
      this.apiService.getDetails('users', this.id, 'authorizations').subscribe({
        next: (response: any) => {
          this.userAuthorizations = response.items ? JSON.parse(JSON.stringify(response.items)) : null;
          this._spin = false;
        },
        error: (error: any) => {
          this._spin = false;
          this._setErrorMessages(true);
        }
      });
    }
  }

  _onNew() {
    this._isNew = true;
    this._isEdit = true;
    setTimeout(() => {
      this.scrollTo('edit-container');
    }, 400); 
  }

  _onSave(event: any) {
    this._isEdit = false;
    this._isNew = false;
    this.refresh();
  }

  _onDelete(event: any) {
    const _aId = event.id;

    const initialState = {
      title: this.translate.instant('APP.TITLE.Attention'),
      messages: [
        this.translate.instant('APP.MESSAGE.AreYouSure')
      ],
      cancelText: this.translate.instant('APP.BUTTON.Cancel'),
      confirmText: this.translate.instant('APP.BUTTON.Confirm'),
      confirmColor: 'danger'
    };

    this._modalConfirmRef = this.modalService.show(YesnoDialogBsComponent, {
      ignoreBackdropClick: true,
      initialState: initialState
    });
    this._modalConfirmRef.content.onClose.subscribe(
      (response: any) => {
        if (response) {
          this._spin = true;
          this.apiService.deleteElement('authorizations', _aId).subscribe({
            next: (response: any) => {
              this._spin = false;
              this.refresh();
            },
            error: (e: any) => {
              this._setErrorMessages(true);
              this._spin = false;
            }
          });
        }
      }
    );
  }

  _onClose(event: any) {
    this._isEdit = false;
    this._isNew = false;
  }

  _onEditAuthorizations(event: any) {
    this._editAuthorizations = !this._editAuthorizations;
    if (!this._editAuthorizations) {
      this._isEdit = false;
      this._isNew = false;    
    }
  }

  // ScrollTo

  scrollTo(id: string) {
    const box = document.querySelector('.container-scroller');
    const targetElm = document.getElementById(id); // <-- Scroll to here within ".box"

    this.scrollToElm(box, targetElm, 600);   
  }

  scrollToElm(container: any, elm: any, duration: number){
    var pos = this.getRelativePos(elm);

    this._scrollTo(container, pos.top, duration);  // duration in seconds
  }

  getRelativePos(elm: any){
    const pPos: any = elm.parentNode.getBoundingClientRect(), // parent pos
          cPos: any = elm.getBoundingClientRect(), // target pos
          pos: any = {};

    pos.top    = cPos.top    - pPos.top + elm.parentNode.scrollTop + (pPos.bottom - pPos.top),
    pos.right  = cPos.right  - pPos.right,
    pos.bottom = cPos.bottom - pPos.bottom,
    pos.left   = cPos.left   - pPos.left;

    return pos;
  }
  
  _scrollTo(element: any, to: any, duration: number) {
    var start = element.scrollTop,
        change = to - start;

    element.scrollTop = start + change;
  }
}
