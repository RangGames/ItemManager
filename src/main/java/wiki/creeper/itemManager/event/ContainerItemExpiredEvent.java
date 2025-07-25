package wiki.creeper.itemManager.event;

import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Timestamp;
import java.util.List;

public class ContainerItemExpiredEvent extends Event {
    
    private static final HandlerList HANDLERS = new HandlerList();
    private final Container container;
    private final List<ItemStack> expiredItems;
    private final Player opener;
    
    public ContainerItemExpiredEvent(@NotNull Container container, @NotNull List<ItemStack> expiredItems, @Nullable Player opener) {
        this.container = container;
        this.expiredItems = expiredItems;
        this.opener = opener;
    }
    
    @NotNull
    public Container getContainer() {
        return container;
    }
    
    @NotNull
    public List<ItemStack> getExpiredItems() {
        return expiredItems;
    }
    
    @Nullable
    public Player getOpener() {
        return opener;
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