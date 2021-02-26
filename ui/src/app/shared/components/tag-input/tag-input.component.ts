import {Component, ElementRef, Input, OnInit, ViewChild} from '@angular/core';
import {FormArray, FormBuilder, FormControl, FormGroup} from '@angular/forms';
import {COMMA, ENTER} from '@angular/cdk/keycodes';
import {MatChipInputEvent} from '@angular/material/chips';
import {Observable} from 'rxjs';
import {map, startWith, tap} from 'rxjs/operators';
import {MatAutocomplete, MatAutocompleteSelectedEvent} from '@angular/material/autocomplete';
import {EntityType} from '../../../domain/entities';
import {NGXLogger} from 'ngx-logger';
import {TagService} from './tag.service';

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
  @Input() entityType: EntityType;
  @Input() parentFormTagsControlName = 'tags';

  // props for tag chip support, https://stackoverflow.com/questions/52061184/input-material-chips-init-form-array
  selectable = true;
  removable = true;
  addOnBlur = true;
  readonly separatorKeysCodes: number[] = [ENTER, COMMA];

  // for autocomplete
  // tags: string[] = ['Lemon'];
  tagCtrl = new FormControl(); // this.formBuilder.array([]);
  filteredTags: Observable<string[]>;
  tagSuggestions: string[] = [];

  // tag input needs to correspond with  #tagInput template var
  @ViewChild('tagInput') tagInput: ElementRef<HTMLInputElement>;
  @ViewChild('auto') matAutocomplete: MatAutocomplete;

  constructor(private formBuilder: FormBuilder,
              private tagService: TagService,
              private logger: NGXLogger) {
  }

  ngOnInit() {
    this.tagService.entityTags(this.entityType).pipe(
      tap<string[]>( tags => this.logger.info(`loaded ${tags.length} tags for entity ${this.entityType}`))
    ).subscribe( tags => {
      this.tagSuggestions = tags;
    });
    // mock: of(['watch', 'important', 'listen', 'place', 'dish', 'komoot']);
    // tagSuggestion$: Observable<string[]>; // = this.tagSe//of(['watch', 'important', 'listen', 'place', 'dish', 'komoot']);

    // reuse control "parentFormTagsControlName" if present in parent form, otherwise create
    if (this.parentForm.get(this.parentFormTagsControlName) == null) {
      this.logger.warn(`${this.parentFormTagsControlName} not found in parent form, adding empty array` );
      this.parentForm.addControl(this.parentFormTagsControlName, this.formBuilder.array([]));
    }

    this.filteredTags = this.tagCtrl.valueChanges.pipe(
      startWith(null as string), // cast: https://github.com/ReactiveX/rxjs/issues/4772#issuecomment-496417283
      map((tagInput: string | null) => {
        // this.logger.info(`filter ${tag}`);
        // tagInput contains as-you-type string (e.g. tra ... to be completed to travel)
        return tagInput ? this.filter(tagInput) : this.tagSuggestions.slice();
      }));
  }

  private filter(tag: string): string[] {
    const filterValue = tag.toLowerCase();
    return this.tagSuggestions.filter(potentialTag => potentialTag.toLowerCase().indexOf(filterValue) === 0);
  }

  // Triggered when tag is added from UI
  tagAddedDirectly(e: MatChipInputEvent) {
    // const input = e.input;
    const value = e.value;
    this.pushNewTag(value);
  }

  // Triggered when added via autocomplete
  tagSelectedFromAutoComplete(event: MatAutocompleteSelectedEvent): void {
    this.pushNewTag(event.option.viewValue);
  }

  // only if removable == true and remove action is triggered on an added tag
  removeTag(i: number) {
    const control = this.parentForm.get(this.parentFormTagsControlName) as FormArray;
    control.removeAt(i);
  }

  private pushNewTag(value: string) {
    // take only anything before the first blank
    const trimmedVal = (value || '').trim().toLocaleLowerCase().split(' ')[0];
    if (trimmedVal) {
      // this.logger.info(`pushing ${trimmedVal}`);
      const control = this.parentForm.get(this.parentFormTagsControlName) as FormArray;
      control.push(this.formBuilder.control(trimmedVal));
    }
    this.tagInput.nativeElement.value = '';
    this.tagCtrl.setValue(null);
  }

}
