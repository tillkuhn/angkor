export interface User {
  id?: any;
  login?: string;
  firstName?: string;
  lastName?: string;
  name?: string;
  email?: string;
  activated?: boolean;
  langKey?: string;
  authorities?: any[];
  createdBy?: string;
  createdDate?: Date;
  lastModifiedBy?: string;
  lastModifiedDate?: Date;
  password?: string;
}

// export declare type AuthScope = 'PUBLIC' | 'ALL_AUTH' | 'PRIVATE';
