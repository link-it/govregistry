import { AfterContentChecked, Component, HostListener, OnInit, ViewChild } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { UntypedFormControl, UntypedFormGroup } from '@angular/forms';
import { HttpParams } from '@angular/common/http';

import { TranslateService } from '@ngx-translate/core';

import { FieldClass } from 'projects/link-lab/src/lib/it/link/classes/definitions';

import { ConfigService } from 'projects/tools/src/lib/config.service';
import { Tools } from 'projects/tools/src/lib/tools.service';
import { EventsManagerService } from 'projects/tools/src/lib/eventsmanager.service';
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

  _spin: boolean = true;
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
    public apiService: OpenAPIService
  ) {
    this.config = this.configService.getConfiguration();

    this._initSearchForm();
  }

  @HostListener('window:resize') _onResize() {
    this.desktop = (window.innerWidth >= 992);
  }

  ngOnInit() {
    this.configService.getConfig(this.model).subscribe(
      (config: any) => {
        this.usersConfig = config;
        this._loadUsers();
      }
    );
  }

  ngAfterContentChecked(): void {
    this.desktop = (window.innerWidth >= 992);
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

    let aux: any;
    if (!url) {
      this.users = [];
      const sort: any = { sort: this.sortField, sort_direction: this.sortDirection}
      query = { ...query, ...sort };
      aux = { params: this._queryToHttpParams(query) };
    }

    this._spin = true;
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
            const _list: any = response.items.map((user: any) => {
              const element = {
                id: user.id,
                source: { ...user }
              };
              return element;
            });
            this.users = (url) ? [...this.users, ..._list] : [..._list];
            this._preventMultiCall = false;
          }
          this._spin = false;
          Tools.ScrollTo(0);
        }
      },
      error: (error: any) => {
        this._setErrorMessages(true);
        this._preventMultiCall = false;
        this._spin = false;
        // Tools.OnError(error);
      }
    });
  }

  _queryToHttpParams(query: any) : HttpParams {
    let httpParams = new HttpParams();

    Object.keys(query).forEach(key => {
      if (query[key]) {
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
