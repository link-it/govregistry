import { Directive, ElementRef, Input, OnInit, Renderer2 } from '@angular/core';

@Directive({
  selector: '[setBackgroundImage]'
})
export class SetBackgroundImageDirective implements OnInit {

  @Input() imageUrl!: string;
  @Input() position: string = 'contain';

  constructor(private elementRef:ElementRef, private renderer: Renderer2) {}

  ngOnInit() {
    this.renderer.setStyle(this.elementRef.nativeElement, 'backgroundImage', `url(${this.imageUrl})`);
    this.renderer.setStyle(this.elementRef.nativeElement, 'background-size', this.position);
    this.renderer.setStyle(this.elementRef.nativeElement, 'background-position', 'center');
    this.renderer.setStyle(this.elementRef.nativeElement, 'background-repeat', 'no-repeat');
  }
}
