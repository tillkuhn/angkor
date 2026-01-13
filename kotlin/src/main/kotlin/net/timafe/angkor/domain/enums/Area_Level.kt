package net.timafe.angkor.domain.enums

// Use underscore Area_Level in Class name since as opposed to the classic PostgreSQLEnumType, the bew
// Hibernate build-in PostgreSQLEnumJdbcType does not translate AreaLevel (kotlin) to area_level (postgres)
enum class Area_Level {
    PLANET,
    CONTINENT,
    CONT_SECT,
    COUNTRY,
    REGION
}
