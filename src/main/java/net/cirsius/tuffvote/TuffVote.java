package net.cirsius.tuffvote;

import org.yaml.snakeyaml.Yaml;
import java.io.*;
import java.nio.file.Files;
import java.util.*;

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

    @SuppressWarnings("unchecked")
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

            Yaml yaml = new Yaml();
            try (FileInputStream fis = new FileInputStream(configFile)) {
                Map<String, Object> data = yaml.load(fis);
                commands = data.get("commands") instanceof List ? (List<String>) data.get("commands") : new ArrayList<>();
                services = data.get("services") instanceof List ? (List<String>) data.get("services") : new ArrayList<>();
                broadcastMessage = (String) data.getOrDefault("broadcast_message", "");
            }
        } catch (IOException e) {
            platform.log("couldnt load config " + e.getMessage());
        }
    }

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
        void log(String msg);
        void broadcast(String msg);
        void runCommand(String cmd);
    }
}
