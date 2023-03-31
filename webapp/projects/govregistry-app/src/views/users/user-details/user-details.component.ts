import { AfterContentChecked, Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChanges } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { AbstractControl, UntypedFormControl, UntypedFormGroup, Validators } from '@angular/forms';

import { TranslateService } from '@ngx-translate/core';
import { BsModalService, BsModalRef } from 'ngx-bootstrap/modal';

import { ConfigService } from 'projects/tools/src/lib/config.service';
import { Tools } from 'projects/tools/src/lib/tools.service';
import { EventsManagerService } from 'projects/tools/src/lib/eventsmanager.service';
import { AuthenticationService } from '@app/services/authentication.service';
import { OpenAPIService } from 'projects/govregistry-app/src/services/openAPI.service';

import { YesnoDialogBsComponent } from 'projects/components/src/lib/dialogs/yesno-dialog-bs/yesno-dialog-bs.component';

import { User } from './user';

import * as jsonpatch from 'fast-json-patch';

@Component({
  selector: 'app-user-details',
  templateUrl: 'user-details.component.html',
  styleUrls: ['user-details.component.scss']
})
export class UserDetailsComponent implements OnInit, OnChanges, AfterContentChecked, OnDestroy {
  static readonly Name = 'UserDetailsComponent';
  readonly model: string = 'users';

  @Input() id: number | null = null;
  @Input() user: any = null;
  @Input() config: any = null;

  @Output() close: EventEmitter<any> = new EventEmitter<any>();
  @Output() save: EventEmitter<any> = new EventEmitter<any>();

  _title: string = '';

  appConfig: any;

  hasTab: boolean = true;
  tabs: any[] = [
    { label: 'Details', icon: 'details', link: 'details', enabled: true }
  ];
  _currentTab: string = 'details';

  _isDetails = true;

  _isEdit = false;
  _closeEdit = true;
  _isNew = false;
  _formGroup: UntypedFormGroup = new UntypedFormGroup({});
  _user: User = new User({});

  _authorizations: any[] = [];

  userProviders: any = null;

  _spin: boolean = true;
  desktop: boolean = false;

  _useRoute: boolean = true;

  breadcrumbs: any[] = [];

  _error: boolean = false;
  _errorMsg: string = '';
  
  _modalConfirmRef!: BsModalRef;
  
  _canEdit: boolean = false;
  
  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private translate: TranslateService,
    private modalService: BsModalService,
    private configService: ConfigService,
    public tools: Tools,
    public eventsManagerService: EventsManagerService,
    public authenticationService: AuthenticationService,
    public apiService: OpenAPIService
  ) {
    this.appConfig = this.configService.getConfiguration();
  }

  ngOnInit() {
    this._canEdit = this.authenticationService.hasPermission('USERS', 'edit');

    this.route.params.subscribe(params => {
      if (params['id'] && params['id'] !== 'new') {
        this.id = params['id'];
        this._initBreadcrumb();
        this._isDetails = true;
        this.configService.getConfig(this.model).subscribe(
          (config: any) => {
            this.config = config;
            this._loadAll();
          }
        );
      } else {
        this._isNew = true;
        this._isEdit = true;

        this._initBreadcrumb();
        // this._loadAnagrafiche();

        if (this._isEdit) {
          this._initForm({ ...this._user });
        } else {
          this._loadAll();
        }
      }
    });
  }

  ngOnDestroy() {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.id) {
      this.id = changes.id.currentValue;
      this._loadAll();
    }
    if (changes.user) {
      const user = changes.user.currentValue;
      this.user = user.source;
      this.id = this.user.id;
    }
  }

  ngAfterContentChecked(): void {
    this.desktop = (window.innerWidth >= 992);
  }

  _loadAll() {
    this._loadUser();
    // this._loadAuthorization();
  }

  _hasControlError(name: string) {
    return (this.f[name].errors && this.f[name].touched);
  }

  get f(): { [key: string]: AbstractControl } {
    return this._formGroup.controls;
  }

  _initForm(data: any = null) {
    if (data) {
      let _group: any = {};
      Object.keys(data).forEach((key) => {
        let value = '';
        switch (key) {
          case 'principal':
            // const pattern = /^[\w_\-\.]+$/;
            value = data[key] ? data[key] : null;
            _group[key] = new UntypedFormControl(value, [
              Validators.required,
              // Validators.pattern(pattern)
            ]);
            break;
          case 'full_name':
            value = data[key] ? data[key] : null;
            _group[key] = new UntypedFormControl(value, [ Validators.required ]);
            break;
          case 'email':
            value = data[key] ? data[key] : null;
            _group[key] = new UntypedFormControl(value, [
              // Validators.required,
              Validators.email
            ]);
            break;
          case 'enabled':
            value = data[key] ? data[key] : false;
            _group[key] = new UntypedFormControl(value, []);
            break;
          default:
            value = data[key] ? data[key] : null;
            _group[key] = new UntypedFormControl(value, []);
            break;
        }
      });
      this._formGroup = new UntypedFormGroup(_group);
    }
  }

  __onSave(body: any) {
    this._error = false;
    this.apiService.saveElement(this.model, body).subscribe(
      (response: any) => {
        this.user = new User({ ...response });
        this._user = new User({ ...response });
        this.id = this.user.id;
        this._initBreadcrumb();
        this._isEdit = false;
        this._isNew = false;
        this.save.emit({ id: this.id, user: response, update: false });
        this.router.navigate([this.model, this.user.id], { replaceUrl: true });
      },
      (error: any) => {
        this._error = true;
        this._errorMsg = Tools.GetErrorMsg(error);
      }
    );
  }

  __onUpdate(id: number, body: any) {
    this._error = false;
    const _bodyPatch: any[] = jsonpatch.compare(this.user, body);
    if (_bodyPatch) {
      this.apiService.updateElement(this.model, id, _bodyPatch).subscribe(
        (response: any) => {
          this._isEdit = !this._closeEdit;
          this.user = new User({ ...response });
          this._user = new User({ ...response });
          this.id = this.user.id;
          this.save.emit({ id: this.id, user: response, update: true });
        },
        (error: any) => {
          this._error = true;
          this._errorMsg = Tools.GetErrorMsg(error);
        }
      );
    } else {
      console.log('No difference');
    }
  }

  _onSubmit(form: any, close: boolean = true) {
    if (this._isEdit && this._formGroup.valid) {
      this._closeEdit = close;
      if (this._isNew) {
        this.__onSave(form);
      } else {
        this.__onUpdate(this.user.id, form);
      }
    }
  }

  _deleteUser() {
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
          this.apiService.deleteElement(this.model, this.user.id).subscribe(
            (response) => {
              this.save.emit({ id: this.id, user: response, update: false });
            },
            (error) => {
              this._error = true;
              this._errorMsg = Tools.GetErrorMsg(error);
            }
          );
        }
      }
    );
  }

  _loadUser() {
    if (this.id) {
      this._spin = true;
      this.user = null;
      this.apiService.getDetails(this.model, this.id).subscribe({
        next: (response: any) => {
          this.user = new User({ ...response });
          this._user = new User({ ...response });
          this._spin = false;
        },
        error: (error: any) => {
          this._spin = false;
          Tools.OnError(error);
        }
      });
    }
  }

  _initBreadcrumb() {
    const _title = this.id ? `#${this.id}` : this.translate.instant('APP.TITLE.New');
    this.breadcrumbs = [
      { label: '', url: '', type: 'title', icon: 'people' },
      { label: 'APP.TITLE.Users', url: '/users', type: 'link' },
      { label: `${_title}`, url: '', type: 'title' }
    ];
  }

  _clickTab(tab: string) {
    this._currentTab = tab;
  }

  _dummyAction(event: any, param: any) {
    console.log(event, param);
  }

  _editUser() {
    this._initForm({ ...this._user });
    this._isEdit = true;
    this._error = false;
  }

  _onClose() {
    this.close.emit({ id: this.id, user: this._user });
  }

  _onSave() {
    this.save.emit({ id: this.id, user: this._user });
  }

  _onCancelEdit() {
    this._isEdit = false;
    if (this._isNew) {
      if (this._useRoute) {
        this.router.navigate([this.model]);
      } else {
        this.close.emit({ id: this.id, user: null });
      }
    } else {
      this._user = new User({ ...this.user });
    }
  }

  onBreadcrumb(event: any) {
    if (this._useRoute) {
      this.router.navigate([event.url]);
    } else {
      this._onClose();
    }
  }

  _loadAuthorization() {
    this.apiService.getDetails(this.model, this.id, 'authorizations').subscribe({
      next: (response: any) => {
        this._authorizations = response.items;
        if (this.config.detailsTitle) {
          this._title = Tools.simpleItemFormatter(this.config.detailsTitle, this._authorizations);
        }
      },
      error: (error: any) => {
        Tools.OnError(error);
      }
    });
  }
}
