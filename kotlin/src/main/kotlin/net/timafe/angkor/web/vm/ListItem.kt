package net.timafe.angkor.web.vm

// UI Represenation
//export interface ListItem {
//    label: string;
//    value: string;
//    icon?: string; // check https://material.io/resources/icons/?style=baseline
//    maki?: string; // optimized for mapbox, check https://labs.mapbox.com/maki-icons/
//    emoji?: string; // https://emojipedia.org/search/
//}
data class ListItem(
    val value: String,
    val label: String,
    val icon: String?
)
