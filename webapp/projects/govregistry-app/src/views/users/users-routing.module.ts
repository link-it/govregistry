import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { UsersComponent } from './users.component';
import { UserDetailsComponent } from './user-details/user-details.component';

const routes: Routes = [
  {
    path: '',
    children: [
      {
        path: '',
        data: { breadcrumb: 'Users' },
        component: UsersComponent
      },
      {
        path: ':id',
        data: { breadcrumb: ':id' },
        component: UserDetailsComponent
      }
    ]
  }
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class UsersRoutingModule {}
