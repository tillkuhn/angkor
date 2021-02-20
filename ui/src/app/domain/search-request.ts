import {ListItem} from './list-item';


export const sortDirections: ListItem[] = [
  {value: 'ASC', label: 'Asc', icon: 'arrow_downward'},
  {value: 'DESC', label: 'Desc', icon: 'arrow_upward'}
];
export const defaultPageSize = 100;
export declare type SortDirection = 'ASC' | 'DESC' ;

export class SearchRequest {
  query = '';
  page: 0;
  pageSize = defaultPageSize; // default
  sortDirection: SortDirection = 'ASC';
  sortProperties: string[] = ['name'];

  // todo support multiple, workaround to bind select box to first array element
  get primarySortProperty() {
    return this.sortProperties[0];
  }

  set primarySortProperty(sortProperty) {
    this.sortProperties[0] = sortProperty;
  }

  reverseSortOrder() {
    const currentOrder = this.sortDirection;
    this.sortDirection = currentOrder === 'ASC' ? 'DESC' : 'ASC';
  }

  sortDirections(): ListItem[] {
    return sortDirections;
  }

}

