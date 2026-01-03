package net.cirsius.tuffvote;

import com.vexsoftware.votifier.bungee.events.VotifierEvent;
import com.vexsoftware.votifier.model.Vote;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class TuffVote extends Plugin implements Listener {

    private Configuration config;
    private List<String> commands;
    private List<String> services;
    private String broadcastMessage;

    @Override
    public void onEnable() {
        commands = new ArrayList<>();
        services = new ArrayList<>();
        getProxy().getPluginManager().registerListener(this, this);
        getProxy().getPluginManager().registerCommand(this, new ReloadCommand());
        loadConfig();
    }

    @EventHandler
    public void onVote(VotifierEvent event) {
        Vote vote = event.getVote();

        if (!services.isEmpty() && services.stream().noneMatch(s -> s.equalsIgnoreCase(vote.getServiceName()))) {
            return;
        }

        String username = vote.getUsername();
        String serviceName = vote.getServiceName();
        if (broadcastMessage != null && !broadcastMessage.isEmpty()) {
            String message = broadcastMessage
                    .replace("%username%", username)
                    .replace("%service%", serviceName);
            message = ChatColor.translateAlternateColorCodes('&', message);
            TextComponent component = new TextComponent(message);
            
            for (ProxiedPlayer player : getProxy().getPlayers()) {
                player.sendMessage(component);
            }
            getProxy().getConsole().sendMessage(component);
        }

        for (String command : commands) {
            String processedCommand = command.replace("%username%", username);
            getProxy().getPluginManager().dispatchCommand(getProxy().getConsole(), processedCommand);
        }
    }

    public void loadConfig() {
        try {
            if (!getDataFolder().exists()) {
                getDataFolder().mkdir();
            }

            File configFile = new File(getDataFolder(), "config.yml");

            if (!configFile.exists()) {
                try (InputStream in = getResourceAsStream("config.yml")) {
                    Files.copy(in, configFile.toPath());
                }
            }

            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
            commands = config.getStringList("commands");
            services = config.getStringList("services");
            broadcastMessage = config.getString("broadcast_message", "");

        } catch (IOException e) {
            getLogger().severe("couldnt load config " + e.getMessage());
        }
    }

    private class ReloadCommand extends Command {

        public ReloadCommand() {
            super("tuffvotereload");
        }

        @Override
        public void execute(CommandSender sender, String[] args) {
            loadConfig();
            sender.sendMessage("config reloaded");
        }
    }
}