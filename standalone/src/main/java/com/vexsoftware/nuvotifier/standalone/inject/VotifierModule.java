package com.vexsoftware.nuvotifier.standalone.inject;

import com.google.inject.AbstractModule;
import com.vexsoftware.nuvotifier.standalone.inject.config.ConfigurationModule;
import com.vexsoftware.nuvotifier.standalone.inject.server.VotifierServerModule;
import com.vexsoftware.nuvotifier.standalone.service.Service;
import com.vexsoftware.nuvotifier.standalone.service.impl.NuVotifierService;

public class VotifierModule extends AbstractModule {

    private final String[] args;

    public VotifierModule(String[] args) {
        this.args = args;
    }

    @Override
    protected void configure() {
        this.bind(String[].class).toInstance(this.args);
        this.bind(Service.class).to(NuVotifierService.class);

        this.install(new ConfigurationModule());
        this.install(new VotifierServerModule());
    }
}
