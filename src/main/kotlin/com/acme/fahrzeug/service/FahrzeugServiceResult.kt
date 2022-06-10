package com.acme.fahrzeug.service

import am.ik.yavi.core.ConstraintViolation
import com.acme.fahrzeug.entity.Fahrzeug
import com.acme.fahrzeug.entity.FahrzeugId
import com.acme.fahrzeug.mail.SendResult
import com.acme.fahrzeug.security.CustomUser

/**
 * Resultat-Typ für [FahrzeugReadService.findById]
 */
sealed interface FindByIdResult {
    /**
     * Resultat-Typ, wenn ein Fahrzeug gefunden wurde.
     * @property fahrzeug Der gefundene Fahrzeug
     */
    data class Success(val fahrzeug: Fahrzeug) : FindByIdResult

    /**
     * Resultat-Typ, wenn kein Fahrzeug gefunden wurde.
     * @property id ID des nicht-vorhandenen Fahrzeugs
     */
    data class NotFound(val id: FahrzeugId) : FindByIdResult

    /**
     * Resultat-Typ, wenn ein Fahrzeug wegen unzureichender Rollen _nicht_ gesucht werden darf.
     * @property rollen Die vorhandenen Rollen
     */
    data class AccessForbidden(val rollen: Collection<String> = emptyList()) : FindByIdResult
}

/**
 * Resultat-Typ für [FahrzeugWriteService.create]
 */
sealed interface CreateResult {
    /**
     * Resultat-Typ, wenn ein neues Fahrzeug erfolgreich angelegt wurde.
     * @property fahrzeug Das neu angelegte Fahrzeug
     */
    data class Success(val fahrzeug: Fahrzeug) : CreateResult

    /**
     * Resultat-Typ, wenn ein neues Fahrzeug erfolgreich angelegt wurde, aber die Email nicht verschickt wurde.
     * @property fahrzeug Das neu angelegte Fahrzeug
     * @property sendResult Resultat der verschickten Email
     */
    data class SuccessWithoutEmail(val fahrzeug: Fahrzeug, val sendResult: SendResult) : CreateResult

    /**
     * Resultat-Typ, wenn ein Fahrzeug wegen Constraint-Verletzungen nicht angelegt wurde.
     * @property violations Die verletzten Constraints
     */
    data class ConstraintViolations(val violations: Collection<ConstraintViolation>) : CreateResult

    /**
     * Resultat-Typ, wenn das Passwort ungültig ist.
     * @property invalidUser Ungültiger User vom Typ [CustomUser]]
     */
    data class InvalidUser(val invalidUser: CustomUser) : CreateResult

    /**
     * Resultat-Typ, wenn der Username eines neu anzulegenden Fahrzeugs bereits existiert.
     * @property username Der existierende Username
     */
    data class UsernameExists(val username: String) : CreateResult

    /**
     * Resultat-Typ, wenn die Email eines neu anzulegenden Fahrzeug bereits existiert.
     * @property kennzeichen Die existierende Email
     */
    data class KennzeichenExists(val kennzeichen: String) : CreateResult
}

/**
 * Resultat-Typ für [FahrzeugWriteService.update]
 */
sealed interface UpdateResult {
    /**
     * Resultat-Typ, wenn ein Fahrzeug erfolgreich aktualisiert wurde.
     * @property fahrzeug Das aktualisierte Fahrzeug
     */
    data class Success(val fahrzeug: Fahrzeug) : UpdateResult

    /**
     * Resultat-Typ, wenn ein Fahrzeug wegen Constraint-Verletzungen nicht aktualisiert wurde.
     * @property violations Die verletzten Constraints
     */
    data class ConstraintViolations(val violations: Collection<ConstraintViolation>) : UpdateResult

    /**
     * Resultat-Typ, wenn die Versionsnummer eines zu öndernden Fahrzeug ungültig ist.
     * @property version Die ungültige Versionsnummer
     */
    data class VersionInvalid(val version: String) : UpdateResult

    /**
     * Resultat-Typ, wenn die Versionsnummer eines zu öndernden Fahrzeug nicht aktuell ist.
     * @property version Die veraltete Versionsnummer
     */
    data class VersionOutdated(val version: Int) : UpdateResult

    /**
     * Resultat-Typ, wenn das Kennzeichen eines zu öndernden Fahrzeugs bereits existiert.
     * @property kennzeichen Die existierende Kennzeichen
     */
    data class KennzeichenExists(val kennzeichen: String) : UpdateResult

    /**
     * Resultat-Typ, wenn ein nicht-vorhandener Kunde aktualisiert werden sollte.
     */
    object NotFound : UpdateResult
}
