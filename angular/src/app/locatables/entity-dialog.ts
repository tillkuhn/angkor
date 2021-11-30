export declare type EntityDialogMode = 'Add' | 'View' | 'Edit';
export declare type EntityDialogResult = 'Added' | 'Updated' | 'Deleted' | 'Canceled';

export class EntityDialogRequest {

  id?: string;
  mode: EntityDialogMode = 'View';

}

export class EntityDialogResponse<E> {

  entity?: E;
  result: EntityDialogResult;

}
