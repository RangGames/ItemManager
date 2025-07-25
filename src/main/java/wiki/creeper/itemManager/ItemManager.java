package wiki.creeper.itemManager;

import org.bukkit.plugin.java.JavaPlugin;
import wiki.creeper.itemManager.api.ItemManagerAPI;
import wiki.creeper.itemManager.api.impl.ItemManagerImpl;
import wiki.creeper.itemManager.listener.ItemEventListener;
import wiki.creeper.itemManager.task.ExpiredItemCheckTask;

public final class ItemManager extends JavaPlugin {
    
    private static ItemManager instance;
    private ItemManagerAPI itemManagerAPI;
    private ExpiredItemCheckTask expiredItemCheckTask;

    @Override
    public void onEnable() {
        instance = this;
        
        itemManagerAPI = new ItemManagerImpl(this);
        
        getServer().getPluginManager().registerEvents(new ItemEventListener(this, itemManagerAPI), this);
        
        expiredItemCheckTask = new ExpiredItemCheckTask(this, itemManagerAPI, 20L * 60);
        expiredItemCheckTask.start();
        
        getLogger().info("ItemManager has been enabled!");
    }

    @Override
    public void onDisable() {
        if (expiredItemCheckTask != null) {
            expiredItemCheckTask.cancel();
        }
        
        getLogger().info("ItemManager has been disabled!");
    }
    
    public static ItemManager getInstance() {
        return instance;
    }
    
    public ItemManagerAPI getItemManagerAPI() {
        return itemManagerAPI;
    }
}
