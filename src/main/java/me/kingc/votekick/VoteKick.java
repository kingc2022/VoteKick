package me.kingc.votekick;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class VoteKick extends JavaPlugin {
    private final Logger logger = getLogger();

    @Override
    public void onEnable() {
        // Plugin startup logic
        logger.info("[VoteKick] Plugin enabled");
        saveDefaultConfig();

        getCommand("vote").setExecutor(new me.kingc.votekick.commands.vote());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        logger.info("[VoteKick] Plugin disabled");
    }
}
