package net.timafe.angkor.web.vm

// UI Representation
//export interface ListItem {
//    label: string;
//    value: string;
//    icon?: string; // check https://material.io/resources/icons/?style=baseline
//    maki?: string; // optimized for mapbox, check https://labs.mapbox.com/maki-icons/
//    emoji?: string; // https://emojipedia.org/search/
//}

/**
 * ListItem ViewModel, currently only used for Media Types
 * Potentially obsolete in Backend
 */
data class ListItem(
    val value: String,
    val label: String,
    val icon: String?
)
