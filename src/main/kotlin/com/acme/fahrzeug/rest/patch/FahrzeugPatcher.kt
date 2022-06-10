package com.acme.fahrzeug.rest.patch

import com.acme.fahrzeug.entity.Fahrzeug
import com.acme.fahrzeug.rest.patch.PatchOperationType.REPLACE
import org.slf4j.LoggerFactory

/**
 * Singleton-Klasse, um PATCH-Operationen auf Fahrzeug-Objekte anzuwenden.
 */
object FahrzeugPatcher {
    private val logger = LoggerFactory.getLogger(FahrzeugPatcher::class.java)

    /**
     * PATCH-Operationen werden auf ein Fahrzeug-Objekt angewandt.
     * @param fahrzeug Das zu modifizierende Fahrzeug-Objekt.
     * @param operations Die anzuwendenden Operationen.
     * @return Ein Fahrzeug Objekt mit den modifizierten Properties.
     */
    fun patch(fahrzeug: Fahrzeug, operations: Collection<PatchOperation>): Fahrzeug? {
        val replaceOps = operations.filter { it.op == REPLACE }
        logger.debug("patch(): replaceOps={}", replaceOps)
        return replaceOps(fahrzeug, replaceOps)
    }

    private fun replaceOps(fahrzeug: Fahrzeug, ops: Collection<PatchOperation>): Fahrzeug {
        var fahrzeugUpdated = fahrzeug
        ops.forEach { (_, path, value) ->
            when (path) {
                "/beschreibung" -> fahrzeugUpdated = fahrzeugUpdated.copy(beschreibung = value)
                "/kennzeichen" -> fahrzeugUpdated = fahrzeugUpdated.copy(kennzeichen = value)
            }
        }
        logger.trace("replaceOps(): fahrzeugUpdated={}", fahrzeugUpdated)
        return fahrzeugUpdated
    }
}
