import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';

import { EventType } from 'projects/tools/src/lib/classes/events';
import { ConfigService } from 'projects/tools/src/lib/config.service';
import { EventsManagerService } from 'projects/tools/src/lib/eventsmanager.service';

import * as _ from 'lodash';

export const AUTH_CONST: any = {
  storageSession: 'GORE_SESSION'
};

export const USER_ADMIN: string = 'govhub_sysadmin';

export const PERMISSIONS: any = {
  govhub_organizations_editor: [
    { name: 'DASHBOARD', view: true, edit: false, create: false, delete: false },
    { name: 'USERS', view: false, edit: false, create: false, delete: false },
    { name: 'ORGANIZATIONS', view: true, edit: true, create: true, delete: true },
    { name: 'SERVICES', view: false, edit: false, create: false, delete: false },
  ],
  govhub_organizations_viewer: [
    { name: 'DASHBOARD', view: true, edit: false, create: false, delete: false },
    { name: 'USERS', view: false, edit: false, create: false, delete: false },
    { name: 'ORGANIZATIONS', view: true, edit: false, create: false, delete: false },
    { name: 'SERVICES', view: false, edit: false, create: false, delete: false }
  ],
  govhub_services_editor: [
    { name: 'DASHBOARD', view: true, edit: false, create: false, delete: false },
    { name: 'USERS', view: false, edit: false, create: false, delete: false },
    { name: 'ORGANIZATIONS', view: false, edit: false, create: false, delete: false },
    { name: 'SERVICES', view: true, edit: true, create: true, delete: true }
  ],
  govhub_services_viewer: [
    { name: 'DASHBOARD', view: true, edit: false, create: false, delete: false },
    { name: 'USERS', view: false, edit: false, create: false, delete: false },
    { name: 'ORGANIZATIONS', view: false, edit: false, create: false, delete: false },
    { name: 'SERVICES', view: true, edit: false, create: false, delete: false }
  ],
  govhub_users_editor: [
    { name: 'DASHBOARD', view: true, edit: true, create: true, delete: true },
    { name: 'USERS', view: true, edit: true, create: true, delete: true },
    { name: 'ORGANIZATIONS', view: false, edit: false, create: false, delete: false },
    { name: 'SERVICES', view: false, edit: false, create: false, delete: false }
  ],
  govhub_users_viewer: [
    { name: 'DASHBOARD', view: true, edit: true, create: true, delete: true },
    { name: 'USERS', view: true, edit: false, create: false, delete: false },
    { name: 'ORGANIZATIONS', view: false, edit: false, create: false, delete: false },
    { name: 'SERVICES', view: false, edit: false, create: false, delete: false }
  ],
};

@Injectable({
  providedIn: 'root'
})
export class AuthenticationService {

  private currentSession: any = null;

  config: any = null;
  appConfig: any = null;
  authDevelop: boolean = false;

  API_PROFILE: string = '/profile';
  API_LOGOUT: string = '/logout';


  constructor(
    private http: HttpClient,
    public configService: ConfigService,
    private eventsManagerService: EventsManagerService
  ) {
    this.config = this.configService.getConfiguration();
    this.appConfig = this.configService.getAppConfig();
    this.authDevelop = this.appConfig.AUTH_SETTINGS ? this.appConfig.AUTH_SETTINGS.DEVELOP : false;

    if (this.appConfig?.GOVAPI?.LOGOUT_URL) {
      this.API_LOGOUT = this.appConfig.GOVAPI.LOGOUT_URL;
    }

    this.reloadSession();
  }

  ngOnInit(): void {
  }

  login(username: string, password: string) {
    const _basiAuth = `${username}:${password}`;
    let httpHeaders = new HttpHeaders();
    httpHeaders = httpHeaders.set("Authorization", "Basic " + btoa(_basiAuth));

    const httpOptions = {
      headers: httpHeaders,
      withCredentials: true
    };

    let url = `${this.appConfig.GOVAPI['HOST']}${this.API_PROFILE}`;

    return this.http.get(url, httpOptions);
  }

  logout() {
    localStorage.removeItem(AUTH_CONST.storageSession);
    let url = `${this.appConfig.GOVAPI['HOST']}${this.API_LOGOUT}`;
    return this.http.get(url);
  }

  setCurrentSession(data: any) {
    const session = btoa(encodeURI(JSON.stringify(data)));
    localStorage.setItem(AUTH_CONST.storageSession, session);
  }

  getCurrentSession() {
    const storage = localStorage.getItem(AUTH_CONST.storageSession);
    if (storage) {
      const currentSession = JSON.parse(decodeURI(atob(storage)));
      return currentSession;
    }
    return null;
  }

  reloadSession() {
    this.currentSession = this.getCurrentSession();
    this.eventsManagerService.broadcast(EventType.SESSION_UPDATE, this.currentSession);
  }

  isLogged() {
    if (this.currentSession) {
      return true;
    }
    return this.authDevelop;
;
  }

  getUser() {
    const session = this.getCurrentSession();
    return session?.full_name ? session?.full_name : session?.principal ?? '<no-username>';
  }

  getAuthorizations() {
    const session = this.getCurrentSession();
    return session?.authorizations ?? [];
  }

  hasRole(role: string) {
    const _auths = this.getAuthorizations();
    if (_auths.findIndex((x: any) => x.name === role) > -1) {
      return true;
    }
    return false;
  }

  isAdmin() {
    const _auths: any[] = this.getAuthorizations();
    if (!this.currentSession) {
      return false;
    } else {
      const idx = _auths.findIndex((auth: any) => auth.role.role_name === USER_ADMIN);
      return ( idx > -1);
    }
  }

  getPermissions() {
    const _auths: any[] = this.getAuthorizations();
    let permissions: any[] = [];
    _auths.forEach((auth: any) => {
      permissions = permissions.concat(PERMISSIONS[auth.role.role_name]);
    });
    return permissions;
  }

  hasPermission(value: string, grant = 'view') {
    const uValue = value;
    if (this.isAdmin() || uValue === 'PUBLIC') { return true; }
    const permissions = this.getPermissions();
    const idx = permissions.findIndex((auth: any) => auth.name === uValue);
    const permission = (idx > -1) ? permissions[idx] : null;
    if (permission) {
      return permission[grant];
    }
    return false;
  }
}
