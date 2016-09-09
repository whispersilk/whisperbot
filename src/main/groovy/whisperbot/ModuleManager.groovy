package whisperbot

import com.google.common.reflect.ClassPath

import javax.lang.model.element.Modifier

import sx.blah.discord.api.ClientBuilder
import sx.blah.discord.api.EventSubscriber
import sx.blah.discord.api.IDiscordClient
import sx.blah.discord.handle.impl.events.*
import sx.blah.discord.util.MessageBuilder

import whisperbot.exception.CoreModuleNotFoundException
import whisperbot.module.Module

class ModuleManager {

    List modules
    IDiscordClient client

    ModuleManager init(IDiscordClient client) {
        modules = []
        this.client = client
        loadModules()
    }

    ModuleManager loadModules(String expectedName = null) {
        ClassLoader loader = ModuleManager.getClassLoader()
        ClassPath.from(loader).getTopLevelClasses("whisperbot.module").collect { it.getName() }.each { className ->
            Class moduleClass = loader.loadClass(className)
            if(moduleClass.getModifiers() != Modifier.ABSTRACT && moduleClass.getSuperclass().getName() == 'whisperbot.module.Module') {
                Module module = moduleClass.newInstance()

                // Do some extra work if we're looking for a specific module.
                if(expectedName != null) {
                    // If the name isn't right, just skip over it (return rather than break because the each closure is its own class).
                    if(module.name != expectedName) {
                        return
                    }
                    // If this is the module we want, remove existing instances of it from the modules list before adding the new one.
                    else {
                        modules.removeAll { it.name == expectedName }
                    }
                }
                module.init(this)
                modules << module
            }
        }
        if(!modules.find { it.name == 'core' }) {
            throw new CoreModuleNotFoundException("Must load a module with name 'core'!")
        }
        println modules
    }

    Module findModuleByName(String name) {
        return modules.find { it.name == name }
    }

    void sendMessage(def channel, String message) {
        sendMessage(channel, '', message)
    }

    void sendMessage(def channel, String prefix, String message) {
        if(prefix.size() + message.size() <= 2000) {
            new MessageBuilder(client).withChannel(channel).withContent(prefix + message).build()
            return
        }
        List pieces = []
        int length = prefix.size()
        StringBuilder builder = new StringBuilder()
        builder.append(prefix)
        message.each { character ->
            if(length < 2000) {
                builder.append(character)
                length++
            }
            else {
                pieces << builder.toString()
                builder = new StringBuilder()
                builder.append(character)
                length = 1
            }
        }
        pieces.each { piece ->
            new MessageBuilder(client).withChannel(channel).withContent(piece).build()
        }
    }

    @EventSubscriber
    void onDiscordDisconnected(DiscordDisconnectedEvent event) {
        client = new ClientBuilder().withToken(WhisperBotMain.LOGIN_TOKEN).login()
        client.getDispatcher().registerListener(this)
        println 'Re-registered this in client dispatcher.'
        modules.each {
            it.onDiscordDisconnected(event)
        }
    }

    @EventSubscriber
    void onMessageReceived(MessageReceivedEvent event) {
        String serverId = event.message.guild.id
        String channelId = event.message.channel.id
        modules.each {
            if(it.enabledOnServer(serverId) && it.enabledOnChannel(serverId, channelId)) {
                it.onMessageReceived(event)
            }
        }
    }
}