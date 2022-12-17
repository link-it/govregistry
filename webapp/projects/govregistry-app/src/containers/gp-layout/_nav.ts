import { INavData } from './gp-sidebar-nav';

export const navItemsMainMenu: INavData[] = [
  {
    title: true,
    label: 'APP.MENU.Dashboard',
    path: 'dashboard',
    url: '/dashboard',
    icon: 'dashboard',
    permission: 'DASHBOARD',
    attributes: { disabled: false }
  },
  {
    title: true,
    label: 'APP.MENU.Users',
    path: 'users',
    url: '/users',
    icon: 'people',
    permission: 'USERS',
    attributes: { disabled: false }
  },
  {
    title: true,
    label: 'APP.MENU.Organizations',
    path: 'organizations',
    url: '/organizations',
    icon: 'corporate_fare',
    permission: 'ORGANIZATIONS',
    attributes: { disabled: false }
  },
  {
    title: true,
    label: 'APP.MENU.Services',
    path: 'services',
    url: '/services',
    icon: 'apps',
    permission: 'SERVICES',
    attributes: { disabled: false }
  },
];
