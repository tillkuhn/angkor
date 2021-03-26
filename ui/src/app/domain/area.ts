import {ManagedEntity} from '@shared/domain/entities';
import {POI} from '@domain/poi';

export interface GenericArea {
  code: string;
  name: string;
  parentCode: string;
  level: string;
  adjectival?: string;
  coordinates?: number[];
}

// Interface used all across the UI
export interface Area extends GenericArea, ManagedEntity, POI {
  id?: string; // Satisfy ManagedEntity interface, to be refactored
}

// For API we don't need specific interface at the moment, GenericArea is OK
export interface AreaNode {
  id: string;
  parentId: string;
  value: string;
  children?: AreaNode[];
}
