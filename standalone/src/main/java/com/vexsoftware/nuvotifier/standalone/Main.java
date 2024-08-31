package com.vexsoftware.nuvotifier.standalone;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.vexsoftware.nuvotifier.standalone.inject.VotifierModule;
import com.vexsoftware.nuvotifier.standalone.service.Service;

public class Main {

    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new VotifierModule(args));
        Service service = injector.getInstance(Service.class);
        service.start();

        while (true) {
            // Keep the thing running
        }
    }
}