import {COMMA, ENTER} from '@angular/cdk/keycodes';
import {Component, ElementRef, Input, OnInit, ViewChild} from '@angular/core';
import {EntityType} from '../../domain/entities';
import {UntypedFormArray, UntypedFormBuilder, UntypedFormControl, UntypedFormGroup} from '@angular/forms';
import {MatLegacyAutocomplete as MatAutocomplete, MatLegacyAutocompleteSelectedEvent as MatAutocompleteSelectedEvent} from '@angular/material/legacy-autocomplete';
import {MatLegacyChipInputEvent as MatChipInputEvent} from '@angular/material/legacy-chips';
import {NGXLogger} from 'ngx-logger';
import {Observable} from 'rxjs';
import {TagService} from './tag.service';
import {map, startWith, tap} from 'rxjs/operators';

/**
 * TagInputComponent, example of a simple (:-)) form child
 *
 * Thanks
 * Original Angular Material Chip Input Example:
 * https://material.angular.io/components/chips/examples
 * https://stackblitz.com/edit/reactive-form-with-child-component?file=app%2Fmy-form-father%2Fmy-form-father.component.ts
 *
 */
@Component({
  selector: 'app-tag-input',
  templateUrl: './tag-input.component.html',
  styleUrls: ['./tag-input.component.scss']
})
export class TagInputComponent implements OnInit {

  @Input() parentForm: UntypedFormGroup;
  @Input() parentFormTagsControlName = 'tags';
  @Input() entityType: EntityType;

  // tag input needs to correspond with #tagInput template var
  @ViewChild('tagInput') tagInput: ElementRef<HTMLInputElement>;
  @ViewChild('auto') matAutocomplete: MatAutocomplete;

  // props for tag chip support, https://stackoverflow.com/questions/52061184/input-material-chips-init-form-array
  readonly separatorKeysCodes: number[] = [ENTER, COMMA];
  readonly tagInputCtl = new UntypedFormControl();
  selectable = true;
  removable = true;
  addOnBlur = true;
  filteredTags: Observable<string[]>;
  tagSuggestions: string[] = [];

  constructor(private formBuilder: UntypedFormBuilder,
              private tagService: TagService,
              private logger: NGXLogger) {
  }

  ngOnInit() {
    this.tagService.entityTags(this.entityType).pipe(
      tap<string[]>(tags => this.logger.info(`TagInputComponent.ngOnInit: loaded ${tags.length} tags for entity ${this.entityType}`))
    ).subscribe(tags => {
      this.tagSuggestions = tags;
    });
    // mock: of(['watch', 'important', 'listen', 'place', 'dish', 'komoot']);
    // tagSuggestion$: Observable<string[]>; // = this.tagSe//of(['watch', 'important', 'listen', 'place', 'dish', 'komoot']);

    // reuse control "parentFormTagsControlName" if present in parent form (see @Input), otherwise create it
    if (this.parentForm.get(this.parentFormTagsControlName) == null) {
      this.logger.warn(`${this.parentFormTagsControlName} not found in parent form, adding empty array`);
      this.parentForm.addControl(this.parentFormTagsControlName, this.formBuilder.array([]));
    }

    this.filteredTags = this.tagInputCtl.valueChanges.pipe(
      startWith(null as string), // cast: https://github.com/ReactiveX/rxjs/issues/4772#issuecomment-496417283
      map((tagInput: string | null) => {
        // tagInput contains the as-you-type string (e.g. "tra ..." to be completed to "travel")
        // Annoying: If field is empty on enter, we display ALL choices
        // Instead we start with  use [] and start displaying only if at least one char was typed in
        return tagInput ? this.filter(tagInput) : this.tagSuggestions.slice();
      }));
  }

  /** Triggered / Emitted when a chip is to be added (matChipInputTokenEnd)="addChip($event)" */
  addTag(e: MatChipInputEvent) {
    const value = TagInputComponent.trimTagValue(e.value);

    // Add our Tag
    if (value) {
      const tagsCtl = this.parentForm.get(this.parentFormTagsControlName) as UntypedFormArray;
      tagsCtl.push(this.formBuilder.control(value));
    }

    // Clear the input value
    e.chipInput!.clear();
    this.tagInputCtl.setValue(null);
  }

  /** Triggered when added via autocomplete */
  tagSelected(e: MatAutocompleteSelectedEvent): void {
    // this.pushNewTag(event.option.viewValue);
    const tagsCtl = this.parentForm.get(this.parentFormTagsControlName) as UntypedFormArray;
    const value = TagInputComponent.trimTagValue(e.option.viewValue);
    tagsCtl.push(this.formBuilder.control(value));

    this.tagInput.nativeElement.value = '';
    this.tagInputCtl.setValue(null);
  }

  /** only if removable == true and remove action is triggered on an added tag */
  removeTag(i: number) {
    const tagsCtl = this.parentForm.get(this.parentFormTagsControlName) as UntypedFormArray;
    this.logger.info(`remove tag at index ${i} current Size ${tagsCtl.length}`);
    tagsCtl.removeAt(i);
  }

  private filter(tag: string): string[] {
    const filterValue = tag.toLowerCase();
    return this.tagSuggestions.filter(potentialTag => potentialTag.toLowerCase().indexOf(filterValue) === 0);
  }

  private static trimTagValue(value: string) {
    // Value contains number of occurrences e.g. "travel (123)" so we only return the first part using blank separator
    return (value || '').trim().toLocaleLowerCase().split(' ')[0];
  }

  // private pushNewTag(value: string) {
  //   // take only anything before the first blank
  //   const trimmedVal = (value || '').trim().toLocaleLowerCase().split(' ')[0];
  //   if (trimmedVal) {
  //     // this.logger.info(`pushing ${trimmedVal}`);
  //     const control = this.parentForm.get(this.parentFormTagsControlName) as FormArray;
  //     control.push(this.formBuilder.control(trimmedVal));
  //   }
  //   this.tagInput.nativeElement.value = '';
  //   this.tagCtrl.setValue(null);
  // }

}
