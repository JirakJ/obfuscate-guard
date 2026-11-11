package dev.htmeta.obfuscateguard.ui.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@State(
    name = "ObfuscateGuardSettings",
    storages = [Storage("ObfuscateGuardSettings.xml")]
)
class ObfuscateGuardSettings : PersistentStateComponent<ObfuscateGuardSettings> {

    var enabled: Boolean = true
    /** Level pre-selected in the configuration wizard (an [dev.htmeta.obfuscateguard.core.model.ObfuscationLevel] name). */
    var defaultLevel: String = "STANDARD"
    var showGutterIcons: Boolean = true

    override fun getState(): ObfuscateGuardSettings = this

    override fun loadState(state: ObfuscateGuardSettings) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        fun getInstance(): ObfuscateGuardSettings =
            ApplicationManager.getApplication().getService(ObfuscateGuardSettings::class.java)
    }
}
