package wiki.creeper.itemManager.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ItemAttributionDeniedEvent extends Event implements Cancellable {
    
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final ItemStack item;
    private final UUID itemOwner;
    private final DeniedAction action;
    private boolean cancelled;
    
    public enum DeniedAction {
        USE,
        PICKUP,
        INVENTORY_CLICK,
        INVENTORY_DRAG,
        DROP,
        CONSUME,
        PLACE_BLOCK,
        CONTAINER_EXTRACT
    }
    
    public ItemAttributionDeniedEvent(@NotNull Player player, @NotNull ItemStack item, @NotNull UUID itemOwner, @NotNull DeniedAction action) {
        this.player = player;
        this.item = item.clone();
        this.itemOwner = itemOwner;
        this.action = action;
        this.cancelled = false;
    }
    
    @NotNull
    public Player getPlayer() {
        return player;
    }
    
    @NotNull
    public ItemStack getItem() {
        return item.clone();
    }
    
    @NotNull
    public UUID getItemOwner() {
        return itemOwner;
    }
    
    @NotNull
    public DeniedAction getAction() {
        return action;
    }
    
    @Override
    public boolean isCancelled() {
        return cancelled;
    }
    
    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
    
    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
    
    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}