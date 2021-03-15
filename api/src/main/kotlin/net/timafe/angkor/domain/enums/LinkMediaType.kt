package net.timafe.angkor.domain.enums

/**
 * CREATE TYPE media_type AS ENUM ( 'VIDEO','AUDIO','IMAGE','PDF','DEFAULT');
 */
enum class LinkMediaType {
    DEFAULT, VIDEO, FEED, AUDIO, IMAGE, PDF;
}
