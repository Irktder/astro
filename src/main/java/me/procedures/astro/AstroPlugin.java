package me.procedures.astro;

import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.PaperCommandManager;
import lombok.Getter;
import me.joeleoli.frame.Frame;
import me.procedures.astro.arena.Arena;
import me.procedures.astro.commands.ArenaCommand;
import me.procedures.astro.listeners.*;
import me.procedures.astro.commands.DuelCommand;
import me.procedures.astro.commands.LadderCommand;
import me.procedures.astro.config.PracticeConfiguration;
import me.procedures.astro.data.Mongo;
import me.procedures.astro.ladder.Ladder;
import me.procedures.astro.managers.*;
import me.procedures.astro.scoreboard.AdapterResolver;
import me.procedures.astro.utils.CC;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

@Getter
public class AstroPlugin extends JavaPlugin {

    @Getter private static AstroPlugin instance;

    private PracticeConfiguration configuration;
    private Mongo mongo;

    private LadderManager ladderManager;
    private ProfileManager profileManager;
    private QueueManager queueManager;
    private ArenaManager arenaManager;
    private SpectatorManager spectatorManager;

    public void onEnable() {
        instance = this;

        this.configuration = new PracticeConfiguration();
        this.mongo = new Mongo();

        this.ladderManager = new LadderManager(this);
        this.profileManager = new ProfileManager(this);
        this.queueManager = new QueueManager(this);
        this.arenaManager = new ArenaManager(this);
        this.spectatorManager = new SpectatorManager(this);

        this.registerListeners(new PlayerListener(this), new ChatListener(this), new BlockListener(this), new EnviornmentListener(), new InventoryListener(this));
        this.registerCommands(new PaperCommandManager(this));

        new Frame(this, new AdapterResolver());
    }

    public void onDisable() {
        this.ladderManager.saveLadders();
        this.arenaManager.saveArenas();
      //this.profileManager.saveProfiles();
    }

    private void registerListeners(Listener... listeners) {
        Arrays.stream(listeners)
                .forEach(listener ->  this.getServer().getPluginManager().registerEvents(listener, this));
    }

    private void registerCommands(PaperCommandManager commandManager) {
        this.registerContexts(commandManager);
        this.registerDependencies(commandManager);

        Arrays.asList(new LadderCommand(), new ClearEntitiesCommand(), new DuelCommand(), new ArenaCommand())
                .forEach(commandManager::registerCommand);
    }

    private void registerContexts(PaperCommandManager commandManager) {
        commandManager.getCommandContexts().registerContext(Ladder.class, c -> {
            String arg = c.popFirstArg();

            if (!this.ladderManager.getLadders().containsKey(arg)) {
                c.getSender().sendMessage(CC.PRIMARY + "The specified ladder does not exist.");
                throw new InvalidCommandArgument(true);
            }

            return this.ladderManager.getLadders().get(arg);
        });

        commandManager.getCommandContexts().registerContext(Arena.class, c -> {
            String arg = c.popFirstArg();

            if (!this.arenaManager.getArenas().containsKey(arg)) {
                c.getSender().sendMessage(CC.PRIMARY + "The specified arena does not exist.");
                throw new InvalidCommandArgument(true);
            }

            return this.arenaManager.getArenas().get(arg);
        });
    }

    private void registerDependencies(PaperCommandManager commandManager) {
        commandManager.registerDependency(LadderManager.class, this.ladderManager);
        commandManager.registerDependency(ProfileManager.class, this.profileManager);
        commandManager.registerDependency(QueueManager.class, this.queueManager);
        commandManager.registerDependency(Mongo.class, this.mongo);
    }
}
