import { AfterContentChecked, Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChanges } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { AbstractControl, UntypedFormControl, UntypedFormGroup, Validators } from '@angular/forms';

import { LangChangeEvent, TranslateService } from '@ngx-translate/core';
import { BsModalService, BsModalRef } from 'ngx-bootstrap/modal';

import { ConfigService } from 'projects/tools/src/lib/config.service';
import { Tools } from 'projects/tools/src/lib/tools.service';
import { EventsManagerService } from 'projects/tools/src/lib/eventsmanager.service';
import { OpenAPIService } from 'projects/govregistry-app/src/services/openAPI.service';
import { PageloaderService } from 'projects/tools/src/lib/pageloader.service';
import { FieldClass } from 'projects/link-lab/src/lib/it/link/classes/definitions';

import { YesnoDialogBsComponent } from 'projects/components/src/lib/dialogs/yesno-dialog-bs/yesno-dialog-bs.component';

import { Organization } from './organization';

import * as jsonpatch from 'fast-json-patch';
import moment from 'moment';

@Component({
  selector: 'app-organization-details',
  templateUrl: 'organization-details.component.html',
  styleUrls: ['organization-details.component.scss']
})
export class OrganizationDetailsComponent implements OnInit, OnChanges, AfterContentChecked, OnDestroy {
  static readonly Name = 'OrganizationDetailsComponent';
  readonly model: string = 'organizations';

  @Input() id: number | null = null;
  @Input() organization: any = null;
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

  _informazioni: FieldClass[] = [];

  _isDetails = true;

  _isEdit = false;
  _closeEdit = true;
  _isNew = false;
  _formGroup: UntypedFormGroup = new UntypedFormGroup({});
  _organization: Organization = new Organization({});
  _isEditLogos = false;

  organizationProviders: any = null;

  _spin: boolean = true;
  desktop: boolean = false;

  _useRoute: boolean = true;

  breadcrumbs: any[] = [];

  _error: boolean = false;
  _errorMsg: string = '';

  _modalConfirmRef!: BsModalRef;

  _imagePlaceHolder: string = './assets/images/logo-placeholder.png';
  _selectedFile: any = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private translate: TranslateService,
    private modalService: BsModalService,
    private configService: ConfigService,
    public tools: Tools,
    public eventsManagerService: EventsManagerService,
    public apiService: OpenAPIService,
    public pageloaderService: PageloaderService
  ) {
    this.appConfig = this.configService.getConfiguration();
  }

  ngOnInit() {
    this.translate.onLangChange.subscribe((event: LangChangeEvent) => {
      // Changed
    });

    // this.pageloaderService.resetLoader();
    // this.pageloaderService.isLoading.subscribe({
    //   next: (x) => { this._spin = x; },
    //   error: (e: any) => { console.log('loader error', e); }
    // });

    this.route.params.subscribe(params => {
      if (params['id'] && params['id'] !== 'new') {
        this.id = params['id'];
        this._initBreadcrumb();
        this._isDetails = true;
        this.configService.getConfig(this.model).subscribe(
          (config: any) => {
            this.config = config;
            this._translateConfig();
            this._loadAll();
          }
        );
      } else {
        this._isNew = true;
        this._isEdit = true;

        this._initBreadcrumb();
        // this._loadAnagrafiche();

        if (this._isEdit) {
          this._initForm({ ...this._organization });
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
    if (changes.organization) {
      const organization = changes.organization.currentValue;
      this.organization = organization.source;
      this.id = this.organization.id;
    }
  }

  ngAfterContentChecked(): void {
    this.desktop = (window.innerWidth >= 992);
  }

  _loadAll() {
    this._loadOrganization();
    // this._loadOrganizationProviders();
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
          case 'legal_name':
            value = data[key] ? data[key] : null;
            _group[key] = new UntypedFormControl(value, [Validators.required]);
            break;
          case 'tax_code':
            const pattern = /^[0-9]{11}$/;
            value = data[key] ? data[key] : null;
            _group[key] = new UntypedFormControl(value, [
              Validators.required,
              Validators.pattern(pattern)
            ]);
            break;
          case 'office_email_address':
            value = data[key] ? data[key] : null;
            _group[key] = new UntypedFormControl(value, [
              Validators.required,
              Validators.email
            ]);
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
    this._spin = true;
    this.apiService.saveElement(this.model, body).subscribe(
      (response: any) => {
        this.organization = new Organization({ ...response });
        this._organization = new Organization({ ...response });
        this.id = this.organization.id;
        this._initBreadcrumb();
        this._isEdit = false;
        this._isNew = false;
        this._spin = false;
        this.save.emit({ id: this.id, payment: response, update: false });
      },
      (error: any) => {
        this._spin = false;
        this._error = true;
        this._errorMsg = Tools.GetErrorMsg(error);
      }
    );
  }

  __removeEmpty(obj: any) {
    const $this = this;
    return Object.keys(obj)
      .filter(function (k) {
        return obj[k] != null;
      })
      .reduce(function (acc: any, k: string) {
        acc[k] = typeof obj[k] === "object" ? $this.__removeEmpty(obj[k]) : obj[k];
        return acc;
      }, {});
  }

  __onUpdate(id: number, body: any) {
    this._error = false;
    const _organization = this.__removeEmpty(this.organization);
    const _body = this.__removeEmpty(body);
    const _bodyPatch: any[] = jsonpatch.compare(_organization, _body);
    if (_bodyPatch) {
      this.apiService.updateElement(this.model, id, _bodyPatch).subscribe(
        (response: any) => {
          this._isEdit = !this._closeEdit;
          this.organization = new Organization({ ...response });
          this._organization = new Organization({ ...response });
          this.id = this.organization.id;
          this.save.emit({ id: this.id, payment: response, update: true });
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
        this.__onUpdate(this.organization.id, form);
      }
    }
  }

  _deleteOrganization() {
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
          this.apiService.deleteElement(this.model, this.organization.id).subscribe(
            (response) => {
              this.save.emit({ id: this.id, organization: response, update: false });
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

  _loadOrganization(reload: boolean = true) {
    if (this.id) {
      if (reload) {
        this._spin = true;
        this.organization = null;
      }
      this.apiService.getDetails(this.model, this.id).subscribe({
        next: (response: any) => {
          this.organization = response; // new Organization({ ...response });
          this._organization = new Organization({ ...response });
          this._title = this.organization.creditorReferenceId;
          if (this.config.detailsTitle) {
            this._title = Tools.simpleItemFormatter(this.config.detailsTitle, this.organization);
          }
          this._spin = false;
          // this.__initInformazioni();
        },
        error: (error: any) => {
          this._spin = false;
          Tools.OnError(error);
        }
      });
    }
  }

  __initInformazioni() {
    if (this.organization) {
      this._informazioni = Tools.generateFields(this.config.details, this.organization).map((field: FieldClass) => {
        field.label = this.translate.instant(field.label);
        return field;
      });
    }
  }

  _translateConfig() {
    if (this.config && this.config.options) {
      Object.keys(this.config.options).forEach((key: string) => {
        if (this.config.options[key].label) {
          this.config.options[key].label = this.translate.instant(this.config.options[key].label);
        }
        if (this.config.options[key].values) {
          Object.keys(this.config.options[key].values).forEach((key2: string) => {
            this.config.options[key].values[key2].label = this.translate.instant(this.config.options[key].values[key2].label);
          });
        }
      });
    }
  }

  _initBreadcrumb() {
    const _title = this.id ? `#${this.id}` : this.translate.instant('APP.TITLE.New');
    this.breadcrumbs = [
      { label: '', url: '', type: 'title', icon: 'corporate_fare' },
      { label: 'APP.TITLE.Organizations', url: '/organizations', type: 'link' },
      { label: `${_title}`, url: '', type: 'title' }
    ];
  }

  _clickTab(tab: string) {
    this._currentTab = tab;
  }

  _dummyAction(event: any, param: any) {
    console.log(event, param);
  }

  _editOrganization() {
    this._initForm({ ...this._organization });
    this._isEdit = true;
    this._error = false;
  }

  _onClose() {
    this.close.emit({ id: this.id, organization: this._organization });
  }

  _onSave() {
    this.save.emit({ id: this.id, organization: this._organization });
  }

  _onCancelEdit() {
    this._isEdit = false;
    this._error = false;
    this._errorMsg = '';
    if (this._isNew) {
      if (this._useRoute) {
        this.router.navigate([this.model]);
      } else {
        this.close.emit({ id: this.id, organization: null });
      }
    } else {
      this._organization = new Organization({ ...this.organization });
    }
  }

  onBreadcrumb(event: any) {
    if (this._useRoute) {
      this.router.navigate([event.url]);
    } else {
      this._onClose();
    }
  }

  _onImageLoaded(event: any, type: string) {
    this.apiService.uploadImage(this.model, this.organization.id, type, event).subscribe(
      (response) => {
        this._loadOrganization(false);
      },
      (error: any) => {
        console.log('error', error);
      }
    );
  }

  _onFileLoaded(event: any, type: string) {
    this._selectedFile = event.target.files[0];
    console.log(this._selectedFile);

    const formData = new FormData();
    formData.append('file', this._selectedFile, this._selectedFile.name);
    this.apiService.uploadImage(this.model, this.organization.id, type, formData).subscribe(
      (response) => {
        console.log('response', response);
      },
      (error: any) => {
        console.log('error', error);
      }
    );
  }

  // _decodeImage(data: string): string {
  //   return data ? window.atob(data) : this._imagePlaceHolder;
  // }
  _decodeImage = (data: string): string => {
    return data ? window.atob(data) : this._imagePlaceHolder;
  };

  _getLogo(item: any, type: string, bg: boolean = false) {
    let logoUrl = this._imagePlaceHolder;
    if (item && item._links && item._links[type]) {
      logoUrl = item._links[type].href;
      logoUrl += '?t=' + moment().valueOf();
      // logoUrl = this.apiService.getImageUrl(logoUrl);
    }

    return bg ? `url(${logoUrl})` : logoUrl;
  };

  _hasLogo(item: any, type: string) {
    let _hasLogo = false;
    if (item && item._links && item._links[type]) {
      _hasLogo = true;
    }
    return _hasLogo;
  };

  toggleEditLogos() {
    this._isEditLogos = !this._isEditLogos;
  }
}
