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
package com.acme.fahrzeug.security

import am.ik.yavi.builder.StringValidatorBuilder
import am.ik.yavi.constraint.password.PasswordPolicy.NUMBERS
import am.ik.yavi.constraint.password.PasswordPolicy.SYMBOLS
import am.ik.yavi.core.ViolationMessage
import org.springframework.stereotype.Service

/**
 * Validierung von Passwörter.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@h-ka.de)
 */
@Service
@Suppress("UseDataClass")
class CustomUserValidator {
    /**
     * Ein Validierungsobjekt für die Validierung von Passwörter.
     */
    private val passwordValidator = StringValidatorBuilder.of("password") { constraint ->
        constraint.password { passwordPolicy ->
            passwordPolicy
                .uppercase()
                .lowercase()
                .optional(1, NUMBERS, SYMBOLS)
                .build()
        }.message(
            ViolationMessage.of(
                "user.password",
                "Lowercase and uppercase is required, and number or symbol.",
            ),
        )
    }.build()

    /**
     * Validierung eines Entity-Objekts der Klasse [CustomUser]
     *
     * @param user Das zu validierende CustomUser-Objekt
     * @return Eine Collection mit den Verletzungen der Constraints oder eine leere Collection
     */
    fun validatePassword(user: CustomUser) = passwordValidator.validate(user.password)
}
