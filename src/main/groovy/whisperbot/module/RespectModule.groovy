package whisperbot.module

import sx.blah.discord.api.IDiscordClient
import sx.blah.discord.handle.obj.IMessage

import whisperbot.ModuleManager
import whisperbot.data.Command
import whisperbot.data.Events
import whisperbot.util.SpreadsheetConnector

class RespectModule extends Module {

    private static final String OAUTH_FILE = '/google_oauth.json'

    private SpreadsheetConnector characters
    private SpreadsheetConnector groups

    RespectModule() {
        name = 'respect'
        enabledByDefault = true
        commands = [ 
            [
                name: 'rt_character', on: [Events.MESSAGE_RECEIVED] as Set, enabledByDefault: true,
                info: '`~rt name` or `~respect_thread name`. Searches for a respect thread for a character.',
                action: { message -> if(messageStartsWith(message.content, '~rt', '~respect_thread')) fetchRespectThread(message) }
            ] as Command,
            [
                name: 'rt_group', on: [Events.MESSAGE_RECEIVED] as Set, enabledByDefault: true,
                info: '`~grt name` or `~group_respect_thread name`. Searches for a respect thread for a group.',
                action: { message -> if(messageStartsWith(message.content, '~grt', '~group_respect_thread')) fetchGroupRespectThread(message) }
            ] as Command
        ]
    }

    void init(ModuleManager manager) {
        super.init(manager)
        characters = new SpreadsheetConnector('Character Spread', 'Characters').init(OAUTH_FILE)
        groups = new SpreadsheetConnector('Character Spread', 'Groups').init(OAUTH_FILE)
    }

    void fetchRespectThread(IMessage iMessage) {
        List pieces = iMessage.content.split(' ')
        pieces.remove(0)
        String message = pieces.join(' ')
        if(message == 'N/A') {
            return
        }
        List preParse = getMatchingRows(characters, message, 'name', 'alias')
        List data = characters.parseFields(['name', 'alias', 'respectthread'], preParse)
        if(data.size() > 10) {
            manager.sendMessage(iMessage.channel, "Sorry, I found ${data.size()} matches for that. Please try to be more specific in your searching.")
        }
        else if(data.size() > 1) {
            String matchList = data.collect { "`${ (it.name && it.name != 'N/A') ? it.name : '[Unnamed]' } ${ (it.alias && it.alias != 'N/A') ? '(' + it.alias + ')' : '(No alias)' }`" }.join(', ')
            manager.sendMessage(iMessage.channel, "Sorry, I found more than one match for that. Possibilities are: $matchList")
        }
        else if(data) {
            String nameString = "${ (data[0].name && data[0].name != 'N/A') ? data[0].name : '[Unnamed]' } ${ (data[0].alias && data[0].alias != 'N/A') ? '(' + data[0].alias + ')' : '(No alias)' }"
            manager.sendMessage(iMessage.channel, "Respect thread for ${nameString} is: ${data[0].respectthread}")
        }
        else {
            manager.sendMessage(iMessage.channel, "Sorry, I didn't find any respect threads that match `${message}`.")
        }
    }

    void fetchGroupRespectThread(IMessage iMessage) {
        List pieces = iMessage.content.split(' ')
        pieces.remove(0)
        String message = pieces.join(' ')
        List preParse = getMatchingRows(groups, message, 'name')
        List data = groups.parseFields(['name', 'respectthread'], preParse)
        if(data.size() > 10) {
            manager.sendMessage(iMessage.channel, "Sorry, I found ${data.size()} matches for that. Please try to be more specific in your searching.")
        }
        else if(data.size() > 1) {
            String matchList = data.collect { "`${it.name}`" }.join(', ')
            manager.sendMessage(iMessage.channel, "Sorry, I found more than one match for that. Possibilities are: $matchList")
        }
        else if(data) {
            manager.sendMessage(iMessage.channel, "Respect thread for ${message} is: ${data[0].respectthread}")
        }
        else {
            manager.sendMessage(iMessage.channel, "Sorry, I didn't find any group respect threads that match `${message}`.")
        }
    }

    List getMatchingRows(SpreadsheetConnector connector, String query, String... columns) {
        List toReturn = []
        List fromSearchOne = [], fromSearchTwo = [], fromSearchThree = []
        columns.each { column ->
            List temp = connector.findAll(column, query, 'equals_insensitive')
            fromSearchOne += temp
            if(!temp) {
                temp = connector.findAll(column, query, 'contains_as_word')
                fromSearchTwo += temp
            }
            if(!temp) {
                temp = connector.findAll(column, query, 'contains')
                fromSearchThree += temp
            }
        }
        toReturn += (fromSearchOne) ?: (fromSearchTwo) ?: fromSearchThree
        return (toReturn as Set) as List
    }
}