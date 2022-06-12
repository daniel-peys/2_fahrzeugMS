package com.acme.fahrzeug.entity

import net.minidev.json.annotate.JsonIgnore
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table

/**
 * Fahrzeughalterdaten für die Anwendungslogik und zum Abspeichern in der DB.
 * @property id Generierte UUID
 * @property vorname Der Vorname als unveränderliches Pflichtfeld.
 * @property nachname Der Nachname unveränderliches Pflichtfeld.
 * @constructor Erzeugt ein Objekt mit Vorname und Nachname
 */
@Entity
@Table(name = "fahrzeughalter")
data class Fahrzeughalter(
    @Id
    // https://docs.jboss.org/hibernate/orm/5.6/userguide/html_single/Hibernate_User_Guide.html#identifiers-generators-uuid
    @GeneratedValue
    // Oracle: https://stackoverflow.com/questions/50003906/storing-uuid-as-string-in-mysql-using-jpa
    // @GeneratedValue(generator = "UUID")
    // @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    // @Column(columnDefinition = "char(36)")
    // @Type(type = "org.hibernate.type.UUIDCharType")
    @JsonIgnore
    val id: DbId? = null,

    val vorname: String = "",

    val nachname: String = "",
)
