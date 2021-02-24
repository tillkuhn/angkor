import {Component, ElementRef, Input, OnInit, ViewChild} from '@angular/core';
import {FormArray, FormBuilder, FormControl, FormGroup, Validators} from '@angular/forms';
import {COMMA, ENTER} from '@angular/cdk/keycodes';
import {MatChipInputEvent} from '@angular/material/chips';
import {Observable} from 'rxjs';
import {map, startWith} from 'rxjs/operators';
import {MatAutocomplete, MatAutocompleteSelectedEvent} from '@angular/material/autocomplete';

/**
 * Simple form child
 * see https://stackblitz.com/edit/reactive-form-with-child-component?file=app%2Fmy-form-father%2Fmy-form-father.component.ts
 * THANKS!!!
 */
@Component({
  selector: 'app-tag-input',
  templateUrl: './tag-input.component.html',
  styleUrls: ['./tag-input.component.scss']
})
export class TagInputComponent implements OnInit {

  @Input() parentForm: FormGroup;
  @Input() tagSuggestions: string[];

  // props for tag chip support, https://stackoverflow.com/questions/52061184/input-material-chips-init-form-array
  selectable = true;
  removable = true;
  addOnBlur = true;
  readonly separatorKeysCodes: number[] = [ENTER, COMMA];

  // for autocomplete
  // tags: string[] = ['Lemon'];
  tagCtrl = new FormControl(); // this.formBuilder.array([]);
  filteredTags: Observable<string[]>;

  // tag input needs to correspond with  #tagInput template var
  @ViewChild('tagInput') tagInput: ElementRef<HTMLInputElement>;
  @ViewChild('auto') matAutocomplete: MatAutocomplete;

  constructor(private formBuilder: FormBuilder) {
  }

  ngOnInit() {
    this.parentForm.addControl('tags', this.formBuilder.array([]));
    this.filteredTags = this.tagCtrl.valueChanges.pipe(
      startWith(null as string), // https://github.com/ReactiveX/rxjs/issues/4772#issuecomment-496417283
      map((tag: string | null) => tag ? this.filter(tag) : this.tagSuggestions.slice()));
    // new FormControl('', Validators.required)
  }

  private filter(tag: string): string[] {
    const filterValue = tag.toLowerCase();
    return this.tagSuggestions.filter(potentialTag => potentialTag.toLowerCase().indexOf(filterValue) === 0);
  }

  // Triggered when tag is added from UI
  tagAddedDirectly(e: MatChipInputEvent) {
    const input = e.input;
    const value = e.value;
    this.pushNewTag(value);
  }

  // Triggered when added via autocomplete
  tagSelectedFromAutoComplete(event: MatAutocompleteSelectedEvent): void {
    // this.tags.push(event.option.viewValue);
    this.pushNewTag(event.option.viewValue);
  }

  // only if removable == true and remove action is triggered on an added tag
  removeTag(i: number) {
    const control = this.parentForm.controls.tags as FormArray;
    control.removeAt(i);
  }

  private pushNewTag(value: string) {
    if ((value || '').trim()) {
      const control = this.parentForm.controls.tags as FormArray;
      control.push(this.formBuilder.control(value.trim().toLowerCase()));
    }
    this.tagInput.nativeElement.value = '';
    this.tagCtrl.setValue(null);
  }


}
