import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import * as _ from 'lodash';

@Component({
  selector: 'app-photo-base64',
  templateUrl: './photo-base64.component.html',
  styleUrls: ['./photo-base64.component.scss']
})
export class PhotoBase64Component implements OnInit, OnChanges {

  @Input() placeHolder: string = '';
  @Input() boxWidth: string = '175';
  @Input() boxHeight: string = 'auto';
  @Input() imageSaved: string = '';
  @Input('isImageSaved') _isImageSaved: boolean = false;
  @Input() maxSize: number = 200000;
  @Input() removeLabel: string = 'Remove';
  @Input() fileTypes: string[] = ['image/png', 'image/jpeg'];
  @Input() asDataUrl: boolean = true;
  @Input() isEdit: boolean = true;

  @Output() imageLoaded: EventEmitter<any> = new EventEmitter();

  imageError!: string | null;
  cardImageBase64!: string | null;

  constructor() { }

  ngOnInit() {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.imageSaved && changes.imageSaved.currentValue) {
      this.cardImageBase64 = changes.imageSaved.currentValue;
    }
  }

  fileChangeEvent(fileInput: any) {
    this.imageError = null;
    if (fileInput.target.files && fileInput.target.files[0]) {
      const file_type = fileInput.target.files[0].type;
      // Size Filter Bytes
      const max_size = this.maxSize; // /* The size of the file in bytes. */
      const allowed_types = this.fileTypes;
      const max_height = 15200;
      const max_width = 25600;

      if (fileInput.target.files[0].size > max_size) {
        this.imageError = 'Maximum size allowed is ' + max_size / 1000 + 'Mb';
        return false;
      }

      if (!_.includes(allowed_types, fileInput.target.files[0].type)) {
        this.imageError = 'Only Images are allowed ( JPG | PNG )';
        return false;
      }
      const reader = new FileReader();
      reader.onload = (e: any) => {
        if (this.asDataUrl) {
          const image = new Image();
          image.src = e.target.result;
          image.onload = (rs: any) => {
            const img_height = rs.currentTarget['height'];
            const img_width = rs.currentTarget['width'];

            if (img_height > max_height && img_width > max_width) {
              this.imageError =
                'Maximum dimentions allowed ' +
                max_height +
                '*' +
                max_width +
                'px';
              return false;
            } else {
              const imgBase64Path = e.target.result;
              this.cardImageBase64 = imgBase64Path;
              this._isImageSaved = true;
              this.imageLoaded.emit(this.base64ToArrayBuffer(imgBase64Path.split(',')[1]));
              return true;
            }
          };
        } else {
          const imageData = e.target.result;
          // const int32View = new Int32Array(imageData);
          // let imageType = 'unknown';
          // switch(int32View[0]) {
          //   case 1196314761:
          //     imageType = "image/png";
          //     break;
          //   case 944130375:
          //     imageType = "image/gif";
          //     break;
          //   case 544099650:
          //     imageType = "image/bmp";
          //     break;
          //   case -520103681:
          //     imageType = "image/jpg";
          //     break;
          //   default:
          //     imageType= "unknown";
          //     break;
          // }
          const _base64 = this.arrayBufferToBase64(imageData);
          this.cardImageBase64 = `data:${file_type};base64,${_base64}`;
          this._isImageSaved = true;
          this.imageLoaded.emit(imageData);
        }
      };

      if (this.asDataUrl) {
        reader.readAsDataURL(fileInput.target.files[0]);
      } else {
        reader.readAsArrayBuffer(fileInput.target.files[0]);
      }
      return true;
    }
    return false;
  }

  removeImage() {
    this.cardImageBase64 = null;
    this._isImageSaved = false;
    this.imageError = null;
    this.imageLoaded.emit(null);
  }

  arrayBufferToBase64(buffer: any) {
    var binary = '';
    var bytes = new Uint8Array( buffer );
    var len = bytes.byteLength;
    for (var i = 0; i < len; i++) {
        binary += String.fromCharCode( bytes[ i ] );
    }
    return window.btoa( binary );
  }

  _base64ToArrayBuffer(base64Url: any) {
    let base64 = base64Url.replaceAll('-', '+');
    base64 = base64.replaceAll('_', '/');
    const binaryString = window.atob(base64);
    const length = binaryString.length;
    const bytes = new Uint8Array(length);
    for (let i = 0; i < length; i++) {
        bytes[i] = binaryString.charCodeAt(i);
    }
    return bytes.buffer;
  }

  _b64ToUint6(nChr: any) {
    return nChr > 64 && nChr < 91
      ? nChr - 65
      : nChr > 96 && nChr < 123
      ? nChr - 71
      : nChr > 47 && nChr < 58
      ? nChr + 4
      : nChr === 43
      ? 62
      : nChr === 47
      ? 63
      : 0;
  }

  base64ToArrayBuffer(sBase64: any, nBlocksSize: number = 1024) {
    const sB64Enc = sBase64.replace(/[^A-Za-z0-9+/]/g, "");
    const nInLen = sB64Enc.length;
    const nOutLen = nBlocksSize
      ? Math.ceil(((nInLen * 3 + 1) >> 2) / nBlocksSize) * nBlocksSize
      : (nInLen * 3 + 1) >> 2;
    const taBytes = new Uint8Array(nOutLen);
  
    let nMod3;
    let nMod4;
    let nUint24 = 0;
    let nOutIdx = 0;
    for (let nInIdx = 0; nInIdx < nInLen; nInIdx++) {
      nMod4 = nInIdx & 3;
      nUint24 |= this._b64ToUint6(sB64Enc.charCodeAt(nInIdx)) << (6 * (3 - nMod4));
      if (nMod4 === 3 || nInLen - nInIdx === 1) {
        nMod3 = 0;
        while (nMod3 < 3 && nOutIdx < nOutLen) {
          taBytes[nOutIdx] = (nUint24 >>> ((16 >>> nMod3) & 24)) & 255;
          nMod3++;
          nOutIdx++;
        }
        nUint24 = 0;
      }
    }
  
    return taBytes;
  }  
}

/** Other File Type
  'application/msword'
  'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
  'image/jpg'
  'image/jpeg'
  'application/pdf'
  'image/png'
  'application/vnd.ms-excel'
  'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
*/
