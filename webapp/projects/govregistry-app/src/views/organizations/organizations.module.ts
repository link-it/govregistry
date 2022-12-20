import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { TranslateModule } from '@ngx-translate/core';

import { VendorsModule } from 'projects/vendors/src/lib/vendors.module';
import { ComponentsModule } from 'projects/components/src/lib/components.module';
import { LinkLabModule } from 'projects/link-lab/src/lib/link-lab.module';
import { HasPermissionModule } from '../../directives/has-permission/has-permission.module';

import { OrganizationsComponent } from './organizations.component';
import { OrganizationsRoutingModule } from './organizations-routing.module';
import { OrganizationDetailsModule } from './organization-details/organization-details.module';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    TranslateModule,
    VendorsModule,
    ComponentsModule,
    LinkLabModule,
    HasPermissionModule,
    OrganizationsRoutingModule,
    OrganizationDetailsModule
  ],
  declarations: [
    OrganizationsComponent
  ],
  providers: [],
  schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class OrganizationsModule { }
