import * as moment from 'moment';

export class User {

  id: number | null = null;
  principal: string | null = null;
  full_name: string | null = null;
  email: string | null = null;
  enabled: boolean = true;

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
