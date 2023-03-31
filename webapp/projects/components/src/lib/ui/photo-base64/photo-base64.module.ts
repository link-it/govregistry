import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { HideMissingModule } from '../../directives/hide-missing.module';
import { ImgFallbackModule } from '../../directives/image-fallback.module';

import { PhotoBase64Component } from './photo-base64.component';

@NgModule({
  declarations: [
    PhotoBase64Component
  ],
  imports: [
    CommonModule,
    HideMissingModule,
    ImgFallbackModule
  ],
  exports: [
    PhotoBase64Component
  ]
})
export class PhotoBase64Module { }
