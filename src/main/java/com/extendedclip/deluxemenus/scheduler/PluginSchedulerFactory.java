package com.extendedclip.deluxemenus.scheduler;

import org.bukkit.plugin.Plugin;

/**
 * Creates the appropriate scheduler implementation based on the server type.
 * Uses Folia schedulers when running on Folia/Paper, otherwise Bukkit scheduler.
 */
public final class PluginSchedulerFactory {

    private static final String FOLIA_SCHEDULER_CLASS = "io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler";

    private PluginSchedulerFactory() {
    }

    public static PluginScheduler create(Plugin plugin) {
        if (isFolia()) {
            return new FoliaPluginScheduler(plugin);
        }
        return new BukkitPluginScheduler();
    }

    private static boolean isFolia() {
        try {
            Class.forName(FOLIA_SCHEDULER_CLASS);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
