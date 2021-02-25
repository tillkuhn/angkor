// Same props for API and UI Entity

interface Tag {
  label: string;
}


export interface TagSummary extends Tag {
  count: number;
}
