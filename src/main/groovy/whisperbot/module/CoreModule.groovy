package whisperbot.module

import sx.blah.discord.api.IDiscordClient
import sx.blah.discord.handle.obj.IMessage

import whisperbot.ModuleManager
import whisperbot.data.Command
import whisperbot.data.Events
import whisperbot.util.SpreadsheetConnector

class CoreModule extends Module {

    CoreModule() {
        name = 'core'
        enabledByDefault = true
        commands = [
            [ 
                name: 'enable_module_server', on: [Events.MESSAGE_RECEIVED] as Set, enabledByDefault: true,
                info: '`~ems moduleName` or `~enable_module_server moduleName`. Enables a module on the current server.',
                action: { message -> if(messageStartsWith(message.content, '~ems', '~enable_module_server')) enableModuleServer(message) }
            ] as Command,
            [ 
                name: 'disable_module_server', on: [Events.MESSAGE_RECEIVED] as Set, enabledByDefault: true,
                info: '`~dms moduleName` or `~disable_module_server moduleName`. Disables a module on the current server.',
                action: { message -> if(messageStartsWith(message.content, '~dms', '~disable_module_server')) disableModuleServer(message) }
            ] as Command,
            [
                name: 'list_commands', on: [Events.MESSAGE_RECEIVED] as Set, enabledByDefault: true,
                info: '`~command` or `~commands`. Lists all modules and their commands.',
                action: { message -> if(messageStartsWith(message.content, '~command', '~commands')) listCommands(message) }
            ] as Command
        ]
    }

    void init(ModuleManager manager) {
        super.init(manager)
    }

    void enableModuleServer(IMessage iMessage) {
        List moduleNames = iMessage.content.split(' ')
        moduleNames.remove(0)
        if(moduleNames.isEmpty()) {
            return
        }
        Map successes = [:]
        moduleNames.each { moduleName ->
            Module module = manager.findModuleByName(moduleName)
            if(!module) {
                successes[(moduleName)] = false
                return
            }
            else if(module.name == this.name) {
                return
            }
            module.setEnabledOnServer(iMessage.guild.id, true)
            successes[(moduleName)] = true
        }
        List successList = successes.findAll { it.value }*.key // Get all entries where key.value is true, then get a list with the keys of each.
        List failureList = successes.findAll { !it.value }*.key
        List messagePieces = []
        if(successList) {
            messagePieces << "${successList.size() > 1 ? 'Modules' : 'Module'} `${successList.join(', ')}` ${successList.size() > 1 ? 'were' : 'was'} enabled on this server."
        }
        if(failureList) {
            messagePieces << "No ${failureList.size() > 1 ? 'modules were' : 'module was'} found with ${failureList.size() > 1 ? 'names' : 'name'} `${failureList.join(', ')}` to enable."
        }
        manager.sendMessage(iMessage.channel, messagePieces.join(' '))
    }

    void disableModuleServer(IMessage iMessage) {
        List moduleNames = iMessage.content.split(' ')
        moduleNames.remove(0)
        if(moduleNames.isEmpty()) {
            return
        }
        Map successes = [:]
        moduleNames.each { moduleName ->
            Module module = manager.findModuleByName(moduleName)
            if(!module) {
                successes[(moduleName)] = false
                return
            }
            else if(module.name == this.name) {
                return
            }
            module.setEnabledOnServer(iMessage.guild.id, false)
            successes[(moduleName)] = true
        }
        List successList = successes.findAll { it.value }*.key // Get all entries where key.value is true, then get a list with the keys of each.
        List failureList = successes.findAll { !it.value }*.key
        List messagePieces = []
        if(successList) {
            messagePieces << "${successList.size() > 1 ? 'Modules' : 'Module'} `${successList.join(', ')}` ${successList.size() > 1 ? 'were' : 'was'} disabled on this server."
        }
        if(failureList) {
            messagePieces << "No ${failureList.size() > 1 ? 'modules were' : 'module was'} found with ${failureList.size() > 1 ? 'names' : 'name'} `${failureList.join(', ')}` to disable."
        }
        manager.sendMessage(iMessage.channel, messagePieces.join(' '))
    }

    void listCommands(IMessage iMessage) {
        String moduleInfo = '**Command Listing**\n'
        manager.modules.each { module ->
            if(module.showInCommandsList && module.status.enabledOnServer(iMessage.guild.id)) {
                moduleInfo += "Module ${module.name}\n"
                module.commands.each { command ->
                    moduleInfo += "\t\t${command.name} - ${command.info}\n"
                }
            }
        }
        manager.sendMessage(iMessage.channel, moduleInfo)
    }
}