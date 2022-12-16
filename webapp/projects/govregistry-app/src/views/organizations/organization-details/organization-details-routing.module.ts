import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { OrganizationDetailsComponent } from './organization-details.component';

const routes: Routes = [
  {
    path: '',
    children: [
      {
        path: '',
        component: OrganizationDetailsComponent
      }
    ]
  }
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class OrganizationDetailsRoutingModule {}
