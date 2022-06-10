package com.acme.fahrzeug.entity

import com.fasterxml.jackson.annotation.JsonValue
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
import javax.persistence.AttributeConverter

/**
 * Enum für Fahrzeugtyp. Dazu können auf der Clientseite z.B. Radiobuttons realisiert werden.
 * @property value Der interne Wert
 */
enum class FahrzeugType(val value: String) {
    /**
     * _Anhänger_ mit dem internen Wert `A` für z.B. das Mapping in einem
     * JSON-Datensatz oder das Abspeichern in einer DB.
     */
    ANHAENGER("A"),

    /**
     * _Nutzfahrzeug_ mit dem internen Wert `N` für z.B. das Mapping in einem
     * JSON-Datensatz oder das Abspeichern in einer DB.
     */
    NUTZFAHRZEUG("N"),

    /**
     * _PKW_ mit dem internen Wert `P` für z.B. das Mapping in einem
     * JSON-Datensatz oder das Abspeichern in einer DB.
     */
    PKW("P");

    /**
     * Einen enum-Wert als String mit dem internen Wert ausgeben.
     * Dieser Wert wird durch Jackson in einem JSON-Datensatz verwendet.
     * [https://github.com/FasterXML/jackson-databind/wiki]
     * @return Interner Wert
     */
    @JsonValue
    override fun toString() = value

    /**
     * Companion Object, um aus einem String einen Enum-Wert von GeschlechtType zu bauen
     */
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(FahrzeugType::class.java)

        /**
         * Konvertierung eines Strings in einen Enum-Wert
         * @param value Der String, zu dem ein passender Enum-Wert ermittelt werden soll.
         * @return Passender Enum-Wert.
         */
        fun fromValue(value: String?) = try {
            enumValues<FahrzeugType>().single { fahrzeugtyp -> fahrzeugtyp.value == value }
        } catch (e: NoSuchElementException) {
            logger.warn("Ungueltiger Wert '{}' fuer Fahrzeugtyp: {}", value, e.message)
            null
        }
    }
}

/**
 * Konvertierungsklasse, um die Enum-Werte abgekürzt abzuspeichern.
 */
class FahrzeugTypeConverter : AttributeConverter<FahrzeugType?, String> {
    /**
     * Konvertierungsfunktion, um einen Enum-Wert in einen abgekürzten String für die DB zu transformieren.
     * @param fahrzeugtyp Der Enum-Wert
     * @return Der abgekürzte String
     */
    override fun convertToDatabaseColumn(fahrzeugtyp: FahrzeugType?) = fahrzeugtyp?.value

    /**
     * Konvertierungsfunktion, um einen abgekürzten String aus einer DB-Spalte in einen Enum-Wert zu transformieren.
     * @param value Der abgekürzte String
     * @return Der Enum-Wert
     */
    override fun convertToEntityAttribute(value: String?) = FahrzeugType.fromValue(value)
}
