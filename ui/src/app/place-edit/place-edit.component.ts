import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { ApiService } from '../api.service';
import { FormControl, FormGroupDirective, FormBuilder, FormGroup, NgForm, Validators } from '@angular/forms';
import { ErrorStateMatcher } from '@angular/material/core';

export class MyErrorStateMatcher implements ErrorStateMatcher {
  isErrorState(control: FormControl | null, form: FormGroupDirective | NgForm | null): boolean {
    const isSubmitted = form && form.submitted;
    return !!(control && control.invalid && (control.dirty || control.touched || isSubmitted));
  }
}

@Component({
  selector: 'app-place-edit',
  templateUrl: './place-edit.component.html',
  styleUrls: ['./place-edit.component.scss']
})
export class PlaceEditComponent implements OnInit {

  placeForm: FormGroup;
  id = '';
  name = '';
  summary = '';
  country = '';
  imageUrl = '';
  isLoadingResults = false;
  matcher = new MyErrorStateMatcher();

  constructor(private router: Router, private route: ActivatedRoute, private api: ApiService, private formBuilder: FormBuilder) { }

  ngOnInit() {
    this.getPlace(this.route.snapshot.params.id);
    this.placeForm = this.formBuilder.group({
      name : [null, Validators.required],
      summary : [null, Validators.required],
      country : [null, Validators.required],
      imageUrl : [null, Validators.required]
    });
  }

  getPlace(id: any) {
    this.api.getPlace(id).subscribe((data: any) => {
      this.id = data.id;
      this.placeForm.setValue({
        name: data.name,
        summary: data.summary,
        country: data.country,
        imageUrl: data.imageUrl
      });
    });
  }

  onFormSubmit() {
    this.isLoadingResults = true;
    this.api.updatePlace(this.id, this.placeForm.value)
      .subscribe((res: any) => {
          const id = res.id;
          this.isLoadingResults = false;
          this.router.navigate(['/place-details', id]);
        }, (err: any) => {
          console.log(err);
          this.isLoadingResults = false;
        }
      );
  }

  placeDetails() {
    this.router.navigate(['/place-details', this.id]);
  }

}
