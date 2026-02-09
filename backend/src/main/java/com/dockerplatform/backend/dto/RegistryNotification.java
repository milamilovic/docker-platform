package com.dockerplatform.backend.dto;

import java.util.List;

public class RegistryNotification {
    public List<Event> events;

    public static class Event {
        public String action;
        public Target target;
    }

    public static class Target {
        public String repository;
        public String digest;
        public String tag;
        public Long length;
    }
}
