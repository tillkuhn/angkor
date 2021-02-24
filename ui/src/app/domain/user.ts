export interface User {
  id?: string;
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
  id: string;
  shortname: string;
  emoji: string;
  initials: string;
}

// export declare type AuthScope = 'PUBLIC' | 'ALL_AUTH' | 'PRIVATE';
