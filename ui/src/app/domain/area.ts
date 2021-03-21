import {ManagedEntity} from './entities';
import {GenericLink} from './link';

export interface GenericArea {
  code: string;
  name: string;
  parentCode: string;
  level: string;
  adjectival?: string;
  coordinates?: number[];
}

export interface Area extends GenericArea, ManagedEntity {
  id?: string; // Satisfy ManagedEntity interface, to be refactored
}

