import { AfterContentChecked, Component, HostListener, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { UntypedFormControl, UntypedFormGroup } from '@angular/forms';
import { HttpParams } from '@angular/common/http';

import { MatFormFieldAppearance } from '@angular/material/form-field';

import { LangChangeEvent, TranslateService } from '@ngx-translate/core';

import { ConfigService } from 'projects/tools/src/lib/config.service';
import { Tools } from 'projects/tools/src/lib/tools.service';
import { EventsManagerService } from 'projects/tools/src/lib/eventsmanager.service';
import { OpenAPIService } from 'projects/govregistry-app/src/services/openAPI.service';
import { PageloaderService } from 'projects/tools/src/lib/pageloader.service';
import { SearchBarFormComponent } from 'projects/components/src/lib/ui/search-bar-form/search-bar-form.component';

@Component({
  selector: 'app-organizations',
  templateUrl: 'organizations.component.html',
  styleUrls: ['organizations.component.scss']
})
export class OrganizationsComponent implements OnInit, AfterContentChecked, OnDestroy {
  static readonly Name = 'OrganizationsComponent';
  readonly model: string = 'organizations';

  @ViewChild('searchBarForm') searchBarForm!: SearchBarFormComponent;

  config: any;
  organizationsConfig: any;

  organizations: any[] = [];
  page: any = {};
  _links: any = {};

  _isEdit: boolean = false;

  _hasFilter: boolean = true;
  _formGroup: UntypedFormGroup = new UntypedFormGroup({});
  _filterData: any[] = [];

  _preventMultiCall: boolean = false;

  _spin: boolean = false;
  desktop: boolean = false;

  _materialAppearance: MatFormFieldAppearance = 'fill';

  _message: string = 'APP.MESSAGE.NoResults';
  _messageHelp: string = 'APP.MESSAGE.NoResultsHelp';
  _messageUnimplemented: string = 'APP.MESSAGE.Unimplemented';

  _error: boolean = false;

  showHistory: boolean = true;
  showSearch: boolean = true;
  showSorting: boolean = true;

  sortField: string = 'legal_name';
  sortDirection: string = 'asc';
  sortFields: any[] = [
    { field: 'id', label: 'APP.LABEL.Id', icon: '' },
    { field: 'legal_name', label: 'APP.LABEL.LegalName', icon: '' }
  ];

  searchFields: any[] = [];

  _useRoute: boolean = true;

  breadcrumbs: any[] = [
    { label: 'APP.TITLE.Organizations', url: '', type: 'title', icon: 'corporate_fare' }
  ];

  _unimplemented: boolean = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private translate: TranslateService,
    private configService: ConfigService,
    public tools: Tools,
    private eventsManagerService: EventsManagerService,
    public apiService: OpenAPIService,
    public pageloaderService: PageloaderService
  ) {
    this.config = this.configService.getConfiguration();
    this._materialAppearance = this.config.materialAppearance;

    this._initSearchForm();
  }

  @HostListener('window:resize') _onResize() {
    this.desktop = (window.innerWidth >= 992);
  }

  ngOnInit() {
    this.pageloaderService.resetLoader();
    this.configService.getConfig(this.model).subscribe(
      (config: any) => {
        this.organizationsConfig = config;
        this._loadOrganizations();
      }
    );
  }

  ngOnDestroy() {
    // this.eventsManagerService.off(EventType.NAVBAR_ACTION);
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
    this._formGroup = new UntypedFormGroup({
      q: new UntypedFormControl(''),
    });
  }

  _loadOrganizations(query: any = null, url: string = '') {
    this._setErrorMessages(false);

    let aux: any;
    if (!url) {
      this.organizations = [];
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

          this.page = response.page;
          this._links = response._links;

          if (response.items) {
            const _list: any = response.items.map((organization: any) => {
              const metadataText = Tools.simpleItemFormatter(this.organizationsConfig.simpleItem.metadata.text, organization, this.organizationsConfig.simpleItem.options || null);
              const metadataLabel = Tools.simpleItemFormatter(this.organizationsConfig.simpleItem.metadata.label, organization, this.organizationsConfig.simpleItem.options || null);
              const element = {
                id: organization.id,
                primaryText: Tools.simpleItemFormatter(this.organizationsConfig.simpleItem.primaryText, organization, this.organizationsConfig.simpleItem.options || null),
                secondaryText: Tools.simpleItemFormatter(this.organizationsConfig.simpleItem.secondaryText, organization, this.organizationsConfig.simpleItem.options || null),
                metadata: `${metadataText}<span class="me-2">&nbsp;</span>${metadataLabel}`,
                secondaryMetadata: Tools.simpleItemFormatter(this.organizationsConfig.simpleItem.secondaryMetadata, organization, this.organizationsConfig.simpleItem.options || null),
                editMode: false,
                source: { ...organization }
              };
              return element;
            });
            this.organizations = (url) ? [...this.organizations, ..._list] : [..._list];
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
      this._loadOrganizations(null, this._links.next.href);
    }
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
    // if (Object.keys(values).some((item: any) => values[item] != "")) {
      this._filterData = values;
      this._loadOrganizations(this._filterData);
    // }
  }

  _resetForm() {
    this._filterData = [];
    this._loadOrganizations(this._filterData);
  }

  _onSort(event: any) {
    this.sortField = event.sortField;
    this.sortDirection = event.sortBy;
    this._loadOrganizations(this._filterData);
  }

  onBreadcrumb(event: any) {
    this.router.navigate([event.url]);
  }

  _resetScroll() {
    Tools.ScrollElement('container-scroller', 0);
  }
}
