package net.cirsius.tuffvote;

import com.vexsoftware.votifier.bungee.events.VotifierEvent;
import com.vexsoftware.votifier.model.Vote;
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

    @Override
    public void onEnable() {
        commands = new ArrayList<>();
        getProxy().getPluginManager().registerListener(this, this);
        getProxy().getPluginManager().registerCommand(this, new ReloadCommand());
        loadConfig();
    }

    @EventHandler
    public void onVote(VotifierEvent event) {
        Vote vote = event.getVote();

        if (!vote.getServiceName().equalsIgnoreCase("eaglerserverlist")) {
            return;
        }

        String username = vote.getUsername();

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