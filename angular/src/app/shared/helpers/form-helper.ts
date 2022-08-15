import {ErrorStateMatcher} from '@angular/material/core';
import {UntypedFormControl, FormGroupDirective, NgForm} from '@angular/forms';

export class DefaultErrorStateMatcher implements ErrorStateMatcher {

  isErrorState(control: UntypedFormControl | null, form: FormGroupDirective | NgForm | null): boolean {
    const isSubmitted = form && form.submitted;
    // The !! ensures the resulting type is a boolean (true or false). shorter than $bla != null ? true : false.
    return !!(control && control.invalid && (control.dirty || control.touched || isSubmitted));
  }

}
