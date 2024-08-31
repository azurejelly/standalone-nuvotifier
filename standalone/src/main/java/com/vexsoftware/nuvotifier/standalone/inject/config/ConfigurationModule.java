package com.vexsoftware.nuvotifier.standalone.inject.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.vexsoftware.nuvotifier.standalone.Main;
import com.vexsoftware.nuvotifier.standalone.config.VotifierConfiguration;
import com.vexsoftware.nuvotifier.standalone.config.options.CommandArguments;
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

public class ConfigurationModule extends AbstractModule {

    private final Logger logger = LoggerFactory.getLogger(ConfigurationModule.class);

    @Singleton
    @Provides
    public Options getOptions() {
        Options options = new Options();

        options.addOption(CommandArguments.CONFIG_FOLDER);
        options.addOption(CommandArguments.HOST);
        options.addOption(CommandArguments.PORT);

        return options;
    }

    @Singleton
    @Provides
    public ObjectMapper provideYAMLMapper() {
        ObjectMapper mapper = new ObjectMapper(
                new YAMLFactory()
                        .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
        );

        mapper.findAndRegisterModules();
        return mapper;
    }

    @Provides
    @Singleton
    @Named("configPath")
    public File provideConfigurationDirectory(CommandLine cli, Options options) {
        if (cli.hasOption(CommandArguments.CONFIG_FOLDER)) {
            try {
                File file = cli.getParsedOptionValue(CommandArguments.CONFIG_FOLDER);
                System.out.println(file != null ? "file is not null" : "file is null");
                return cli.getParsedOptionValue(CommandArguments.CONFIG_FOLDER);
            } catch (ParseException ex) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("java -jar nuvotifier-standalone.jar", options);
                System.exit(1);
                return null;
            }
        }

        Path currentRelativePath = Paths.get(".");
        File config = new File(currentRelativePath.toFile(), "config");

        if (!config.exists() && !config.mkdirs()) {
            logger.error("Failed to create configuration directory at {}", config.getAbsolutePath());
            System.exit(1);
            return null;
        }

        return config;
    }

    @Provides
    @Singleton
    public VotifierConfiguration provideVotifierConfiguration(@Named("configPath") File configPath, ObjectMapper mapper) {
        try {
            File file = new File(configPath, "config.yml");

            if (!file.exists()) {
                InputStream resource = this.getClass().getClassLoader().getResourceAsStream("config.yml");
                if (resource == null) {
                    logger.error("Failed to find default configuration file in JAR.");
                    System.exit(1);
                    return null;
                }

                Files.copy(resource, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                logger.debug("Copied default configuration file from JAR.");
            }

            return mapper.readValue(file, VotifierConfiguration.class);
        } catch (IOException e) {
            logger.error("Failed to read or copy defaults to configuration file:", e);
            System.exit(1);
            return null;
        }
    }

    @Singleton
    @Provides
    public CommandLine getCommandLine(Options options, String[] args) {
        try {
            CommandLineParser parser = new DefaultParser();
            return parser.parse(options, args);
        } catch (ParseException ex) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java -jar nuvotifier-standalone.jar", options);
            System.exit(1);
            return null;
        }
    }

    @Provides
    @Singleton
    @Named("bindAddress")
    public String getBindAddress(CommandLine cli, VotifierConfiguration config) {
        if (cli.hasOption(CommandArguments.HOST)) {
            return cli.getOptionValue(CommandArguments.HOST);
        }

        return config.getHost();
    }

    @Provides
    @Singleton
    @Named("port")
    public int getPort(CommandLine cli, Options options, VotifierConfiguration config) {
        if (cli.hasOption(CommandArguments.PORT)) {
            try {
                String str = cli.getOptionValue(CommandArguments.PORT);
                return Integer.parseInt(str);
            } catch (NumberFormatException ex) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("java -jar nuvotifier-standalone.jar", options);
                System.exit(1);
            }
        }

        return config.getPort();
    }

    @Singleton
    @Provides
    public InetSocketAddress getInetSocketAddress(@Named("bindAddress") String address, @Named("port") int port) {
        System.out.println(address != null ? "address is not null" : "address is null");
        return new InetSocketAddress(address, port);
    }
}
