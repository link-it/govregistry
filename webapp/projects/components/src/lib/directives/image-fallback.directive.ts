import { Directive, ElementRef, HostListener, Input } from "@angular/core";

@Directive({
  selector: "img[appImgFallback]",
})
export class ImgFallbackDirective {

  @Input() appImgFallback!: string

  constructor(private el: ElementRef) {}

  @HostListener("error")
  private loadFallBackOnError() {
    const element: HTMLImageElement = <HTMLImageElement>this.el.nativeElement;
    element.src = this.appImgFallback || 'https://via.placeholder.com/200';
  }
}
