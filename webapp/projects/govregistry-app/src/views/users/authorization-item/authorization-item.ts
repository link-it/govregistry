export class AuthorizationItem {

  id: number | null = null;
  role_id: number | null = null;
  role: any | null = null;
  organizations: number[] = [];
  services: number[] = [];
  expiration_date: string | null = null;
  all_organizations: boolean = false;
  all_services: boolean = false;

  constructor(_data?: any) {
    if (_data) {
      for (const key in _data) {
        if (this.hasOwnProperty(key)) {
          if (_data[key] !== null && _data[key] !== undefined) {
            switch (key) {
              default:
                (this as any)[key] = _data[key];
            }
          }
        }
      }
    }
  }
}
