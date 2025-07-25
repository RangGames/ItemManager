package wiki.creeper.itemManager.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Timestamp;

public class ItemExpiredEvent extends Event implements Cancellable {
    
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final ItemStack expiredItem;
    private final Timestamp expireTime;
    private final ExpireAction action;
    private boolean cancelled;
    
    public enum ExpireAction {
        USE,
        PICKUP,
        INVENTORY_CLICK,
        INVENTORY_DRAG,
        DROP,
        CONSUME,
        PLACE_BLOCK,
        CONTAINER_ACCESS,
        PERIODIC_CHECK
    }
    
    public ItemExpiredEvent(@Nullable Player player, @NotNull ItemStack expiredItem, @NotNull Timestamp expireTime, @NotNull ExpireAction action) {
        this.player = player;
        this.expiredItem = expiredItem.clone();
        this.expireTime = expireTime;
        this.action = action;
        this.cancelled = false;
    }
    
    @Nullable
    public Player getPlayer() {
        return player;
    }
    
    @NotNull
    public ItemStack getExpiredItem() {
        return expiredItem.clone();
    }
    
    @NotNull
    public Timestamp getExpireTime() {
        return expireTime;
    }
    
    @NotNull
    public ExpireAction getAction() {
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