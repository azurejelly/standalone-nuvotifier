package com.vexsoftware.nuvotifier.standalone.bootstrap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.vexsoftware.nuvotifier.standalone.config.VotifierConfiguration;
import com.vexsoftware.nuvotifier.standalone.config.options.CommandArguments;
import com.vexsoftware.nuvotifier.standalone.plugin.StandaloneVotifierPlugin;
import com.vexsoftware.nuvotifier.standalone.plugin.builder.VotifierServerBuilder;
import com.vexsoftware.votifier.net.protocol.v1crypto.RSAIO;
import com.vexsoftware.votifier.net.protocol.v1crypto.RSAKeygen;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class VotifierBootstrap {

    private final Logger logger;
    private final String[] args;
    private File directory;
    private VotifierConfiguration config;
    private CommandLine commandLine;
    private Options options;
    private InetSocketAddress socket;
    private ObjectMapper mapper;
    private StandaloneVotifierPlugin plugin;

    public VotifierBootstrap(String[] args) {
        this.args = args;
        this.logger = LoggerFactory.getLogger(VotifierBootstrap.class);
    }

    public void init() {
        this.options = new Options();
        this.options.addOption(CommandArguments.CONFIG_FOLDER);
        this.options.addOption(CommandArguments.HOST);
        this.options.addOption(CommandArguments.PORT);

        this.mapper = new ObjectMapper(new YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER));
        this.mapper.findAndRegisterModules();

        try {
            CommandLineParser parser = new DefaultParser();
            this.commandLine = parser.parse(options, args);
        } catch (ParseException ex) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java -jar nuvotifier-standalone.jar [OPTIONS]", options);
            System.exit(1);
        }

        if (commandLine.hasOption(CommandArguments.CONFIG_FOLDER)) {
            try {
                this.directory = commandLine.getParsedOptionValue(CommandArguments.CONFIG_FOLDER);
                if (!this.directory.exists() && !this.directory.mkdirs()) {
                    logger.error("Failed to create configuration directory at '{}'", this.directory.getAbsolutePath());
                    System.exit(1);
                }
            } catch (ParseException ex) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("java -jar nuvotifier-standalone.jar [OPTIONS]", options);
                System.exit(1);
            } catch (SecurityException ex) {
                logger.error("An exception was caught while attempting to create the configuration directory", ex);
                System.exit(1);
            }
        } else {
            Path currentRelativePath = Paths.get(".");
            this.directory = new File(currentRelativePath.toFile(), "config");

            if (!directory.exists() && !directory.mkdirs()) {
                logger.error("Failed to create configuration directory at {}", directory.getAbsolutePath());
                System.exit(1);
            }
        }

        try {
            File file = new File(directory, "config.yml");

            if (!file.exists()) {
                InputStream resource = this.getClass().getClassLoader().getResourceAsStream("config.yml");
                if (resource == null) {
                    logger.error("Failed to find default configuration file in JAR.");
                    System.exit(1);
                }

                Files.copy(resource, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                logger.debug("Copied default configuration file from JAR.");
            }

            this.config = mapper.readValue(file, VotifierConfiguration.class);
        } catch (IOException e) {
            logger.error("Failed to read or copy defaults to configuration file:", e);
            System.exit(1);
        }

        String address = commandLine.hasOption(CommandArguments.HOST)
                ? commandLine.getOptionValue(CommandArguments.HOST)
                : config.getHost();

        if (commandLine.hasOption(CommandArguments.PORT)) {
            try {
                String str = commandLine.getOptionValue(CommandArguments.PORT);
                this.socket = new InetSocketAddress(address, Integer.parseInt(str));
            } catch (NumberFormatException ex) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("java -jar nuvotifier-standalone.jar [OPTIONS]", options);
                System.exit(1);
            }
        } else {
            this.socket = new InetSocketAddress(address, config.getPort());
        }

        File rsaFolder = new File(directory, "rsa" + File.separator);
        if (!rsaFolder.exists()) {
            if (!rsaFolder.mkdirs()) {
                logger.error(
                        "Cannot make RSA folder at {}, unable to continue creating standalone Votifier server.",
                        rsaFolder.getAbsolutePath()
                );

                System.exit(1);
            }

            try {
                RSAIO.save(rsaFolder, RSAKeygen.generate(2048));
                logger.info("Generated new RSA key pair at {}", rsaFolder.getAbsolutePath());
            } catch (Exception e) {
                logger.error("Failed to generate RSA key pair at {}", rsaFolder.getAbsolutePath(), e);
                System.exit(1);
            }
        }

        try {
            VotifierServerBuilder builder = new VotifierServerBuilder()
                    .bind(socket)
                    .v1KeyFolder(rsaFolder)
                    .disableV1Protocol(config.isDisableV1Protocol())
                    .debug(config.isDebug())
                    .backendServers(config.getBackendServers());

            this.config.getTokens().forEach(builder::addToken);
            this.plugin = builder.create();
        } catch (Exception ex) {
            logger.error("Failed to build the standalone Votifier server", ex);
            System.exit(1);
        }

        this.plugin.start(ex -> {
            if (ex == null) {
                return;
            }

            logger.error("Could not initialize standalone Votifier server", ex);
            System.exit(1);
        });
    }

    public void shutdown() {
        this.plugin.shutdown();
    }

    public Logger logger() {
        return logger;
    }

    public Options options() {
        return options;
    }

    public ObjectMapper mapper() {
        return mapper;
    }

    public StandaloneVotifierPlugin plugin() {
        return plugin;
    }
}
