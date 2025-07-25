package wiki.creeper.itemManager.task;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import wiki.creeper.itemManager.api.ItemManagerAPI;
import wiki.creeper.itemManager.event.ItemExpiredEvent;
import wiki.creeper.itemManager.util.ItemUtil;

public class ExpiredItemCheckTask extends BukkitRunnable {
    
    private final Plugin plugin;
    private final ItemManagerAPI api;
    private final long checkInterval;
    
    public ExpiredItemCheckTask(@NotNull Plugin plugin, @NotNull ItemManagerAPI api, long checkInterval) {
        this.plugin = plugin;
        this.api = api;
        this.checkInterval = checkInterval;
    }
    
    public void start() {
        this.runTaskTimer(plugin, 0L, checkInterval);
    }
    
    @Override
    public void run() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            for (int i = 0; i < player.getInventory().getSize(); i++) {
                ItemStack item = player.getInventory().getItem(i);
                if (item != null && api.isExpired(item)) {
                    ItemExpiredEvent expiredEvent = new ItemExpiredEvent(player, item, api.getExpireTime(item), ItemExpiredEvent.ExpireAction.PERIODIC_CHECK);
                    plugin.getServer().getPluginManager().callEvent(expiredEvent);
                    
                    if (!expiredEvent.isCancelled()) {
                        player.getInventory().setItem(i, null);
                    }
                }
            }
        }
    }
}