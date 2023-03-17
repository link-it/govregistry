import { Injectable } from '@angular/core';
import { HttpHeaders } from '@angular/common/http';
import { Observable, of } from 'rxjs';

import { ApiClient, IRequestOptions, IRawRequestOptions } from './api.client';
import { environment } from '../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class OpenAPIService {

  private proxyPath: string;

  constructor( private http: ApiClient) 
  {
    this.proxyPath = "/"
  }

  getList(name: string, options?: IRequestOptions, pageUrl: string = '') : Observable<any> {
    let url = `${this.proxyPath}${name}`;

    // gestione delle chiamate url per ngx-infinite-scroll o per la paginazione
    if (pageUrl) {
      url = pageUrl;
      if (!environment.production) {
        let path = pageUrl.replace(/^[^:]+:\/\/[^/?#]+.+?(?=\?)/, '');
        url = `${this.proxyPath}${name}${path}`;
      }
    }
    
    switch (name) {
      case "others":
        return of(null); 
        break;

      default:
        return this.http.get<any>(url, options);
    }
    
  }

  getDetails(name: string, id: any, type?: string, options?: IRequestOptions) {
    if(!options) options = {};
    
    const _url = type ? `${this.proxyPath}${name}/${id}/${type}` : `${this.proxyPath}${name}/${id}`;
    return this.http.get<any>(_url, options);
  }

  saveElement(name: string, body: Object, options?: IRequestOptions) {
    if(!options) options = {};
    
    const _url = `${this.proxyPath}${name}`;
    return this.http.post<any>(_url, body, options);
  }

  updateElement(name: string, id: any, body: Object, options?: IRequestOptions) {
    if(!options) options = {};
    options.headers = new HttpHeaders();
    options.headers = options.headers.set('content-type', 'application/json-patch+json');

    const _url = `${this.proxyPath}${name}/${id}`;
    return this.http.patch<any>(_url, body, options);
  }

  uploadImage(name: string, id: any, type: string, body: Object, options?: IRequestOptions) {
    if (!options) options = { };
    options.headers = new HttpHeaders();
    options.headers = options.headers.set('content-type', 'image/png');

    const _url = `${this.proxyPath}${name}/${id}/${type}`;
    return this.http.putRaw<any>(_url, body, options);
  }

  deleteElement(name: string, id: any, options?: IRequestOptions) {
    if(!options) options = {};
    
    const _url = `${this.proxyPath}${name}/${id}`;
    return this.http.delete<any>(_url, options);
  }

  getImageUrl(imageUrl: string) {
    let url = `${this.proxyPath}${name}`;

    if (!environment.production) {
      const api_url = this.http.getApiUrl();
      let path = imageUrl.split('/api/');
      url = `${api_url}${this.proxyPath}${name}${path[1]}`;
    }

    return url;
  }
}
