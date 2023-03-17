import { AfterContentChecked, Component, HostListener, OnInit, ViewChild } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { UntypedFormControl, UntypedFormGroup } from '@angular/forms';
import { HttpParams } from '@angular/common/http';

import { LangChangeEvent, TranslateService } from '@ngx-translate/core';

import { FieldClass } from 'projects/link-lab/src/lib/it/link/classes/definitions';

import { ConfigService } from 'projects/tools/src/lib/config.service';
import { Tools } from 'projects/tools/src/lib/tools.service';
import { EventsManagerService } from 'projects/tools/src/lib/eventsmanager.service';
import { PageloaderService } from 'projects/tools/src/lib/pageloader.service';
import { OpenAPIService } from 'projects/govregistry-app/src/services/openAPI.service';
import { SearchBarFormComponent } from 'projects/components/src/lib/ui/search-bar-form/search-bar-form.component';

@Component({
  selector: 'app-users',
  templateUrl: 'users.component.html',
  styleUrls: ['users.component.scss']
})
export class UsersComponent implements OnInit, AfterContentChecked {
  static readonly Name = 'UsersComponent';
  readonly model: string = 'users';

  @ViewChild('searchBarForm') searchBarForm!: SearchBarFormComponent;

  config: any;
  usersConfig: any;

  users: any[] = [];
  page: any = {};
  _links: any = {};

  _isEdit: boolean = false;

  _hasFilter: boolean = true;
  _formGroup: UntypedFormGroup = new UntypedFormGroup({});
  _filterData: any[] = [];

  _preventMultiCall: boolean = false;

  _spin: boolean = false;
  desktop: boolean = false;

  _useRoute: boolean = true;

  _message: string = 'APP.MESSAGE.NoResults';
  _messageHelp: string = 'APP.MESSAGE.NoResultsHelp';
  _messageUnimplemented: string = 'APP.MESSAGE.Unimplemented';

  _error: boolean = false;

  showHistory: boolean = true;
  showSearch: boolean = true;
  showSorting: boolean = true;

  sortField: string = 'full_name';
  sortDirection: string = 'asc';
  sortFields: any[] = [
    { field: 'id', label: 'APP.LABEL.Id', icon: '' },
    { field: 'full_name', label: 'APP.LABEL.FullName', icon: '' }
  ];

  searchFields: any[] = [];

  breadcrumbs: any[] = [
    { label: 'APP.TITLE.Users', url: '', type: 'title', icon: 'people' }
  ];

  _unimplemented: boolean = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private translate: TranslateService,
    private configService: ConfigService,
    public tools: Tools,
    private eventsManagerService: EventsManagerService,
    public pageloaderService: PageloaderService,
    public apiService: OpenAPIService
  ) {
    this.config = this.configService.getConfiguration();

    this._initSearchForm();
  }

  @HostListener('window:resize') _onResize() {
    this.desktop = (window.innerWidth >= 992);
  }

  ngOnInit() {
    this.translate.onLangChange.subscribe((event: LangChangeEvent) => {
      setTimeout(() => {
        Tools.WaitForResponse(false);
      }, this.config.AppConfig.DELAY || 0);
    });

    this.pageloaderService.resetLoader();
    this.pageloaderService.isLoading.subscribe({
      next: (x) => { this._spin = x; },
      error: (e: any) => { console.log('loader error', e); }
    });

    this.configService.getConfig(this.model).subscribe(
      (config: any) => {
        this.usersConfig = config;
        this._translateConfig();
        this._loadUsers();
      }
    );
  }

  ngAfterContentChecked(): void {
    this.desktop = (window.innerWidth >= 992);
  }

  _translateConfig() {
    if (this.usersConfig && this.usersConfig.options) {
      Object.keys(this.usersConfig.options).forEach((key: string) => {
        if (this.usersConfig.options[key].label) {
          this.usersConfig.options[key].label = this.translate.instant(this.usersConfig.options[key].label);
        }
        if (this.usersConfig.options[key].values) {
          Object.keys(this.usersConfig.options[key].values).forEach((key2: string) => {
            this.usersConfig.options[key].values[key2].label = this.translate.instant(this.usersConfig.options[key].values[key2].label);
          });
        }
      });
    }
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

  _initSearchForm() {
    this._formGroup = new UntypedFormGroup({});
  }

  _loadUsers(query: any = null, url: string = '') {
    this._setErrorMessages(false);

    if (!url) { this.users = []; }

    let aux: any;
    const sort: any = { sort: this.sortField, sort_direction: this.sortDirection}
    query = { ...query, ...sort };
    aux = { params: this._queryToHttpParams(query) };

    this.apiService.getList(this.model, aux, url).subscribe({
      next: (response: any) => {
        if (response === null) {
          this._unimplemented = true;
        } else {

          if (response.page !== undefined) {
            this.page = response.page;
            this._links = this.page.links;  
          }

          if (response.items) {
            const _itemRow = this.usersConfig.itemRow;
            const _options = this.usersConfig.options;
            const _list: any = response.items.map((user: any) => {
              const metadataText = Tools.simpleItemFormatter(_itemRow.metadata.text, user, _options || null);
              const metadataLabel = Tools.simpleItemFormatter(_itemRow.metadata.label, user, _options || null);
              const element = {
                id: user.id,
                primaryText: Tools.simpleItemFormatter(_itemRow.primaryText, user, _options || null, ' '),
                secondaryText: Tools.simpleItemFormatter(_itemRow.secondaryText, user, _options || null, ' '),
                metadata: `${metadataText}<span class="me-2">&nbsp;</span>${metadataLabel}`,
                secondaryMetadata: Tools.simpleItemFormatter(_itemRow.secondaryMetadata, user, _options || null, ' '),
                editMode: false,
                source: { ...user }
              };
              return element;
            });
            this.users = (url) ? [...this.users, ..._list] : [..._list];
            this._preventMultiCall = false;
          }
          Tools.ScrollTo(0);
        }
      },
      error: (error: any) => {
        this._setErrorMessages(true);
        this._preventMultiCall = false;
        // Tools.OnError(error);
      }
    });
  }

  _queryToHttpParams(query: any) : HttpParams {
    let httpParams = new HttpParams();

    Object.keys(query).forEach(key => {
      if (query[key]) {
        let _dateTime = '';
        switch (key)
        {
          default:
            httpParams = httpParams.set(key, query[key]);
        }
      }
    });
    
    return httpParams; 
  }

  __loadMoreData() {
    if (this._links && this._links.next && !this._preventMultiCall) {
      this._preventMultiCall = true;
      this._loadUsers(null, this._links.next.href);
    }
  }

  _generateUsersFields(data: any) {
    return Tools.generateFields(this.usersConfig.details, data).map((field: FieldClass) => {
      field.label = this.translate.instant(field.label);
      return field;
    });
  }

  _onEdit(event: any, param: any) {
    if (this._useRoute) {
      this.router.navigate([this.model, param.id]);
    } else {
      this._isEdit = true;
    }
  }

  _onNew() {
    if (this._useRoute) {
      this.router.navigate([this.model, 'new']);
    } else {
      this._isEdit = true;
    }
  }

  _onCloseEdit() {
    this._isEdit = false;
  }

  _dummyAction(event: any, param: any) {
    console.log(event, param);
  }

  _onSubmit(form: any) {
    if (this.searchBarForm) {
      this.searchBarForm._onSearch();
    }
  }

  _onSearch(values: any) {
    this._filterData = values;
    this._loadUsers(this._filterData);
  }

  _resetForm() {
    this._filterData = [];
    this._loadUsers(this._filterData);
  }

  _onSort(event: any) {
    this.sortField = event.sortField;
    this.sortDirection = event.sortBy;
    this._loadUsers(this._filterData);
  }

  onBreadcrumb(event: any) {
    this.router.navigate([event.url]);
  }

  _resetScroll() {
    Tools.ScrollElement('container-scroller', 0);
  }
}
