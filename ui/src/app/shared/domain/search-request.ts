import {ListItem} from '@shared/domain/list-item';

export declare type SortDirection = 'ASC' | 'DESC';

export const defaultPageSize = 100;

export const sortDirections: ListItem[] = [
  {value: 'ASC', label: 'Asc', icon: 'arrow_downward'},
  {value: 'DESC', label: 'Desc', icon: 'arrow_upward'}
];

export class SearchRequest {
    query = '';
    page: 0;
    pageSize = defaultPageSize; // default
    sortDirection: SortDirection = 'ASC';
    sortProperties: string[] = ['name']; // should be null

    // todo support multiple, workaround to bind select box to first array element
    get primarySortProperty() {
        return this.sortProperties[0];
    }

    set primarySortProperty(sortProperty) {
        this.sortProperties[0] = sortProperty;
    }

    reverseSortDirection() {
        const currentOrder = this.sortDirection;
        this.sortDirection = currentOrder === 'ASC' ? 'DESC' : 'ASC';
    }

    sortDirectionSelectItems(): ListItem[] {
        return sortDirections;
    }

}
