import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AlphanumericOnlyDirective } from './alphanumeric-only.directive';

@NgModule({
  declarations: [
    AlphanumericOnlyDirective
  ],
  imports: [
    CommonModule
  ],
  exports: [
    AlphanumericOnlyDirective
  ]
})
export class AlphanumericOnlyModule { }
