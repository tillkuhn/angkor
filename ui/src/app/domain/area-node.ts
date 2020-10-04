export interface AreaNode {
  id: string;
  parentId: string;
  value: string;
  children?: AreaNode[];
}
