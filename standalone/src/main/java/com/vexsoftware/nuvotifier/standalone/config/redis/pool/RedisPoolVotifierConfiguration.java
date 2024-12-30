package com.vexsoftware.nuvotifier.standalone.config.redis.pool;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RedisPoolVotifierConfiguration {

    private final int timeout;

    @JsonProperty("max-total")
    private final int maxTotal;

    @JsonProperty("max-idle")
    private final int maxIdle;

    @JsonProperty("min-idle")
    private final int minIdle;

    @JsonProperty("min-evictable-idle-time")
    private final int minEvictableIdleTime;

    @JsonProperty("time-between-eviction-runs")
    private final int timeBetweenEvictionRuns;

    @JsonProperty("num-tests-per-eviction-run")
    private final int numTestsPerEvictionRun;

    @JsonProperty("block-when-exhausted")
    private final boolean blockWhenExhausted;

    public RedisPoolVotifierConfiguration(
            int timeout, int maxTotal, int maxIdle,
            int minIdle, int minEvictableIdleTime, int timeBetweenEvictionRuns,
            int numTestsPerEvictionRun, boolean blockWhenExhausted
    ) {
        this.timeout = timeout;
        this.maxTotal = maxTotal;
        this.maxIdle = maxIdle;
        this.minIdle = minIdle;
        this.minEvictableIdleTime = minEvictableIdleTime;
        this.timeBetweenEvictionRuns = timeBetweenEvictionRuns;
        this.numTestsPerEvictionRun = numTestsPerEvictionRun;
        this.blockWhenExhausted = blockWhenExhausted;
    }

    public RedisPoolVotifierConfiguration() {
        this.timeout = 5000;
        this.maxTotal = 128;
        this.maxIdle = 128;
        this.minIdle = 16;
        this.minEvictableIdleTime = 60000;
        this.timeBetweenEvictionRuns = 30000;
        this.numTestsPerEvictionRun = 3;
        this.blockWhenExhausted = true;
    }

    public int getTimeout() {
        return timeout;
    }

    public int getMaxTotal() {
        return maxTotal;
    }

    public int getMaxIdle() {
        return maxIdle;
    }

    public int getMinIdle() {
        return minIdle;
    }

    public int getMinEvictableIdleTime() {
        return minEvictableIdleTime;
    }

    public int getTimeBetweenEvictionRuns() {
        return timeBetweenEvictionRuns;
    }

    public int getNumTestsPerEvictionRun() {
        return numTestsPerEvictionRun;
    }

    public boolean isBlockWhenExhausted() {
        return blockWhenExhausted;
    }
}
