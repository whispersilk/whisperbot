package whisperbot.module

import sx.blah.discord.api.IDiscordClient
import sx.blah.discord.handle.obj.IMessage

import whisperbot.ModuleManager
import whisperbot.data.Command
import whisperbot.data.Events
import whisperbot.data.Link
import whisperbot.util.FileConnector
import whisperbot.util.SpreadsheetConnector

class LinkerModule extends Module {

    private List links
    private FileConnector linkFile

    LinkerModule() {
        name = 'linker'
        enabledByDefault = true
        commands = [
            [
                name: 'add_link', on: [Events.MESSAGE_RECEIVED] as Set, enabledByDefault: true,
                info: '`~addlink name url`. Adds a link with the given name to the given url. Names and urls should be one word.',
                action: { message -> if(messageStartsWith(message.content, '~addlink')) addLink(message) }
            ] as Command,
            [
                name: 'post_link', on: [Events.MESSAGE_RECEIVED] as Set, enabledByDefault: true,
                info: '`~link name`. Posts the link with the given name.',
                action: { message -> if(messageStartsWith(message.content, '~link')) postLink(message) }
            ] as Command,
            [
                name: 'remove_link', on: [Events.MESSAGE_RECEIVED] as Set, enabledByDefault: true,
                info: '`~delink name`. Removes the link with the given name, if it exists.',
                action: { message -> if(messageStartsWith(message.content, '~delink')) removeLink(message) }
            ] as Command,
            [
                name: 'list_links', on: [Events.MESSAGE_RECEIVED] as Set, enabledByDefault: true,
                info: '`~links` or `~showlinks`. Lists the names of all stored links.',
                action: { message -> if(messageStartsWith(message.content, '~links', '~showlinks')) listLinks(message) }
            ] as Command,
            [
                name: 'link_avatar', on: [Events.MESSAGE_RECEIVED] as Set, enabledByDefault: true,
                info: '`~av @nick`. Links the avatar of the mentioned user.',
                action: { message -> if(messageStartsWith(message.content, '~av')) linkAvatar(message) }
            ] as Command
        ]
    }

    void init(ModuleManager manager) {
        super.init(manager)
        linkFile = new FileConnector("${name}/links.json")
        links = linkFile.load('list').collect { it as Link }
    }

    void addLink(IMessage iMessage) {
        List pieces = iMessage.content.split(' ')
        pieces.remove(0)
        if(pieces.size() != 2) {
            manager.sendMessage(iMessage.channel, 'I need two parameters, like so: [link name, with no spaces] [link url, no spaces]')
            return
        }
        else if(findLinkByNameAndServer(pieces[0], iMessage.guild.id)) {
            manager.sendMessage(iMessage.channel, 'There\'s already a link with that name.')
            return
        }
        links << new Link([serverId: iMessage.guild.id, serverName: iMessage.guild.name, name: pieces[0], url: pieces[1]])
        linkFile.save(links)
    }

    void postLink(IMessage iMessage) {
        List pieces = iMessage.content.split(' ')
        pieces.remove(0)
        if(!pieces) {
            manager.sendMessage(iMessage.channel, 'HYAAAAH!')
            return
        }
        else if(pieces.size() == 1) {
            Link link = findLinkByNameAndServer(pieces[0], iMessage.guild.id)
            manager.sendMessage(iMessage.channel, link.url)
        }
    }

    void removeLink(IMessage iMessage) {
        List pieces = iMessage.content.split(' ')
        pieces.remove(0)
        if(pieces.size() == 1) {
            Link link = findLinkByNameAndServer(pieces[0], iMessage.guild.id)
            links = links - link
            linkFile.save(links)
            manager.sendMessage(iMessage.channel, "Removed link `${link.name}`.")
        }
    }

    void listLinks(IMessage iMessage) {
        List linkNames = links.findAll { link -> link.serverId == iMessage.guild.id }.collect { it.name }
        manager.sendMessage(iMessage.channel, "Links: `${linkNames.join('`, `')}`")
    }

    void linkAvatar(IMessage iMessage) {
        manager.sendMessage(iMessage.channel, iMessage.mentions[0].avatarURL)
    }

    Link findLinkByNameAndServer(String linkName, String serverId) {
        return links.find { link -> link.serverId == serverId && link.name.equalsIgnoreCase(linkName) }
    }
}