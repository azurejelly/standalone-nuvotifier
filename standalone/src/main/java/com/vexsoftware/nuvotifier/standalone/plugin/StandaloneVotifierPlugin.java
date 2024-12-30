package com.vexsoftware.nuvotifier.standalone.plugin;

import com.vexsoftware.nuvotifier.standalone.config.redis.RedisVotifierConfiguration;
import com.vexsoftware.nuvotifier.standalone.config.redis.pool.RedisPoolVotifierConfiguration;
import com.vexsoftware.nuvotifier.standalone.config.server.BackendServer;
import com.vexsoftware.nuvotifier.standalone.logger.StandaloneVotifierLoggingAdapter;
import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.net.VotifierServerBootstrap;
import com.vexsoftware.votifier.net.VotifierSession;
import com.vexsoftware.votifier.platform.LoggingAdapter;
import com.vexsoftware.votifier.platform.VotifierPlugin;
import com.vexsoftware.votifier.platform.scheduler.ScheduledExecutorServiceVotifierScheduler;
import com.vexsoftware.votifier.platform.scheduler.VotifierScheduler;
import com.vexsoftware.votifier.support.forwarding.ForwardingVoteSource;
import com.vexsoftware.votifier.support.forwarding.proxy.ProxyForwardingVoteSource;
import com.vexsoftware.votifier.support.forwarding.redis.RedisCredentials;
import com.vexsoftware.votifier.support.forwarding.redis.RedisForwardingVoteSource;
import com.vexsoftware.votifier.support.forwarding.redis.RedisPoolConfiguration;
import com.vexsoftware.votifier.util.KeyCreator;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.security.Key;
import java.security.KeyPair;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class StandaloneVotifierPlugin implements VotifierPlugin {

    private final RedisVotifierConfiguration redis;
    private final boolean debug;
    private final Map<String, Key> tokens;
    private final KeyPair v1Key;
    private final InetSocketAddress bind;
    private final VotifierScheduler scheduler;
    private final Map<String, BackendServer> backendServers;
    private final boolean disableV1Protocol;
    private ForwardingVoteSource forwardingMethod;
    private VotifierServerBootstrap bootstrap;

    public StandaloneVotifierPlugin(
            boolean debug, Map<String, Key> tokens,
            KeyPair v1Key, InetSocketAddress bind,
            Map<String, BackendServer> backendServers,
            boolean disableV1Protocol,
            RedisVotifierConfiguration redis
    ) {
        this.debug = debug;
        this.bind = bind;
        this.tokens = Map.copyOf(tokens);
        this.v1Key = v1Key;
        this.backendServers = backendServers;
        this.scheduler = new ScheduledExecutorServiceVotifierScheduler(Executors.newScheduledThreadPool(1));
        this.disableV1Protocol = disableV1Protocol;
        this.redis = redis;
    }

    public void start(Consumer<Throwable> error) {
        if (bootstrap != null) {
            bootstrap.shutdown();
        }

        this.bootstrap = new VotifierServerBootstrap(bind.getHostString(), bind.getPort(), this, disableV1Protocol);
        this.bootstrap.start(error);
        this.makeForwardingSource(backendServers);
    }

    public void shutdown() {
        if (bootstrap != null) {
            bootstrap.shutdown();
        }
    }

    private void makeForwardingSource(Map<String, BackendServer> backendServers) {
        if (redis != null && redis.isEnabled()) {
            getPluginLogger().info("Using Redis as the vote forwarding method.");

            RedisCredentials redisCredentials = RedisCredentials.builder()
                    .host(redis.getAddress())
                    .port(redis.getPort())
                    .password(redis.getPassword())
                    .channel(redis.getChannel())
                    .build();

            RedisPoolVotifierConfiguration pool = redis.getPoolSettings();
            RedisPoolConfiguration redisPoolConfiguration = RedisPoolConfiguration.builder()
                    .timeout(pool.getTimeout())
                    .maxTotal(pool.getMaxTotal())
                    .maxIdle(pool.getMaxIdle())
                    .minIdle(pool.getMinIdle())
                    .minEvictableIdleTime(pool.getMinEvictableIdleTime())
                    .timeBetweenEvictionRuns(pool.getTimeBetweenEvictionRuns())
                    .numTestsPerEvictionRun(pool.getNumTestsPerEvictionRun())
                    .blockWhenExhausted(pool.isBlockWhenExhausted())
                    .build();

            this.forwardingMethod = new RedisForwardingVoteSource(redisCredentials, redisPoolConfiguration);
        } else {
            List<ProxyForwardingVoteSource.BackendServer> serverList = new ArrayList<>();
            for (Map.Entry<String, BackendServer> entry : backendServers.entrySet()) {
                String key = entry.getKey();
                BackendServer server = entry.getValue();

                InetAddress address;
                try {
                    address = InetAddress.getByName(server.getAddress());
                } catch (UnknownHostException ex) {
                    getPluginLogger().warn("Could not look up getAddress {} for server '{}'. Ignoring!", server.getAddress(), key);
                    continue;
                }

                Key token;
                try {
                    token = KeyCreator.createKeyFrom(server.getToken());
                } catch (IllegalArgumentException ex) {
                    getPluginLogger().error("Could not add proxy target '{}'. Is the provided token valid?" +
                            "Votes will not be forwarded to this server!", key, ex);

                    continue;
                }

                InetSocketAddress socket = new InetSocketAddress(address, server.getPort());
                serverList.add(new ProxyForwardingVoteSource.BackendServer(key, socket, token));
            }

            if (!serverList.isEmpty()) {
                getPluginLogger().info(
                        "Forwarding votes from this NuVotifier instance to another {} valid backend servers.",
                        serverList.size()
                );
            }

            this.forwardingMethod = bootstrap.createForwardingSource(serverList, null);
        }
    }

    @Override
    public Map<String, Key> getTokens() {
        return tokens;
    }

    @Override
    public KeyPair getProtocolV1Key() {
        return v1Key;
    }

    @Override
    public LoggingAdapter getPluginLogger() {
        return new StandaloneVotifierLoggingAdapter(this.getClass());
    }

    @Override
    public VotifierScheduler getScheduler() {
        return scheduler;
    }

    @Override
    public void onVoteReceived(Vote vote, VotifierSession.ProtocolVersion protocolVersion, String remoteAddress) {
        if (debug) {
            getPluginLogger().info("Received protocol {} vote record for username {} from service {} @ {}",
                    protocolVersion == VotifierSession.ProtocolVersion.ONE ? "v1" : "v2",
                    vote.getUsername(),
                    vote.getServiceName(),
                    vote.getAddress()
            );
        }

        if (forwardingMethod != null) {
            forwardingMethod.forward(vote);
        }
    }

    @Override
    public void onError(Throwable throwable, boolean alreadyHandledVote, String remoteAddress) {
        getPluginLogger().error("Exception caught while processing vote from " + remoteAddress
                + " (already handled: " + alreadyHandledVote + ")", throwable);
    }

    @Override
    public boolean isDebug() {
        return debug;
    }
}
