
export declare type SortDirection = 'ASC' | 'DESC' ;

export interface SearchRequest {

  query: string;
  page: number;
  pageSize: number;
  sortDirection: SortDirection;
  sortProperties: string[];

}

