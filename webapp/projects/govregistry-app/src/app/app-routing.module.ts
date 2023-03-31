import { NgModule } from '@angular/core';
import { RouterModule, Routes, PreloadAllModules } from '@angular/router';

import { AuthGuard, PermissionGuard } from '../guard';

import { GpLayoutComponent, SimpleLayoutComponent } from '../containers';

const routes: Routes = [
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
  {
    path: 'auth',
    component: SimpleLayoutComponent,
    children: [
      {
        path: 'login',
        loadChildren: () => import('../views/login/login.module').then(m => m.LoginModule)
      }
    ]
  },
  {
    path: '',
    component: GpLayoutComponent,
    canActivate: [AuthGuard],
    children: [
      {
        path: '_home',
        loadChildren: () => import('../views/home/home.module').then(m => m.HomeModule),
      },
      {
        path: 'dashboard',
        loadChildren: () => import('../views/dashboard/dashboard.module').then(m => m.DashboardModule)
      },
      {
        path: 'users',
        canActivate: [PermissionGuard],
        data: {  permission: 'USERS', grant: 'view' },
        loadChildren: () => import('../views/users/users.module').then(m => m.UsersModule)
      },
      {
        path: 'organizations',
        canActivate: [PermissionGuard],
        data: {  permission: 'ORGANIZATIONS', grant: 'view' },
        loadChildren: () => import('../views/organizations/organizations.module').then(m => m.OrganizationsModule)
      },
      {
        path: 'services',
        canActivate: [PermissionGuard],
        data: {  permission: 'SERVICES', grant: 'view' },
        loadChildren: () => import('../views/services/services.module').then(m => m.ServicesModule)
      },
    ]
  },
  { path: '**', redirectTo: 'dashboard' }
];

@NgModule({
  imports: [RouterModule.forRoot(
    routes,
    { preloadingStrategy: PreloadAllModules, enableTracing: false }
  )],
  exports: [RouterModule]
})
export class AppRoutingModule { }
