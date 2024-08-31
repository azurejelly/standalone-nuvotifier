package com.vexsoftware.nuvotifier.standalone.inject.server;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.vexsoftware.nuvotifier.standalone.config.VotifierConfiguration;
import com.vexsoftware.votifier.net.protocol.v1crypto.RSAIO;
import com.vexsoftware.votifier.net.protocol.v1crypto.RSAKeygen;
import com.vexsoftware.nuvotifier.standalone.plugin.StandaloneVotifierPlugin;
import com.vexsoftware.nuvotifier.standalone.plugin.builder.VotifierServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.InetSocketAddress;

public class VotifierServerModule extends AbstractModule {

    private final Logger logger = LoggerFactory.getLogger(VotifierServerModule.class);

    @Singleton
    @Provides
    public StandaloneVotifierPlugin provideStandaloneVotifierPlugin(
            InetSocketAddress address,
            @Named("configPath") File configPath,
            VotifierConfiguration config
    ) {
        try {
            File rsaFolder = new File(configPath, "rsa" + File.separator);
            if (!rsaFolder.exists()) {
                if (!rsaFolder.mkdirs()) {
                    logger.error(
                            "Cannot make RSA folder at {}, unable to continue creating standalone Votifier server.",
                            rsaFolder.getAbsolutePath()
                    );

                    System.exit(1);
                    return null;
                }

                RSAIO.save(rsaFolder, RSAKeygen.generate(2048));
                logger.info("Generated new RSA key pair at {}", rsaFolder.getAbsolutePath());
            }

            VotifierServerBuilder builder = new VotifierServerBuilder()
                    .bind(address)
                    .v1KeyFolder(rsaFolder)
                    .debug(config.isDebug())
                    .backendServers(config.getBackendServers());

            config.getTokens().forEach(builder::addToken);
            return builder.create();
        } catch (Exception ex) {
            logger.error("Could not create standalone Votifier server", ex);
            System.exit(1);
            return null;
        }
    }
}

