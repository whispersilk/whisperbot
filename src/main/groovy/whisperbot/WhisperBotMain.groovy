package whisperbot

import sx.blah.discord.api.ClientBuilder
import sx.blah.discord.api.IDiscordClient
import sx.blah.discord.util.DiscordException

class WhisperBotMain {

    static void main(String... args) throws DiscordException {
        String botToken = this.getClass().getResource('/bot_token.txt').getContent().getText() // getResource gives a URL, getContent gives an InputStream, getResource gives a String.
        ModuleManager manager = new ModuleManager()
        manager.init(new ClientBuilder().withToken(botToken).login())
        manager.client.getDispatcher().registerListener(manager)
    }
}