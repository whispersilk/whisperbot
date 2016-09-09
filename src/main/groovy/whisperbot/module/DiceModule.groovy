package whisperbot.module

import sx.blah.discord.api.IDiscordClient
import sx.blah.discord.handle.obj.IMessage

import whisperbot.ModuleManager
import whisperbot.data.Command
import whisperbot.data.Events
import whisperbot.data.Link
import whisperbot.util.FileConnector
import whisperbot.util.SpreadsheetConnector

class DiceModule extends Module {

    private static final String CHOOSE_SEPARATOR = '\\.|\\||,' // This is a regex string, but special characters must be escaped using \\

    DiceModule() {
        name = 'dice'
        enabledByDefault = true
        commands = [
            [
                name: 'roll', on: [Events.MESSAGE_RECEIVED] as Set, enabledByDefault: true,
                action: { message -> if(messageStartsWith(message.content, '~roll')) doRoll(message) }
            ] as Command, 
            [
                name: 'choose', on: [Events.MESSAGE_RECEIVED] as Set, enabledByDefault: true,
                info: '`~choose choices`. Selects one of a list of choices. Choices can be separated by commas, periods, or |.',
                action: { message -> if(messageStartsWith(message.content, '~choose')) doChoose(message) }
            ] as Command
        ]
    }

    void init(ModuleManager manager) {
        super.init(manager)
    }

    void doChoose(IMessage iMessage) {
        List pieces = iMessage.content.split(' ')
        pieces.remove(0)
        List options = pieces.join(' ').split(CHOOSE_SEPARATOR).collect { it.trim() }
        String option = options[(Math.random() * options.size) as int]
        manager.sendMessage(iMessage.channel, "Of those options, I chose: $option")
    }

    Map parseDiceRoll(String rollString) {
        Map toReturn = [original: rollString.trim()]
        getModeAndNum(toReturn)
        if(toReturn.error) {
            return toReturn
        }
        getSidesAndBonus(toReturn)
        if(toReturn.error) {
            return toReturn
        }
    }

    void getModeAndNum(Map map) {
        String s = map.original
        String numRolls = s.find(/^\d+/)
        map['rolls'] = numRolls ? numRolls.toInteger() : 1
        s = numRolls ? s.replaceFirst(numRolls, '').trim() : s.trim()
        if(s[0] == 'd') {
            map['mode'] = 'dice'
        }
        else if(s[0] == 'h') {
            map['mode'] == 'highest'
        }
        else if(s[0] == 'a') {
            map['mode'] == 'add'
        }
        else {
            map['error'] == 'No roll type specified.'
        }
        map.original = s.substring(1).trim()
    }

    void getSidesAndBonus(Map map) {
        String s = map.original
        String numSides = s.find(/^\d+/)
        if(!numSides) {
            map['error'] = 'You need to specify a number of sides for the dice to roll.'
            return
        }
        map['sides'] = numSides.toInteger()
        s = s.replaceFirst(numSides, '').trim()

    }






    private static final int MAX_ROLLS = 500
    
    void doRoll(IMessage iMessage) {
        List pieces = iMessage.content.split(' ')
        pieces.remove(0)
        String roll = pieces.join(' ')
        if(!pieces) {
            manager.sendMessage(iMessage.channel, 'To roll you should use [X]dY [+/- mod] [tag]. X, Y, and mod should be integers.')
            return
        }
        String mode = ''
        String numAndSides = roll.find(/^[A-Za-z0-9]*d[A-Za-z0-9]+/)
        if(!numAndSides) {
            numAndSides = roll.find(/^[A-Za-z0-9]*h[A-Za-z0-9]+/)
            roll = roll.replaceFirst(/^[A-Za-z0-9]*h[A-Za-z0-9]+/, '')
            mode = 'h'
        }
        else {
            roll = roll.replaceFirst(/^[A-Za-z0-9]*d[A-Za-z0-9]+/, '')
            mode = 'd'
        }
        int rolls, sides
        String error
        (rolls, sides, error) = getNumAndSides(numAndSides, mode)
        if(error) {
            manager.sendMessage(iMessage.channel, error)
            return
        }
        if(rolls > MAX_ROLLS) {
            manager.sendMessage(iMessage.channel, "Sorry, I won't let you roll more than ${MAX_ROLLS} dice at once.")
            return
        }
        String modAndMult = roll.find(/^[\s]*[\+-][\s]*[A-Za-z0-9]+/)
        roll = roll.replaceFirst(/^[\s]*[\+-][\s]*[A-Za-z0-9]+/, '')
        int mod, mult
        (mod, mult, error) = getModAndMult(modAndMult)
        if(error) {
            manager.sendMessage(iMessage.channel, error)
            return
        }
        String tag = roll.replaceFirst(/^[\s]*/, '')
        if(mode == 'd') {
            showAllRolls(iMessage, rolls, sides, mod, mult, tag)
        }
        else if(mode == 'h') {
            showHighestRoll(iMessage, rolls, sides, mod, mult, tag)
        }
    }

    void showAllRolls(IMessage iMessage, int rolls, int sides, int mod, int mult, String tag) {
        List results = []
        for(int x = 0; x < rolls; x++) {
            results << ((Math.random() * sides + 1 + mod * mult) as int)
            if(results[results.size() - 1] == sides) {
                results[results.size() - 1] = "" + results[results.size() - 1] + ""
            }
        }
        if(rolls < 30) {
            manager.sendMessage(iMessage.channel, "[${results.join(', ')}]${tag ? ' - Tag: ' + tag : ''}")
        }
    }

    void showHighestRoll(IMessage iMessage, int rolls, int sides, int mod, int mult, String tag) {
        int max = 0
        for(int x = 0; x < rolls; x++) {
            int roll = ((Math.random() * sides + 1 + mod * mult) as int)
            if(max < roll) { max = roll }
        }
        manager.sendMessage(iMessage.channel, "Highest of ${rolls}d${sides} is [${max == sides ? '' + max + '' : max}]${tag ? ' - Tag: ' + tag : ''}")
    }

    List getNumAndSides(String numAndSides, String separator) {
        int num, sides
        String error
        List vals = numAndSides.split(separator) as List
        try {
            num = (vals[0]) ? vals[0] as int : 1
        }
        catch(NumberFormatException e) {
            error = 'Number of rolls needs to be nothing or an integer.'
        }
        try {
            sides = vals[1] as int
        }
        catch(NumberFormatException e) {
            error = 'Number of sides needs to be an integer.'
        }
        return [num, sides, error]
    }

    List getModAndMult(String modAndMult) {
        int mod, mult
        String error
        if(!modAndMult) {
            return [0, 1, error]
        }
        mult = (modAndMult.indexOf('+') > -1) ? 1 : -1
        try {
            String modOnly = modAndMult.replaceFirst(/^[\s]*[\+-][\s]*/, '')
            mod = modOnly as int
        }
        catch(NumberFormatException e) {
            error = 'Modifier value needs to be an integer.'
        }
        return [mod, mult, error]
    }
}