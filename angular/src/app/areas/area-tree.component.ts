import {Component, OnInit} from '@angular/core';
import {MatTreeNestedDataSource} from '@angular/material/tree';
import {NestedTreeControl} from '@angular/cdk/tree';
import {NGXLogger} from 'ngx-logger';
import {MatSnackBar} from '@angular/material/snack-bar';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {DefaultErrorStateMatcher} from '@shared/helpers/form-helper';
import {AreaStoreService} from './area-store.service';
import {AreaNode} from '@domain/area';
import {ListItem} from '@shared/domain/list-item';

@Component({
  selector: 'app-area-tree',
  templateUrl: './area-tree.component.html',
  styleUrls: ['./area-tree.component.scss', '../shared/components/common.component.scss' ]
})
export class AreaTreeComponent implements OnInit {

  treeControl = new NestedTreeControl<AreaNode>(node => node.children);
  dataSource = new MatTreeNestedDataSource<AreaNode>();

  formData: FormGroup;
  matcher = new DefaultErrorStateMatcher();
  areaLevels: ListItem[] = [
    {value: 'REGION', label: 'Region'},
    {value: 'COUNTRY', label: 'Country'},
    {value: 'CONT_SEC', label: 'Continent Section'},
    {value: 'CONTINENT', label: 'Continent'}
  ];

  constructor(private logger: NGXLogger,
              private snackBar: MatSnackBar,
              private formBuilder: FormBuilder,
              private store: AreaStoreService) {
  }

  ngOnInit(): void {
    this.formData = this.formBuilder.group({
      code: [null, Validators.required],
      name: [null, Validators.required],
      parentCode: [null, Validators.required],
      level: ['COUNTRY', Validators.required]
    });
    this.store.getAreaTree().subscribe(
      data => this.dataSource.data = data
    );
  }

  onFormSubmit() {
    // this.newItemForm.patchValue({tags: ['new']});
    this.store.addArea(this.formData.value)
      .subscribe((res: any) => {
        const code = res.code;
        this.snackBar.open('Area code saved with id ' + code, 'Close', {
          duration: 2000,
        });
        this.ngOnInit(); // reload tree, clear form
      }, (err: any) => {
        this.logger.error(err);
      });
  }

  onAddClick(nodeId: string) {
    this.logger.info('Adding sub node below ' + nodeId);
    this.formData.patchValue({ parentCode: nodeId });
    let defaultLevel = 'COUNTRY';
    if (nodeId?.length === 2) {
      defaultLevel = 'REGION';
    }
    this.formData.patchValue( {level: defaultLevel});
  }

  hasChild = (_: number, node: AreaNode) => !!node.children && node.children.length > 0;

}
