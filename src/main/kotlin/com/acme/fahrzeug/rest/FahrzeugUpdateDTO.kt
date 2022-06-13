package com.acme.fahrzeug.rest

import com.acme.fahrzeug.entity.Fahrzeug
import com.acme.fahrzeug.entity.FahrzeugType
import com.acme.fahrzeug.entity.Fahrzeughalter
import java.time.LocalDate

/**
 * ValueObject für das Ändern eines neuen Fahrzeuges. Beim Lesen wird die Klasse [FahrzeugModel] für die
 * Ausgabe verwendet und für das Neuanlegen die Klasse [FahrzeugUserDTO].
 * @property beschreibung Gültiger Beschreibung eines Fahrzeuges, d.h. mit einem geeigneten Muster.
 * @property kennzeichen Kennzeichen eines Fahrzeuges
 * @property kilometerstand Kilometerstand eines Fahrzeuges mit eingeschränkten Werten.
 * @property erstzulassung Die Erstzulassung eines Fahrzeuges
 * @property fahrzeugtype Der Fahrzeugtyp eines Fahrzeuges
 * @property fahrzeughalter Der Fahrzeughalter eines Fahrzeuges
 */
data class FahrzeugUpdateDTO(
    val beschreibung: String,

    val kennzeichen: String,

    val kilometerstand: Int = 0,

    val erstzulassung: LocalDate?,

    val fahrzeugtype: FahrzeugType?,

    val fahrzeughalter: Fahrzeughalter,
) {
    /**
     * Konvertierung in ein Objekt des Anwendungskerns
     * @return Fahrzeugbjekt für den Anwendungskern
     */
    fun toFahrzeug() = Fahrzeug(
        id = null,
        beschreibung = beschreibung,
        kennzeichen = kennzeichen,
        kilometerstand = kilometerstand,
        erstzulassung = erstzulassung,
        fahrzeugtyp = fahrzeugtype,
        username = "TBD",
        fahrzeughalter = fahrzeughalter,
    )

    /**
     * Vergleich mit einem anderen Objekt oder null.
     * https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier
     * @param other Das zu vergleichende Objekt oder null
     * @return True, falls das zu vergleichende (Fahrzeug-) Objekt das gleiche Kennzeichenhat.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FahrzeugUpdateDTO
        return kennzeichen == other.kennzeichen
    }

    /**
     * Hashwert aufgrund des kennzeichens
     * @return Der Hashwert.
     */
    override fun hashCode() = kennzeichen.hashCode()
}
