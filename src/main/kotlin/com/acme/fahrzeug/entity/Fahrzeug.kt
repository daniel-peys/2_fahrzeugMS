package com.acme.fahrzeug.entity

import com.acme.fahrzeug.service.FahrzeugValidator.Companion.TIMEZONE_BERLIN
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalDateTime.now
import java.time.ZoneId
import java.util.UUID
import javax.persistence.CascadeType.PERSIST
import javax.persistence.CascadeType.REMOVE
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.NamedQuery
import javax.persistence.OneToOne
import javax.persistence.Table
import javax.persistence.Version

// https://docs.jboss.org/hibernate/orm/5.6/userguide/html_single/Hibernate_User_Guide.html
// https://medium.com/swlh/defining-jpa-hibernate-entities-in-kotlin-1ff8ee470805

/**
 * Unveränderliche Daten eines Fahrzeuges. In DDD ist Fahrzeug ist ein _Aggregate Root_.
 *
 * ![Klassendiagramm](../../../images/Fahrzeug.svg)
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@h-ka.de)
 *
 * @property id ID eines Fahrzeuges als UUID.
 * @property version Versionsnummer in der DB
 * @property beschreibung Gültiger Beschreibung eines Fahrzeugs, d.h. mit einem geeigneten Muster.
 * @property kennzeichen Kennzeichen eines Fahrzeuges.
 * @property kilometerstand Kilometerstand eines Fahrzeuges
 * @property erstzulassung Die Erstzulassung eines Fahrzeuges
 * @property fahrzeugtyp Der Fahrzeugtyp eines Fahrzeuges.
 * @property fahrzeughalter Die Fahrzeughalter eines Fahrzeugs
 * @property username Der Username bzw. Loginname eines Fahrzeuges.
 */
@Entity
@Table(name = "fahrzeug")
@NamedQuery(
    name = Fahrzeug.ALL,
    query = "SELECT k FROM Fahrzeug k",
)
@NamedQuery(
    name = Fahrzeug.BY_KENNZEICHEN,
    query = """
        SELECT f
        FROM  Fahrzeug f
        WHERE f.kennzeichen = :${Fahrzeug.PARAM_KENNZEICHEN}
    """,
)
@NamedQuery(
    name = Fahrzeug.KENNZEICHEN_EXISTS,
    query = """
        SELECT COUNT(*)
        FROM  Fahrzeug k
        WHERE k.kennzeichen = :${Fahrzeug.PARAM_KENNZEICHEN}
    """,
)
@NamedQuery(
    name = Fahrzeug.BY_BESCHREIBUNG,
    query = """
        SELECT k
        FROM  Fahrzeug k
        WHERE k.beschreibung LIKE :${Fahrzeug.PARAM_BESCHREIBUNG}
    """,
)
@NamedQuery(
    name = Fahrzeug.BESCHREIBUNG_PREFIX,
    query = """
        SELECT DISTINCT k.beschreibung
        FROM  Fahrzeug k
        WHERE k.beschreibung LIKE :${Fahrzeug.PARAM_BESCHREIBUNG}
    """,
)
// "var" fuer Properties, weil JPA fuer "mutable" entworfen wurde :-(
// https://www.jpa-buddy.com/blog/best-practices-and-common-pitfalls/#rules-for-jpa-entities
// https://vladmihalcea.com/immutable-entity-jpa-hibernate
@Suppress("DataClassShouldBeImmutable")
data class Fahrzeug(
    @Id
    // https://docs.jboss.org/hibernate/orm/5.6/userguide/html_single/Hibernate_User_Guide.html#identifiers-generators-uuid
    @GeneratedValue
    // Oracle: https://stackoverflow.com/questions/50003906/storing-uuid-as-string-in-mysql-using-jpa
    // @GeneratedValue(generator = "UUID")
    // @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    // @Column(columnDefinition = "char(36)")
    // @Type(type = "org.hibernate.type.UUIDCharType")
    @Suppress("experimental:annotation-spacing")
    val id: FahrzeugId? = null,

    // https://docs.jboss.org/hibernate/orm/5.6/userguide/html_single/Hibernate_User_Guide.html#locking-optimistic-mapping
    @Version
    val version: Int = 0,

    var beschreibung: String,

    var kennzeichen: String,

    var kilometerstand: Int = 0,

    var erstzulassung: LocalDate?,

    @Convert(converter = FahrzeugTypeConverter::class)
    var fahrzeugtyp: FahrzeugType? = null,

    @OneToOne(optional = false, cascade = [PERSIST, REMOVE])
    @JoinColumn(updatable = true)
    var fahrzeughalter: Fahrzeughalter,

    var username: String,

    // https://docs.jboss.org/hibernate/orm/5.6/userguide/html_single/Hibernate_User_Guide.html#mapping-generated-CreationTimestamp
    @CreationTimestamp
    @Suppress("UnusedPrivateMember")
    private val erzeugt: LocalDateTime = now(ZoneId.of(TIMEZONE_BERLIN)),

    // https://docs.jboss.org/hibernate/orm/5.6/userguide/html_single/Hibernate_User_Guide.html#mapping-generated-UpdateTimestamp
    @UpdateTimestamp
    @Suppress("UnusedPrivateMember")
    private val aktualisiert: LocalDateTime = now(ZoneId.of(TIMEZONE_BERLIN)),
) {
    /**
     * Vergleich mit einem anderen Objekt oder null.
     * https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier
     * https://vladmihalcea.com/the-best-way-to-implement-equals-hashcode-and-tostring-with-jpa-and-hibernate
     * https://thorben-janssen.com/ultimate-guide-to-implementing-equals-and-hashcode-with-hibernate
     *
     * @param other Das zu vergleichende Objekt oder null
     * @return True, falls das zu vergleichende (Fahrzeug-) Objekt das gleiche Kennzeichen hat.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Fahrzeug
        return kennzeichen != null && kennzeichen == other.kennzeichen
    }

    /**
     * Hashwert aufgrund des Kennzeichen.
     * @return Der Hashwert.
     */
    override fun hashCode() = kennzeichen?.hashCode() ?: this::class.hashCode()

    /**
     * Properties überschreiben, z.B. bei PUT-Requests von der REST-Schnittstelle
     * @param neu Ein transientes FahrzeugObjekt mit den neuen Werten für die Properties
     */
    @Suppress("DataClassContainsFunctions")
    fun set(neu: Fahrzeug) {
        beschreibung = neu.beschreibung
        kennzeichen = neu.kennzeichen
        kilometerstand = neu.kilometerstand
        erstzulassung = neu.erstzulassung
        fahrzeugtyp = neu.fahrzeugtyp
        fahrzeughalter = neu.fahrzeughalter
    }

    /**
     * Konstante für Named Queries
     */
    companion object {
        private const val PREFIX = "Fahrzeug."

        /**
         * Name für die Named Query, mit der alle Fahrzeuge gesucht werden
         */
        const val ALL = "${PREFIX}all"

        /**
         * Name für die Named Query, mit der Fahrzeuge anhand der Emailadresse gesucht werden
         */
        const val BY_KENNZEICHEN = "${PREFIX}byKennzeichen"

        /**
         * Name für die Named Query, mit der ermittelt wird, ob es zu einem Kennzeichen bereits einen Fahrzeuge gibt
         */
        const val KENNZEICHEN_EXISTS = "${PREFIX}kennzeichenExists"

        /**
         * Name für die Named Query, mit der Fahrzeug anhand eines Teilstrings für den Nachnamen gesucht werden
         */
        const val BY_BESCHREIBUNG = "${PREFIX}byBeschreibung"

        /**
         * Name für die Named Query, mit der die Nachnamen zu einem Teilstring gesucht werden
         */
        const val BESCHREIBUNG_PREFIX = "${PREFIX}beschreibungPrefix"

        /**
         * Parametername für das Kennzeichen
         */
        const val PARAM_KENNZEICHEN = "kennzeichen"

        /**
         * Parametername für die Beschreibung
         */
        const val PARAM_BESCHREIBUNG = "beschreibung"
    }
}

/**
 * Datentyp für die IDs von Fahrzeug-Objekten
 */
typealias FahrzeugId = UUID
// typealias FahrzeugId = Long

/**
 * Datentyp für sonstige IDs in DB-Tabellen
 */
typealias DbId = UUID
// typealias DbId = Long
