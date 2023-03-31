import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { TranslateModule } from '@ngx-translate/core';

import { VendorsModule } from 'projects/vendors/src/lib/vendors.module';
import { ComponentsModule } from 'projects/components/src/lib/components.module';
import { LinkLabModule } from 'projects/link-lab/src/lib/link-lab.module';
import { HasPermissionModule } from '@app/directives/has-permission/has-permission.module';

import { UserDetailsComponent } from './user-details.component';
import { UserDetailsRoutingModule } from './user-details-routing.module';
import { UserAuthorizationsModule } from '../user-authorizations/user-authorizations.module';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    TranslateModule,
    VendorsModule,
    ComponentsModule,
    LinkLabModule,
    HasPermissionModule,
    UserDetailsRoutingModule,
    UserAuthorizationsModule
  ],
  declarations: [
    UserDetailsComponent
  ],
  exports: [UserDetailsComponent],
  providers: [],
  schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class UserDetailsModule { }
