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

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.hateoas.config.EnableHypermediaSupport
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType.HAL
import org.springframework.hateoas.support.WebStack.WEBFLUX

/**
 * Konfigurationsklasse für die Anwendung bzw. den Microservice.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@h-ka.de)
 */
// @EnableReactiveMethodSecurity fuer @PreAuthorize und @Secured
// TODO @AutoConfiguration ab Spring Boot 3
@Configuration(proxyBeanMethods = false)
@EnableHypermediaSupport(type = [HAL], stacks = [WEBFLUX])
// @EnableReactiveMethodSecurity
@EnableConfigurationProperties(DbProps::class, MailProps::class, MailAddressProps::class)
class AppConfig : HibernateReactiveConfig, SecurityConfig
