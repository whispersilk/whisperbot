package whisperbot.data

import whisperbot.util.FileConnector

class Status {

    List servers
    
    boolean defaultEnabledState

    FileConnector config
    private String fileLocation // Should be 'config/[moduleName].json' for modules, or 'config/[moduleName]/[commandName].json' for commands.

    Status(String fileLocation, boolean defaultEnabledState) {
        this.fileLocation = fileLocation
        this.defaultEnabledState = defaultEnabledState
        config = new FileConnector(fileLocation)
        servers = config.load('list').collect { it as ServerStatus }
    }

    boolean enabledOnServer(String serverId) {
        return findServerById(serverId).enabled
    }

    boolean enabledOnChannel(String serverId, String channelId) {
        return findServerById(serverId).enabledOnChannel(channelId)
    }

    void setEnabledOnServer(String serverId, boolean enabled) {
        findServerById(serverId).enabled = enabled
        config.save(servers)
    }

    void setEnabledOnChannel(String serverId, String channelId, boolean enabled) {
        findServerById(serverId).setEnabledOnChannel(channelId, enabled)
        config.save(servers)
    }

    ServerStatus findServerById(String serverId) {
        ServerStatus server = servers.find { it.serverId == serverId }
        if(!server) {
            server = new ServerStatus(serverId, defaultEnabledState)
            servers << server
            config.save(servers)
        }
        return server
    }
}