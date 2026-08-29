package net.cirsius.tuffvote.bungee;

import com.vexsoftware.votifier.bungee.events.VotifierEvent;
import com.vexsoftware.votifier.model.Vote;
import net.cirsius.tuffvote.TuffVote;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;

import java.io.File;
import java.io.InputStream;

public class TuffVoteBungee extends Plugin implements Listener, TuffVote.Platform {

    private TuffVote tuffVote;

    @Override
    public void onEnable() {
        tuffVote = new TuffVote(this);
        getProxy().getPluginManager().registerListener(this, this);
        getProxy().getPluginManager().registerCommand(this, new ReloadCommand());
        tuffVote.enable();
    }

    @EventHandler
    public void onVote(VotifierEvent event) {
        Vote vote = event.getVote();
        tuffVote.handleVote(vote.getUsername(), vote.getServiceName());
    }

    @Override
    public File dataFolder() {
        return getDataFolder();
    }

    @Override
    public InputStream resource(String name) {
        return getResourceAsStream(name);
    }

    @Override
    public TuffVote.Config loadConfig(File configFile) throws Exception {
        Configuration config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
        return new TuffVote.Config(
                config.getStringList("commands"),
                config.getStringList("services"),
                config.getString("broadcast_message", "")
        );
    }

    @Override
    public void log(String msg) {
        getLogger().severe(msg);
    }

    @Override
    public void broadcast(String msg) {
        String message = ChatColor.translateAlternateColorCodes('&', msg);
        TextComponent component = new TextComponent(message);

        for (ProxiedPlayer player : getProxy().getPlayers()) {
            player.sendMessage(component);
        }
        getProxy().getConsole().sendMessage(component);
    }

    @Override
    public void runCommand(String cmd) {
        getProxy().getPluginManager().dispatchCommand(getProxy().getConsole(), cmd);
    }

    private class ReloadCommand extends Command {

        public ReloadCommand() {
            super("tuffvotereload");
        }

        @Override
        public void execute(CommandSender sender, String[] args) {
            tuffVote.loadConfig();
            sender.sendMessage("config reloaded");
        }
    }
}
