package whisperbot.data

class ChannelStatus {
    String channelId
    boolean enabled

    ChannelStatus() { }

    ChannelStatus(String channelId, boolean enabled) {
        this.channelId = channelId
        this.enabled = enabled
    }
}