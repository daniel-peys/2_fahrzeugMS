/*
 * Copyright (C) 2017 - present Juergen Zimmermann, Hochschule Karlsruhe
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
package com.acme.fahrzeug.config.dev

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import java.util.Base64.getEncoder

/**
 * Über einen _CommandLineRunner_ werden beispielhafte Resultate fuer BASIC Authentifizierung ausgegeben.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@h-ka.de)
 */
interface LogBasicAuth {
    /**
     * Bean-Definition, um einen CommandLineRunner  bereitzustellen, der verschiedene Benutzerkennungen für
     * _BASIC_-Authentifizierung codiert.
     * @return CommandLineRunner
     */
    @Bean
    fun logBasicAuth(
        @Value("\${app.password}") password: String,
        @Value("\${app.password-falsch}") passwordFalsch: String,
    ) = CommandLineRunner {
        val username = "admin"
        val pair = "$username:$password".toByteArray(charset("ISO-8859-1"))
        val encoded = "Basic ${getEncoder().encodeToString(pair)}"

        LoggerFactory.getLogger(LogBasicAuth::class.java)
            .warn("BASIC Authentication \"{}:{}\" -> {}", username, password, encoded)
    }
}
