package com.vexsoftware.nuvotifier.standalone;

import com.vexsoftware.nuvotifier.standalone.bootstrap.VotifierBootstrap;

public class Main {

    public static void main(String[] args) {
        VotifierBootstrap bootstrap = new VotifierBootstrap(args);
        bootstrap.init();

        while (true) {
            // Keep the thing running
        }
    }
}