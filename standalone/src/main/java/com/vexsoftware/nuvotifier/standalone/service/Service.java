package com.vexsoftware.nuvotifier.standalone.service;

public interface Service {

    void start();

    default void stop() {}
}
