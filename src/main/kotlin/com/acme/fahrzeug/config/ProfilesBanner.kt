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
package com.acme.fahrzeug.config

import org.springframework.boot.Banner
import org.springframework.boot.SpringBootVersion
import org.springframework.core.SpringVersion
import org.springframework.security.core.SpringSecurityCoreVersion
import java.net.InetAddress
import java.util.Locale

/**
 * Singleton-Klasse, um sinnvolle Konfigurationswerte für den Microservice vorzugeben.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@h-ka.de)
 */
object ProfilesBanner {
    /**
     * Konstante für das Spring-Profile "dev".
     */
    const val DEV = "dev"

    /**
     * Banner für den Start des Microservice in der Konsole.
     */
    val banner = Banner { _, _, out ->
        val jdkVersion = "${Runtime.version()} @ ${System.getProperty("java.version.date")}"
        val osVersion = System.getProperty("os.name")
        val localhost = InetAddress.getLocalHost()
        val serviceHost = System.getenv("FAHRZEUG_SERVICE_HOST")
        val servicePort = System.getenv("FAHRZEUG_SERVICE_PORT")
        val kubernetes = when (serviceHost) {
            null -> "N/A"
            else -> "FAHRZEUG_SERVICE_HOST=$serviceHost, FAHRZEUG_SERVICE_PORT=$servicePort"
        }
        val username = System.getProperty("user.name")

        out.println(
            """
            |--------------------------------
            |------------FAHRZEUG------------
            |--------------------------------
            |Version              2.0
            |Spring Boot          ${SpringBootVersion.getVersion()}
            |Spring Security      ${SpringSecurityCoreVersion.getVersion()}
            |Spring Framework     ${SpringVersion.getVersion()}
            |Hibernate            ${org.hibernate.Version.getVersionString()}
            |Kotlin               ${KotlinVersion.CURRENT}
            |OpenJDK              $jdkVersion
            |Betriebssystem       $osVersion
            |Rechnername          ${localhost.hostName}
            |IP-Adresse           ${localhost.hostAddress}
            |Kubernetes           $kubernetes
            |Username             $username
            |JVM Locale           ${Locale.getDefault()}
            |OpenAPI              /swagger-ui.html /v3/api-docs
            |
            """
                .trimMargin("|"),
        )
    }
}
