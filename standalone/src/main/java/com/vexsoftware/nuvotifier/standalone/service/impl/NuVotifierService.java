package com.vexsoftware.nuvotifier.standalone.service.impl;

import com.google.inject.Inject;
import com.vexsoftware.nuvotifier.standalone.service.Service;
import com.vexsoftware.nuvotifier.standalone.plugin.StandaloneVotifierPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NuVotifierService implements Service {

    private final Logger logger = LoggerFactory.getLogger(NuVotifierService.class);

    @Inject
    private StandaloneVotifierPlugin plugin;

    @Override
    public void start() {
        plugin.start(ex -> {
            if (ex == null) {
                return;
            }

            logger.error("Could not initialize standalone Votifier server", ex);
            System.exit(1);
        });
    }
}
