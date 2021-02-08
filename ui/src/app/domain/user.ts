export interface User {
  id?: any;
  login?: string;
  firstName?: string;
  lastName?: string;
  name?: string;
  email?: string;
  imageUrl?: string;
  roles?: string[];
  createdAt?: Date;
  updatedAt?: Date;
  emoji: string;
}

export interface UserSummary {
  id: any;
  shortname: string;
  emoji: string;
}

// export declare type AuthScope = 'PUBLIC' | 'ALL_AUTH' | 'PRIVATE';
