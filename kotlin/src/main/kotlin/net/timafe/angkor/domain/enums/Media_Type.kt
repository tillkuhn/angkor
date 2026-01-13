package net.timafe.angkor.domain.enums

/**
 * CREATE TYPE media_type AS ENUM ( 'VIDEO','AUDIO','IMAGE','PDF','DEFAULT');
 * Source for icon keys: https://fonts.google.com/icons?selected=Material+Icons
 * See comment in Area_Level regarding the user of underscore in Enum class name.
 */
enum class Media_Type(
    val label: String,
    val icon: String?
) {
    DEFAULT("Other","link"),
    VIDEO("Video / Youtube URL","videocam"),
    FEED("RSS Feeds","rss_feed"),
    AUDIO("Audio Stream / MP3","music_note"),
    IMAGE("Images","photo_camera"),
    PDF("PDF Doc","picture_as_pdf"),
    KOMOOT_TOUR("Komoot Tour","tour"),
    BLOG_ARTICLE("Blog Article","feed");
}
