package gg.rsmod.plugins.osrs.content

import gg.rsmod.game.model.NEW_ACCOUNT_ATTR
import gg.rsmod.plugins.osrs.api.ChatMessageType
import gg.rsmod.plugins.osrs.api.InterfaceDestination
import gg.rsmod.plugins.osrs.api.OSRSGameframe
import gg.rsmod.plugins.osrs.api.Skills
import gg.rsmod.plugins.osrs.api.ext.*
import gg.rsmod.plugins.osrs.content.inter.attack.AttackTab

onLogin {
    val p = it.player()

    /**
     * First log-in logic (when accounts have just been made).
     */
    if (p.attr.getOrDefault(NEW_ACCOUNT_ATTR, false)) {
        AttackTab.setEnergy(p, 100)
    }

    /**
     * Skill-related logic.
     */
    if (p.getSkills().getMaxLevel(Skills.HITPOINTS) < 10) {
        p.getSkills().setBaseLevel(Skills.HITPOINTS, 10)
    }
    p.calculateAndSetCombatLevel()
    p.sendWeaponComponentInformation()
    p.sendCombatLevelText()

    /**
     * Interface-related logic.
     */
    p.openOverlayInterface(p.components.displayMode)
    InterfaceDestination.values().filter { pane -> pane.interfaceId != -1 }.forEach { pane ->
        if (pane == InterfaceDestination.XP_COUNTER && p.getVarbit(OSRSGameframe.XP_DROPS_VISIBLE_VARBIT) == 0) {
            return@forEach
        } else if (pane == InterfaceDestination.MINI_MAP && p.getVarbit(OSRSGameframe.HIDE_DATA_ORBS_VARBIT) == 1) {
            return@forEach
        }
        p.openInterface(pane.interfaceId, pane)
    }

    /**
     * Inform the client whether or not we have a display name.
     */
    val displayName = p.username.isNotEmpty()
    p.runClientScript(1105, if (displayName) 1 else 0) // Has display name
    p.runClientScript(423, p.username)
    if (p.getVarp(1055) == 0 && displayName) {
        p.syncVarp(1055)
    }

    /**
     * Game-related logic.
     */
    p.sendRunEnergy(p.runEnergy.toInt())
    p.message("Welcome to ${p.world.gameContext.name}.", ChatMessageType.SERVER)
}