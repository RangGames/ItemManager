package wiki.creeper.itemManager.listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import wiki.creeper.itemManager.api.ItemManagerAPI;
import wiki.creeper.itemManager.event.ContainerItemExpiredEvent;
import wiki.creeper.itemManager.event.ItemAttributionDeniedEvent;
import wiki.creeper.itemManager.event.ItemExpiredEvent;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ItemEventListener implements Listener {
    
    private final Plugin plugin;
    private final ItemManagerAPI itemManagerAPI;
    
    public ItemEventListener(@NotNull Plugin plugin, @NotNull ItemManagerAPI itemManagerAPI) {
        this.plugin = plugin;
        this.itemManagerAPI = itemManagerAPI;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerPickupItem(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        
        ItemStack item = event.getItem().getItemStack();
        
        if (itemManagerAPI.isExpired(item)) {
            ItemExpiredEvent expiredEvent = new ItemExpiredEvent(player, item, itemManagerAPI.getExpireTime(item), ItemExpiredEvent.ExpireAction.PICKUP);
            plugin.getServer().getPluginManager().callEvent(expiredEvent);
            
            if (!expiredEvent.isCancelled()) {
                event.setCancelled(true);
                event.getItem().remove();
            }
            return;
        }
        
        if (itemManagerAPI.isAttribution(item)) {
            UUID attribution = itemManagerAPI.getAttribution(item);
            if (attribution != null && !attribution.equals(player.getUniqueId())) {
                ItemAttributionDeniedEvent deniedEvent = new ItemAttributionDeniedEvent(player, item, attribution, ItemAttributionDeniedEvent.DeniedAction.PICKUP);
                plugin.getServer().getPluginManager().callEvent(deniedEvent);
                
                if (!deniedEvent.isCancelled()) {
                    event.setCancelled(true);
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null || item.getType() == Material.AIR) return;
        
        Player player = event.getPlayer();
        
        if (!itemManagerAPI.canUse(item, player.getUniqueId())) {
            if (itemManagerAPI.isExpired(item)) {
                ItemExpiredEvent expiredEvent = new ItemExpiredEvent(player, item, itemManagerAPI.getExpireTime(item), ItemExpiredEvent.ExpireAction.USE);
                plugin.getServer().getPluginManager().callEvent(expiredEvent);
                
                if (!expiredEvent.isCancelled()) {
                    event.setCancelled(true);
                    removeExpiredItem(player, item);
                }
            } else {
                UUID attribution = itemManagerAPI.getAttribution(item);
                if (attribution != null) {
                    ItemAttributionDeniedEvent deniedEvent = new ItemAttributionDeniedEvent(player, item, attribution, ItemAttributionDeniedEvent.DeniedAction.USE);
                    plugin.getServer().getPluginManager().callEvent(deniedEvent);
                    
                    if (!deniedEvent.isCancelled()) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        
        ItemStack currentItem = event.getCurrentItem();
        ItemStack cursor = event.getCursor();
        
        Inventory clickedInventory = event.getClickedInventory();
        
        if (isContainerInventory(clickedInventory) && currentItem != null && currentItem.getType() != Material.AIR) {
            if (itemManagerAPI.isAttribution(currentItem)) {
                UUID attribution = itemManagerAPI.getAttribution(currentItem);
                if (attribution != null && !attribution.equals(player.getUniqueId())) {
                    ItemAttributionDeniedEvent deniedEvent = new ItemAttributionDeniedEvent(player, currentItem, attribution, ItemAttributionDeniedEvent.DeniedAction.CONTAINER_EXTRACT);
                    plugin.getServer().getPluginManager().callEvent(deniedEvent);
                    
                    if (!deniedEvent.isCancelled()) {
                        event.setCancelled(true);
                    }
                    return;
                }
            }
        }
        
        handleInventoryItem(event, player, currentItem);
        handleInventoryItem(event, player, cursor);
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        
        ItemStack item = event.getOldCursor();
        if (item == null || item.getType() == Material.AIR) return;
        
        if (!itemManagerAPI.canUse(item, player.getUniqueId())) {
            if (itemManagerAPI.isExpired(item)) {
                ItemExpiredEvent expiredEvent = new ItemExpiredEvent(player, item, itemManagerAPI.getExpireTime(item), ItemExpiredEvent.ExpireAction.INVENTORY_DRAG);
                plugin.getServer().getPluginManager().callEvent(expiredEvent);
                
                if (!expiredEvent.isCancelled()) {
                    event.setCancelled(true);
                }
            } else {
                UUID attribution = itemManagerAPI.getAttribution(item);
                if (attribution != null) {
                    ItemAttributionDeniedEvent deniedEvent = new ItemAttributionDeniedEvent(player, item, attribution, ItemAttributionDeniedEvent.DeniedAction.INVENTORY_DRAG);
                    plugin.getServer().getPluginManager().callEvent(deniedEvent);
                    
                    if (!deniedEvent.isCancelled()) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItemDrop().getItemStack();
        
        if (itemManagerAPI.isExpired(item)) {
            ItemExpiredEvent expiredEvent = new ItemExpiredEvent(player, item, itemManagerAPI.getExpireTime(item), ItemExpiredEvent.ExpireAction.DROP);
            plugin.getServer().getPluginManager().callEvent(expiredEvent);
            
            if (!expiredEvent.isCancelled()) {
                event.getItemDrop().remove();
            }
            return;
        }
        
        if (itemManagerAPI.isAttribution(item)) {
            UUID attribution = itemManagerAPI.getAttribution(item);
            if (attribution != null && !attribution.equals(player.getUniqueId())) {
                ItemAttributionDeniedEvent deniedEvent = new ItemAttributionDeniedEvent(player, item, attribution, ItemAttributionDeniedEvent.DeniedAction.DROP);
                plugin.getServer().getPluginManager().callEvent(deniedEvent);
                
                if (!deniedEvent.isCancelled()) {
                    event.setCancelled(true);
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItemInHand();
        
        if (!itemManagerAPI.canUse(item, player.getUniqueId())) {
            if (itemManagerAPI.isExpired(item)) {
                ItemExpiredEvent expiredEvent = new ItemExpiredEvent(player, item, itemManagerAPI.getExpireTime(item), ItemExpiredEvent.ExpireAction.PLACE_BLOCK);
                plugin.getServer().getPluginManager().callEvent(expiredEvent);
                
                if (!expiredEvent.isCancelled()) {
                    event.setCancelled(true);
                    removeExpiredItem(player, item);
                }
            } else {
                UUID attribution = itemManagerAPI.getAttribution(item);
                if (attribution != null) {
                    ItemAttributionDeniedEvent deniedEvent = new ItemAttributionDeniedEvent(player, item, attribution, ItemAttributionDeniedEvent.DeniedAction.PLACE_BLOCK);
                    plugin.getServer().getPluginManager().callEvent(deniedEvent);
                    
                    if (!deniedEvent.isCancelled()) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        if (!itemManagerAPI.canUse(item, player.getUniqueId())) {
            if (itemManagerAPI.isExpired(item)) {
                ItemExpiredEvent expiredEvent = new ItemExpiredEvent(player, item, itemManagerAPI.getExpireTime(item), ItemExpiredEvent.ExpireAction.CONSUME);
                plugin.getServer().getPluginManager().callEvent(expiredEvent);
                
                if (!expiredEvent.isCancelled()) {
                    event.setCancelled(true);
                    removeExpiredItem(player, item);
                }
            } else {
                UUID attribution = itemManagerAPI.getAttribution(item);
                if (attribution != null) {
                    ItemAttributionDeniedEvent deniedEvent = new ItemAttributionDeniedEvent(player, item, attribution, ItemAttributionDeniedEvent.DeniedAction.CONSUME);
                    plugin.getServer().getPluginManager().callEvent(deniedEvent);
                    
                    if (!deniedEvent.isCancelled()) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        new BukkitRunnable() {
            @Override
            public void run() {
                checkAndRemoveExpiredItems(player);
            }
        }.runTaskLater(plugin, 20L);
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        
        new BukkitRunnable() {
            @Override
            public void run() {
                checkAndRemoveExpiredItems(player);
                
                Inventory openedInventory = event.getInventory();
                if (isContainerInventory(openedInventory)) {
                    checkContainerExpiredItems(openedInventory, player);
                }
            }
        }.runTaskLater(plugin, 1L);
    }
    
    private void handleInventoryItem(InventoryClickEvent event, Player player, ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return;
        
        if (!itemManagerAPI.canUse(item, player.getUniqueId())) {
            if (itemManagerAPI.isExpired(item)) {
                ItemExpiredEvent expiredEvent = new ItemExpiredEvent(player, item, itemManagerAPI.getExpireTime(item), ItemExpiredEvent.ExpireAction.INVENTORY_CLICK);
                plugin.getServer().getPluginManager().callEvent(expiredEvent);
                
                if (!expiredEvent.isCancelled()) {
                    event.setCancelled(true);
                    
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (event.getCurrentItem() != null && event.getCurrentItem().equals(item)) {
                                event.setCurrentItem(null);
                            }
                            if (event.getCursor() != null && event.getCursor().equals(item)) {
                                event.getWhoClicked().setItemOnCursor(null);
                            }
                        }
                    }.runTask(plugin);
                }
            } else {
                UUID attribution = itemManagerAPI.getAttribution(item);
                if (attribution != null) {
                    ItemAttributionDeniedEvent deniedEvent = new ItemAttributionDeniedEvent(player, item, attribution, ItemAttributionDeniedEvent.DeniedAction.INVENTORY_CLICK);
                    plugin.getServer().getPluginManager().callEvent(deniedEvent);
                    
                    if (!deniedEvent.isCancelled()) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }
    
    private void checkAndRemoveExpiredItems(Player player) {
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item != null && itemManagerAPI.isExpired(item)) {
                ItemExpiredEvent expiredEvent = new ItemExpiredEvent(player, item, itemManagerAPI.getExpireTime(item), ItemExpiredEvent.ExpireAction.PERIODIC_CHECK);
                plugin.getServer().getPluginManager().callEvent(expiredEvent);
                
                if (!expiredEvent.isCancelled()) {
                    player.getInventory().setItem(i, null);
                }
            }
        }
    }
    
    private void removeExpiredItem(Player player, ItemStack item) {
        new BukkitRunnable() {
            @Override
            public void run() {
                player.getInventory().remove(item);
            }
        }.runTask(plugin);
    }
    
    private boolean isContainerInventory(Inventory inventory) {
        if (inventory == null) return false;
        
        InventoryType type = inventory.getType();
        return type == InventoryType.CHEST || 
               type == InventoryType.DISPENSER || 
               type == InventoryType.DROPPER || 
               type == InventoryType.FURNACE || 
               type == InventoryType.BREWING || 
               type == InventoryType.HOPPER || 
               type == InventoryType.SHULKER_BOX ||
               type == InventoryType.BARREL ||
               type == InventoryType.BLAST_FURNACE ||
               type == InventoryType.SMOKER;
    }
    
    private void checkContainerExpiredItems(Inventory inventory, Player opener) {
        List<ItemStack> expiredItems = new ArrayList<>();
        
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && itemManagerAPI.isExpired(item)) {
                expiredItems.add(item.clone());
                inventory.setItem(i, null);
            }
        }
        
        if (!expiredItems.isEmpty() && inventory.getHolder() instanceof Container container) {
            ContainerItemExpiredEvent event = new ContainerItemExpiredEvent(container, expiredItems, opener);
            plugin.getServer().getPluginManager().callEvent(event);
        }
    }
}