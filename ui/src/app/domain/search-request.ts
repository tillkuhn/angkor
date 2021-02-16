
export declare type SortDirection = 'ASC' | 'DESC' ;

export interface SearchRequest {

  search: string;
  page: number;
  size: number;
  sortDirection: SortDirection;
  sortProperties: string[];

}

