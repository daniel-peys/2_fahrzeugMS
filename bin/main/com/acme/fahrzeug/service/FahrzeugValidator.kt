/*
 * Copyright (C) 2021 - present Juergen Zimmermann, Hochschule Karlsruhe
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.acme.fahrzeug.service

import am.ik.yavi.builder.validator
import am.ik.yavi.core.ViolationMessage
import com.acme.fahrzeug.entity.Fahrzeug
import org.springframework.stereotype.Service
import java.time.LocalDate.now
import java.time.ZoneId

/**
 * Validierung von Objekten der Klasse [Fahrzeug].
 */
@Service
class FahrzeugValidator(fahrzeughalterValidator: FahrzeughalterValidator) {
    private val validator = validator {
        @Suppress("MagicNumber")
        Fahrzeug::beschreibung {
            notEmpty().message(
                ViolationMessage.of("fahrzeug.beschreibung.notEmpty", "Beschreibung is required."),
            )
                .lessThanOrEqual(40).message(
                    ViolationMessage.of(
                        "fahrzeug.beschreibung.lessThanOrEqual",
                        "A beschreibung can be a maximum of 40 characters long.",
                    ),
                )/*
                .pattern(BESCHREIBUNG_PATTERN).message(
                    ViolationMessage.of(
                        "fahrzeug.beschreibung.pattern",
                        "After a capital letter at least one lowercase letter is required.",
                    ),
                )*/
        }

        @Suppress("MagicNumber")
        Fahrzeug::kennzeichen {
            notEmpty().message(
                ViolationMessage.of("fahrzeug.kennzeichen.notEmpty", "Kennzeichen is required."),
            )
                .lessThanOrEqual(40).message(
                    ViolationMessage.of(
                        "fahrzeug.kennzeichen.lessThanOrEqual",
                        "An email can be a maximum of 40 characters long.",
                    ),
                )
                .pattern(KENNZEICHEN_PATTERN).message(
                    ViolationMessage.of(
                        "fahrzeug.kennzeichen.pattern",
                        "After a capital letter at least one lowercase letter is required.....?",
                    ),
                )
        }

        Fahrzeug::kilometerstand {
            greaterThanOrEqual(MIN_KILOMETERSTAND).message(
                ViolationMessage.of("fahrzeug.kilometerstand.min", "The kilometerstand value must be at least {1}."),
            )
                .lessThanOrEqual(MAX_KILOMETERSTAND).message(
                    ViolationMessage.of("fahrzeug.kilometerstand.max", "The kilometerstand value must not exceed {1}."),
                )
        }

        /*Fahrzeug::erstzulassung {
            before { now(ZoneId.of(TIMEZONE_BERLIN)) }.message(
                ViolationMessage.of("fahrzeug.erstzulassung.before", "The erstzulassung must be in the past."),
            )
        }*/

        Fahrzeug::fahrzeughalter.nest(fahrzeughalterValidator.validator)

        @Suppress("MagicNumber")
        Fahrzeug::username {
            lessThanOrEqual(20)
                .message(
                    ViolationMessage.of(
                        "fahrzeug.username.lessThanOrEqual",
                        "A username can be a maximum of 20 characters long.",
                    ),
                )
        }
    }

    /**
     * Validierung eines Entity-Objekts der Klasse [Fahrzeug]
     *
     * @param fahrzeug Das zu validierende Fahrzeug-Objekt
     * @return Eine Collection mit den Verletzungen der Constraints oder eine leere Collection
     */
    fun validate(fahrzeug: Fahrzeug) = validator.validate(fahrzeug)

    /**
     * Konstante für Validierung
     */
    companion object {
        /**
         * Muster für einen gültige Beschreibung
         */
        //TODO pattern anpassen
        //const val BESCHREIBUNG_PATTERN = "[A-ZÄÖÜ][a-zäöüß]+"

        /**
         * Muster für einen gültige Beschreibung
         */
        const val KENNZEICHEN_PATTERN = "[A-ZÖÜÄ]{1,3} [A-ZÖÜÄ]{1,2} [1-9]{1}[0-9]{0,2}"

        /**
         * Kleinster Wert für eine Kategorie.
         */
        const val MIN_KILOMETERSTAND = 0

        /**
         * Maximaler Wert für eine Kategorie.
         */
        const val MAX_KILOMETERSTAND = 9999999

        /**
         * Mitteleuropäische Zeitzone
         */
        const val TIMEZONE_BERLIN = "Europe/Berlin"
    }
}
