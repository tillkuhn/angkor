import {Component, OnInit} from '@angular/core';
import {MatTreeNestedDataSource} from '@angular/material/tree';
import {NestedTreeControl} from '@angular/cdk/tree';
import {AreaNode} from '../domain/area-node';
import {NGXLogger} from 'ngx-logger';
import {ApiService} from '../shared/api.service';


const TREE_DATA: AreaNode[] = [
  {
    'id': 'europe',
    'parentId': 'earth',
    'value': 'Europe',
    'children': [
      {
        'id': 'eu-scan',
        'parentId': 'europe',
        'value': 'Scandinavia',
        'children': []
      },
      {
        'id': 'it',
        'parentId': 'europe',
        'value': 'Italy',
        'children': []
      }
    ]
  }

];

@Component({
  selector: 'app-area-tree',
  templateUrl: './area-tree.component.html',
  styleUrls: ['./area-tree.component.scss']
})
export class AreaTreeComponent implements OnInit {

  treeControl = new NestedTreeControl<AreaNode>(node => node.children);
  dataSource = new MatTreeNestedDataSource<AreaNode>();

  constructor(private logger: NGXLogger,
              private api: ApiService) {
  }

  ngOnInit(): void {
    this.api.getAreaTree().subscribe(
      data => this.dataSource.data = data
    );
  }


  hasChild = (_: number, node: AreaNode) => !!node.children && node.children.length > 0;

}
