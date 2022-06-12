/*
 * Copyright (C) 2016 - present Juergen Zimmermann, Hochschule Karlsruhe
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
package com.acme.fahrzeug.rest.patch

import com.fasterxml.jackson.annotation.JsonValue

/**
 * Hilfsklasse für _HTTP PATCH_ mit Datensätzen, wie z.B.
 * `{"op": "replace", "path": "/email", "value": "new.email@test.de"}`
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@h-ka.de)
 *
 * @property op PATCH-Operation, z.B. _add_, _remove_, _replace_.
 * @property path Pfad zur adressierten Property, z.B. _/email_.
 * @property value Der neue Wert für die Property.
 */
data class PatchOperation(val op: PatchOperationType, val path: String, val value: String)

/**
 * Enum für die Patch-Operationen.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@h-ka.de)
 *
 * @property value Der interne Wert
 */
enum class PatchOperationType(val value: String) {
    /**
     * Ersetzen eines vorhandenen singulären Wertes.
     */
    REPLACE("replace"),

    /**
     * Einen neuen zusätzlichen Wert zu einer listen- oder mengenwertigen
     * Property hinzufügen.
     */
    ADD("add"),

    /**
     * Aus einer listen- oder mengenwertigen Property einen Wert entfernen.
     */
    REMOVE("remove");

    /**
     * Einen enum-Wert als String mit dem internen Wert ausgeben.
     * @return Der interne Wert.
     */
    @JsonValue
    override fun toString() = value
}
