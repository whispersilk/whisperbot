package whisperbot.data

class ServerStatus {
    String serverId
    boolean enabled
    List channels

    ServerStatus() { }

    ServerStatus(String serverId, boolean enabled) {
        this.serverId = serverId
        this.enabled = enabled
    }

    boolean enabledOnChannel(String channelId) {
        ChannelStatus channel = findChannelById(channelId)
        return channel ? channel.enabled : enabled
    }

    void setEnabledOnChannel(String channelId, boolean enabled) {
        ChannelStatus channel = findByChannelId(channelId)
        if(!channel) {
            channels << new ChannelStatus(channelId, enabled)
        }
        else {
            channel.enabled = enabled
        }
    }

    private ChannelStatus findChannelById(String channelId) {
        return channels.find { it.channelId == channelId }
    }
}