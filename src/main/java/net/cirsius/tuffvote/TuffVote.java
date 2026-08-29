package net.cirsius.tuffvote;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class TuffVote {

    private final Platform platform;
    private List<String> commands;
    private List<String> services;
    private String broadcastMessage;

    public TuffVote(Platform platform) {
        this.platform = platform;
        commands = new ArrayList<>();
        services = new ArrayList<>();
    }

    public void enable() {
        loadConfig();
    }

    public void loadConfig() {
        try {
            File folder = platform.dataFolder();
            if (!folder.exists()) {
                folder.mkdir();
            }

            File configFile = new File(folder, "config.yml");

            if (!configFile.exists()) {
                try (InputStream in = platform.resource("config.yml")) {
                    Files.copy(in, configFile.toPath());
                }
            }

            Config config = platform.loadConfig(configFile);
            commands = config.commands();
            services = config.services();
            broadcastMessage = config.broadcastMessage();
        } catch (Exception e) {
            platform.log("couldnt load config " + e.getMessage());
        }
    }

    public record Config(List<String> commands, List<String> services, String broadcastMessage) { }

    public void handleVote(String username, String serviceName) {
        if (!services.isEmpty() && services.stream().noneMatch(s -> s.equalsIgnoreCase(serviceName))) {
            return;
        }

        if (broadcastMessage != null && !broadcastMessage.isEmpty()) {
            String message = broadcastMessage
                    .replace("%username%", username)
                    .replace("%service%", serviceName);
            platform.broadcast(message);
        }

        for (String command : commands) {
            String processedCommand = command.replace("%username%", username);
            platform.runCommand(processedCommand);
        }
    }

    public interface Platform {
        File dataFolder();
        InputStream resource(String name);
        Config loadConfig(File configFile) throws Exception;
        void log(String msg);
        void broadcast(String msg);
        void runCommand(String cmd);
    }
}
