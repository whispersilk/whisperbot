package whisperbot.module

import sx.blah.discord.api.EventSubscriber
import sx.blah.discord.handle.impl.events.*

import whisperbot.data.Command
import whisperbot.data.Events
import whisperbot.data.Status
import whisperbot.ModuleManager
import whisperbot.util.FileConnector

abstract class Module {

    protected String name
    protected List commands
    protected boolean enabledByDefault
    protected boolean showInCommandsList = true

    protected ModuleManager manager

    Status status

    void init(ModuleManager manager) {
        this.manager = manager
        status = new Status("config/${name}.json", enabledByDefault)
        commands.each {
            it.init(name)
        }
    }

    boolean messageStartsWith(String message, String... options) {
        String start = message.split(' ')[0]
        if(options.find { it.equalsIgnoreCase(start) }) {
            return true
        }
        return false
    }

    Command findCommandByName(String name) {
        return commands.find { it.name == name }
    }

    boolean enabledOnServer(String serverId) {
        return status.enabledOnServer(serverId)
    }

    boolean enabledOnChannel(String serverId, String channelId) {
        return status.enabledOnChannel(serverId, channelId)
    }

    void setEnabledOnServer(String serverId, boolean enabled) {
        status.setEnabledOnServer(serverId, enabled)
    }

    void setEnabledOnChannel(String serverId, String channelId, boolean enabled) {
        status.setEnabledOnChannel(serverId, channelId, enabled)
    }

    String toString() {
        return name
    }

    void onDiscordDisconnected(DiscordDisconnectedEvent event) {
        commands.each {
            if(it.registers(Events.DISCORD_DISCONNECTED)) {
                it.action(event)
            }
        }
    }

    void onMessageReceived(MessageReceivedEvent event) {
        commands.each {
            if(it.registers(Events.MESSAGE_RECEIVED) && it.enabledOnServer(event.message.guild.id) && it.enabledOnChannel(event.message.guild.id, event.message.channel.id)) {
                it.action(event.message)
            }
        }
    }
}