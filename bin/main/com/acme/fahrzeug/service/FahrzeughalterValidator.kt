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
import com.acme.fahrzeug.entity.Fahrzeughalter
import org.springframework.stereotype.Service

/**
 * Validierung von Objekten der Klasse [Fahrzeughalter].
 */
@Service
@Suppress("UseDataClass")
class FahrzeughalterValidator {
    /**
     * Ein Validierungsobjekt für die Validierung von Fahrzeughalter-Objekten
     */
    val validator = validator {
        @Suppress("MagicNumber")
        Fahrzeughalter::vorname {
            notEmpty().message(
                ViolationMessage.of("fahrzeughalter.vorname.notEmpty", "Vorname is required."),
            )
                /*.pattern(Name_PATTERN).message(
                    ViolationMessage.of("fahrzeughalter.vorname.pattern", "Vorname invalid"),
                )*/
        }

        @Suppress("MagicNumber")
        Fahrzeughalter::nachname {
                notEmpty().message(
                    ViolationMessage.of("fahrzeughalter.nachname.notEmpty", "Nachname is required."),
                )/*
                    .pattern(Name_PATTERN).message(
                        ViolationMessage.of("fahrzeughalter.nachname.pattern", "Nachname invalid"),
                    )*/
        }
    }
    /**
     * Konstante für die Validierung eines Fahgrzeughalters
     */
    companion object {
        /**
         * Konstante für den regulären Ausdruck eins Fahrzeughalters
         */
        //ToDO Validierung anpassen
        //const val Name_PATTERN = "[A-ZÄÖÜ][a-zäöüß]+"
    }
}
