package com.vexsoftware.nuvotifier.standalone.config.redis;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vexsoftware.nuvotifier.standalone.config.redis.pool.RedisPoolVotifierConfiguration;

public class RedisVotifierConfiguration {

    private final boolean enabled;
    private final String address;
    private final int port;
    private final String password;
    private final String channel;

    @JsonProperty("pool-settings")
    private final RedisPoolVotifierConfiguration poolSettings;

    public RedisVotifierConfiguration(
            boolean enabled, String address, int port,
            String password, String channel, RedisPoolVotifierConfiguration poolSettings
    ) {
        this.enabled = enabled;
        this.address = address;
        this.port = port;
        this.password = password;
        this.channel = channel;
        this.poolSettings = poolSettings;
    }

    public RedisVotifierConfiguration() {
        this.enabled = false;
        this.address = "127.0.0.1";
        this.port = 6379;
        this.password = "";
        this.channel = "nuvotifier:votes";
        this.poolSettings = new RedisPoolVotifierConfiguration();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public String getPassword() {
        return password;
    }

    public String getChannel() {
        return channel;
    }

    public RedisPoolVotifierConfiguration getPoolSettings() {
        return poolSettings;
    }
}
