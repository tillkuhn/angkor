// https://basarat.gitbook.io/typescript/type-system/index-signatures#typescript-index-signature
export interface ListItem {
  label: string;
  value: string;
  icon?: string; // check https://material.io/resources/icons/?style=baseline
  maki?: string; // optimized for mapbox, check https://labs.mapbox.com/maki-icons/
  emoji?: string; // https://emojipedia.org/search/
}
