import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { HideMissingDirective } from './hide-missing.directive';

@NgModule({
  declarations: [
    HideMissingDirective
  ],
  imports: [
    CommonModule
  ],
  exports: [
    HideMissingDirective
  ]
})
export class HideMissingModule { }
