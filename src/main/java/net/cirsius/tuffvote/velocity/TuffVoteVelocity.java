package net.cirsius.tuffvote.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.velocity.event.VotifierEvent;
import net.cirsius.tuffvote.TuffVote;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;

@Plugin(id = "tuffvote", name = "TuffVote", version = "${version}",
        authors = {"cirsius"}, dependencies = {@Dependency(id = "nuvotifier", optional = true)})
public class TuffVoteVelocity implements TuffVote.Platform {

    private final ProxyServer server;
    private final Path dataDirectory;
    private final org.slf4j.Logger logger;
    private TuffVote tuffVote;

    @Inject
    public TuffVoteVelocity(ProxyServer server, org.slf4j.Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        tuffVote = new TuffVote(this);
        server.getEventManager().register(this, this);
        server.getCommandManager().register("tuffvotereload", new ReloadCommand());
        tuffVote.enable();
    }

    @Subscribe
    public void onVote(VotifierEvent event) {
        Vote vote = event.getVote();
        tuffVote.handleVote(vote.getUsername(), vote.getServiceName());
    }

    @Override
    public File dataFolder() {
        return dataDirectory.toFile();
    }

    @Override
    public InputStream resource(String name) {
        return getClass().getClassLoader().getResourceAsStream(name);
    }

    @Override
    public TuffVote.Config loadConfig(File configFile) throws Exception {
        YamlConfigurationLoader loader = YamlConfigurationLoader.builder().path(configFile.toPath()).build();
        ConfigurationNode config = loader.load();
        return new TuffVote.Config(
                config.node("commands").getList(String.class),
                config.node("services").getList(String.class),
                config.node("broadcast_message").getString("")
        );
    }

    @Override
    public void log(String msg) {
        logger.error(msg);
    }

    @Override
    public void broadcast(String msg) {
        Component message = LegacyComponentSerializer.legacyAmpersand().deserialize(msg);

        for (Player player : server.getAllPlayers()) {
            player.sendMessage(message);
        }
        server.getConsoleCommandSource().sendMessage(message);
    }

    @Override
    public void runCommand(String cmd) {
        server.getCommandManager().executeAsync(server.getConsoleCommandSource(), cmd);
    }

    private class ReloadCommand implements SimpleCommand {

        @Override
        public void execute(Invocation invocation) {
            tuffVote.loadConfig();
            invocation.source().sendMessage(Component.text("config reloaded"));
        }
    }
}
