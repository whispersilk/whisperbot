package whisperbot.data

/**
 * Enum corresponding to event types in sx.blah.discord.handle.impl.events
 * Provides an easy way for custom modules to hook into various events without having to implement @EventSubscriber methods themselves.
 */
enum Events {
    // Went ahead and commented out everything not used right now.
    // AUDIO_PLAY('audio_play'),
    // AUDIO_QUEUED('audio_queued'),
    // AUDIO_RECIEVED('audio_recieved'),
    // AUDIO_STOP('audio_stop'),
    // AUDIO_UNQUEUED('audio_unqueued'),
    // CHANNEL_CREATE('channel_create'),
    // CHANNEL_DELETE('channel_delete'),
    // CHANNEL_UPDATE('channel_update'),
    DISCORD_DISCONNECTED('discord_disconnected'),
    // DISCORD_RECONNECTED('discord_reconnected'),
    // GAME_CHANGE('game_change'),
    // GUILD_CREATE('guild_create'),
    // GUILD_LEAVE('guild_leave'),
    // GUILD_TRANSFER_OWNERSHIP('guild_transfer_ownership'),
    // GUILD_UNAVAILABLE('guild_unavailable'),
    // GUILD_UPDATE('guild_update'),
    // INVITE_RECEIVED('invite_received'),
    MENTION('mention'),
    // MESSAGE_DELETE('message_delete'),
    // MESSAGE_PIN('message_pin'),
    MESSAGE_RECEIVED('message_received'),
    // MESSAGE_SEND('message_send'),
    // MESSAGE_UNPIN('message_unpin'),
    // MESSAGE_UPDATE('message_update'),
    // Module events are commented out because we're ignoring Discord4J's inbuilt modules in favor of our own.
    // MODULE_DISABLED('module_disabled'), 
    // MODULE_ENABLED('module_enabled'),
    // PRESENCE_UPDATED('presence_updated'),
    READY('ready'),
    // ROLE_CREATE('role_create'),
    // ROLE_DELETE('role_delete'),
    // ROLE_UPDATE('role_update'),
    // STATUS_CHANGE('status_change'),
    // TYPING('typing'),
    // USER_BAN('user_ban'),
    // USER_JOIN('user_join'),
    // USER_LEAVE('user_leave'),
    // USER_PARDON('user_pardon'),
    // USER_ROLE_UPDATE('user_role_update'),
    // USER_UPDATE('user_update'),
    // USER__VOICE_CHANNEL_JOIN('user_voice_channel_join'),
    // USER_VOICE_CHANNEL_LEAVE('user_voice_channel_leave'),
    // USER_VOICE_CHANNEL_MOVE('user_voice_channel_move'),
    // USER_VOICE_STATE_UPDATE('user_voice_state_update'),
    // VOICE_CHANNEL_CREATE('voice_channel_create'),
    // VOICE_CHANNEL_DELETE('voice_channel_delete'),
    // VOICE_CHANNEL_UPDATE('voice_channel_update'),
    // VOICE_DISCONNECTED('voice_disconnected'),
    // VOICE_PING('voice_ping'),
    // VOICE_USER_SPEAKING('voice_user_speaking')

    String value

    Events(String value) {
        this.value = value
    }

    static String toString(Events event) {
        return event.value
    }

    static Events fromString(String value) {
        return Events.values().find { it.value == value }
    }
}