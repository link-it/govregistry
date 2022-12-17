import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { OrganizationsComponent } from './organizations.component';
import { OrganizationDetailsComponent } from './organization-details/organization-details.component';

const routes: Routes = [
  {
    path: '',
    children: [
      {
        path: '',
        data: { breadcrumb: 'Organizations' },
        component: OrganizationsComponent
      },
      {
        path: ':id',
        data: { breadcrumb: ':id' },
        component: OrganizationDetailsComponent
      }
    ]
  }
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class OrganizationsRoutingModule {}
