package com.acme.fahrzeug.config

import com.acme.fahrzeug.rest.FahrzeugGetController.Companion.API_PATH
import com.acme.fahrzeug.rest.FahrzeugGetController.Companion.BESCHREIBUNGEN_PATH
import com.acme.fahrzeug.security.AuthController.Companion.AUTH_PATH
import com.acme.fahrzeug.security.Rolle.actuator
import com.acme.fahrzeug.security.Rolle.admin
import com.acme.fahrzeug.security.Rolle.fahrzeug
import org.springframework.boot.actuate.autoconfigure.security.reactive.EndpointRequest
import org.springframework.boot.actuate.health.HealthEndpoint
import org.springframework.boot.context.properties.bind.Bindable.mapOf
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.POST
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.config.web.server.invoke
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder
import org.springframework.security.crypto.password.DelegatingPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers.pathMatchers

/**
 * Security-Konfiguration.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@h-ka.de)
 */
interface SecurityConfig {
    /**
     * Bean-Definition, um den Zugriffsschutz an der REST-Schnittstelle zu konfigurieren.
     *
     * @param http Injiziertes Objekt von `ServerHttpSecurity` als Ausgangspunkt für die Konfiguration.
     * @return Objekt von `SecurityWebFilterChain`
     */
    @Bean
    @Suppress("LongMethod")
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain = http {
        authorizeExchange {
            val fahrzeugIdPath = "$API_PATH/*"

            authorize(pathMatchers(GET, API_PATH), permitAll)
            authorize(pathMatchers(POST, API_PATH), permitAll)
            authorize(pathMatchers(GET, API_PATH), hasRole(admin))
            authorize(pathMatchers(GET, "$API_PATH$AUTH_PATH/rollen", "$API_PATH$BESCHREIBUNGEN_PATH/*"), hasRole(fahrzeug))
            authorize(pathMatchers(GET, fahrzeugIdPath), hasAnyRole(admin, fahrzeug))
            authorize(pathMatchers(fahrzeugIdPath), hasRole(admin))
            authorize(pathMatchers(POST, "$API_PATH$AUTH_PATH/login"), permitAll)

            authorize(pathMatchers(GET, "/swagger-ui.html"), hasRole(admin))

            authorize(pathMatchers(POST, "/graphql"), hasRole(admin))
            authorize(pathMatchers(GET, "/graphiql"), denyAll)
            // authorize(pathMatchers(GET, "/graphiql"), permitAll)

            // Actuator: Health mit Liveness und Readiness wird von Kubernetes genutzt
            authorize(EndpointRequest.to(HealthEndpoint::class.java), permitAll)
            authorize(EndpointRequest.toAnyEndpoint(), hasRole(actuator))

            authorize(anyExchange, authenticated)
        }

        httpBasic {}
        formLogin { disable() }

        // als Default sind durch ServerHttpSecurity aktiviert:
        // * Keine XSS (= Cross-site scripting) Angriffe: Header "X-XSS-Protection: 1; mode=block"
        //   https://www.owasp.org/index.php/Cross-site_scripting
        // * Kein CSRF (= Cross-Site Request Forgery) durch CSRF-Token
        //   https://www.owasp.org/index.php/Cross-Site_Request_Forgery_(CSRF)_Prevention_Cheat_Sheet
        // * Kein Clickjacking: im Header "X-Frame-Options: DENY"
        //   https://www.owasp.org/index.php/Clickjacking
        //   http://tools.ietf.org/html/rfc7034
        // * HSTS (= HTTP Strict Transport Security) für HTTPS: im Header
        //      "Strict-Transport-Security: max-age=31536000 ; includeSubDomains"
        //   https://www.owasp.org/index.php/HTTP_Strict_Transport_Security
        //   https://tools.ietf.org/html/rfc6797
        // * Kein MIME-sniffing: im Header "X-Content-Type-Options: nosniff"
        //   https://blogs.msdn.microsoft.com/ie/2008/09/02/ie8-security-part-vi-beta-2-update
        //   http://msdn.microsoft.com/en-us/library/gg622941%28v=vs.85%29.aspx
        //   https://tools.ietf.org/html/rfc7034
        // * im Header: "Cache-Control: no-cache, no-store, max-age=0, must-revalidate"
        //   https://developer.okta.com/blog/2018/07/30/10-ways-to-secure-spring-boot

        // CSRF wird deaktiviert:
        // * CSRF ist bei einem stateless Web Service sinnlos.
        // * Der interaktive REST-Client von IntelliJ kann nur benutzt werden, wenn CSRF deaktiviert ist.
        // * Ausserdem muss man dann auch in den Tests keinen "CSRF Token" generieren.
        csrf { disable() }

        // CSP = Content Security Policy
        //  https://www.owasp.org/index.php/HTTP_Strict_Transport_Security
        //  https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Content-Security-Policy
        //  https://tools.ietf.org/html/rfc7762
        // headers { contentSecurityPolicy { policyDirectives = "default-src 'self'" }
    }

    /**
     * Bean-Definition, um den Verschlüsselungsalgorithmus für Passwörter bereitzustellen.
     * Es wird _argon2id_ statt _bcrypt_ (Default-Algorithmus von Spring Security) verwendet.
     * @return Objekt für die Verschlüsselung von Passwörtern.
     */
    @Bean
    fun passwordEncoder(): PasswordEncoder {
        // https://docs.spring.io/spring-security/reference/features/authentication/password-storage.html#authentication-password-storage-dpe
        // https://github.com/OWASP/CheatSheetSeries/blob/master/cheatsheets/Password_Storage_Cheat_Sheet.md
        // https://www.rfc-editor.org/rfc/rfc9106.html
        val idForEncode = "argon2id"

        // TODO https://github.com/spring-projects/spring-security/pull/10447#issuecomment-966593724
        // Defaultwerte:
        //     saltLength = 16
        //     hashLength = 32
        //     parallelization = 1  Bouncy Castle kann keine Parallelitaet
        //     memory = 1 << 12     d.h. 2^12 in KByte  ("Memory Cost Parameter")
        //     iterations = 3
        @Suppress("MagicNumber")
        val encoders = mapOf(idForEncode to Argon2PasswordEncoder(16, 32, 1, 1 shl 14, 3))
        return DelegatingPasswordEncoder(idForEncode, encoders)
    }
}
