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
import com.vexsoftware.nuvotifier.standalone.utils.EnvironmentUtil;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class ConfigurationModule extends AbstractModule {

    private final Logger logger = LoggerFactory.getLogger(ConfigurationModule.class);

    @Singleton
    @Provides
    public Options getOptions() {
        Options options = new Options();

        options.addOption(CommandArguments.CONFIGURATION);
        options.addOption(CommandArguments.BIND_ADDRESS);
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
    public File provideDefaultConfigurationPath() {
        if (EnvironmentUtil.isUnix() || EnvironmentUtil.isSolaris() || EnvironmentUtil.isMacOS()) {
            return new File("/etc/nuvotifier/");
        }

        return new File("C:\\ProgramData\\nuvotifier\\");
    }

    @Provides
    @Singleton
    public VotifierConfiguration provideVotifierConfiguration(
            @Named("configPath") File configPath,
            CommandLine cli,
            Options options,
            ObjectMapper mapper
    ) {
        try {
            File file = cli.hasOption(CommandArguments.CONFIGURATION)
                    ? cli.getParsedOptionValue(CommandArguments.CONFIGURATION)
                    : new File(configPath, "config.yml");

            if (!configPath.exists()) {
                if (!configPath.mkdirs()) {
                    logger.error("Failed to make default configuration directory at {}", configPath.getAbsolutePath());
                    System.exit(1);
                    return null;
                }

                logger.debug("Made default configuration directory");
            }

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
        } catch (ParseException ex) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(Main.class.getName(), options);
            System.exit(1);
            return null;
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
            formatter.printHelp(Main.class.getName(), options);
            return null;
        }
    }

    @Singleton
    @Provides
    @Named("bindAddress")
    public String getBindAddress(CommandLine cli, VotifierConfiguration config) {
        if (cli.hasOption(CommandArguments.BIND_ADDRESS)) {
            return cli.getOptionValue(CommandArguments.BIND_ADDRESS);
        }

        return config.getHost();
    }

    @Singleton
    @Provides
    @Named("port")
    public int getPort(CommandLine cli, VotifierConfiguration config) {
        if (cli.hasOption(CommandArguments.PORT)) {
            try {
                return cli.getParsedOptionValue(CommandArguments.PORT);
            } catch (ParseException ex) {
                logger.error("Could not parse command line arguments", ex);
            }
        }

        return config.getPort();
    }

    @Singleton
    @Provides
    public InetSocketAddress getInetSocketAddress(@Named("bindAddress") String address, @Named("port") int port) {
        return new InetSocketAddress(address, port);
    }
}
