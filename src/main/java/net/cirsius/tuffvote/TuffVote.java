package net.cirsius.tuffvote;

import com.vexsoftware.votifier.bungee.events.VotifierEvent;
import com.vexsoftware.votifier.model.Vote;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.event.EventHandler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

public class TuffVote extends Plugin implements Listener {

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

            File configFile = new File(getDataFolder(), "config.txt");

            if (!configFile.exists()) {
                List<String> defaultContent = Arrays.asList(
                        "# use /commands.",
                        "# %username% is the placeholder for, usernames."
                );
                Files.write(configFile.toPath(), defaultContent);
            }

            commands = Files.readAllLines(configFile.toPath());
            commands.removeIf(line -> line.trim().isEmpty() || line.trim().startsWith("#"));

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