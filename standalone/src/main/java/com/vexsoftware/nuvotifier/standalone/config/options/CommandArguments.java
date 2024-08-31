package com.vexsoftware.nuvotifier.standalone.config.options;

import org.apache.commons.cli.Option;

import java.io.File;
import java.net.InetSocketAddress;

public class CommandArguments {

    public static final Option BIND_ADDRESS = Option.builder("b")
            .desc("The address NuVotifier should bind to.")
            .type(InetSocketAddress.class)
            .longOpt("bind")
            .required(false)
            .build();

    public static final Option PORT = Option.builder("p")
            .desc("The port NuVotifier should bind to.")
            .type(int.class)
            .longOpt("port")
            .required(false)
            .build();

    public static final Option CONFIGURATION = Option.builder("c")
            .desc("Configuration file location. Defaults to /etc/nuvotifier/config.yml or C:\\ProgramData\\nuvotifier\\config.yml under Windows.")
            .required(false)
            .type(File.class)
            .longOpt("config")
            .build();
}
