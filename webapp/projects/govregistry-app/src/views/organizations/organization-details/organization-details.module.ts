import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { TranslateModule } from '@ngx-translate/core';

import { VendorsModule } from 'projects/vendors/src/lib/vendors.module';
import { ComponentsModule } from 'projects/components/src/lib/components.module';
import { LinkLabModule } from 'projects/link-lab/src/lib/link-lab.module';
import { HasPermissionModule } from '@app/directives/has-permission/has-permission.module';

import { OrganizationDetailsComponent } from './organization-details.component';
import { OrganizationDetailsRoutingModule } from './organization-details-routing.module';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    TranslateModule,
    VendorsModule,
    ComponentsModule,
    LinkLabModule,
    HasPermissionModule,
    OrganizationDetailsRoutingModule
  ],
  declarations: [
    OrganizationDetailsComponent
  ],
  exports: [OrganizationDetailsComponent],
  providers: [],
  schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class OrganizationDetailsModule { }
