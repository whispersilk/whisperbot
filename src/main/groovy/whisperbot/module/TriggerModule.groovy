package whisperbot.module

import sx.blah.discord.api.IDiscordClient
import sx.blah.discord.handle.obj.IMessage

import whisperbot.ModuleManager
import whisperbot.data.Command
import whisperbot.data.Events
import whisperbot.util.SpreadsheetConnector

class TriggerModule extends Module {

    private static final String OAUTH_FILE = '/google_oauth.json'

    private SpreadsheetConnector triggerSheet
    private SpreadsheetConnector usedSheet

    TriggerModule() {
        name = 'trigger'
        enabledByDefault = false
        commands = [
            [
                name: 'get_trigger', on: [Events.MESSAGE_RECEIVED] as Set, enabledByDefault: true,
                info: '`~trigger` or `~trigger number`. Selects a trigger from the Weaver Dice trigger list, at random or with the given number.',
                action: { message -> if(messageStartsWith(message.content, '~trigger')) getTrigger(message) }
            ] as Command
        ]
    }

    void init(ModuleManager manager) {
        super.init(manager)
        triggerSheet = new SpreadsheetConnector('Weaver Dice Triggers', 'Trigger Events').init(OAUTH_FILE)
        //usedSheet = new SpreadsheetConnector('Weaver Dice Triggers', 'Used Triggers').init(OAUTH_FILE)
    }

    void getTrigger(IMessage iMessage) {
        List triggers = triggerSheet.getColumnByName('triggerevents')
        Map options = getExtras(iMessage.content)
        int selection = -1
        if(options.number != null) {
            /*if(options.number >= triggers.size() || options.number < 0) {
                new MessageBuilderbot.send((options.pm) ? sender : channel, 'No trigger with that number')
                return
            }*/
            selection = options.number
        }
        else {
            selection = (Math.random() * triggers.size()) as int
        }
        String prefix = "${iMessage.author.mention()} - Trigger ${selection}: "
        manager.sendMessage(iMessage.channel, prefix, triggers[selection])
    }

    Map getExtras(String message) {
        Map toReturn = [pm: false]
        List pieces = message.split(' ')
        pieces.remove(0)
        if(pieces && pieces[0] =~ /\d+/) {
            toReturn.put('number', pieces[0] as int)
            pieces.remove(0)
        }
        if(pieces && pieces[0].equalsIgnoreCase('pm')) {
            toReturn.pm = true
        }
        return toReturn
    }
}