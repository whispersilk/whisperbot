package whisperbot.data

class Command {
    String name
    Set on
    Closure action
    String info
    Status status
    boolean enabledByDefault

    void init(String moduleName) {
        status = new Status("config/${moduleName}/${name}.json", enabledByDefault)
    }

    boolean registers(Events event) {
        return (on.find { it == event } != null)
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
}