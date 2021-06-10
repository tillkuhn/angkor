export interface Metric {

  name: string;
  description?: string;
  value: number | string;
  baseUnit?: string;
}
