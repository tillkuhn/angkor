export declare type EntityDialogAction = 'ADD' | 'VIEW' | 'EDIT';
export declare type EntityDialogResult = 'CREATED' | 'UPDATED' | 'DELETED' | 'UNCHANGED';

export class EntityDialog<E> {

  id?: string;
  item?: E;
  action: EntityDialogAction;
  result?: EntityDialogResult;

}

