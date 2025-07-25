package wiki.creeper.itemManager.api;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

public interface ItemManagerAPI {
    
    boolean isExpireItem(@NotNull ItemStack itemStack);
    
    @NotNull
    ItemStack setExpireTime(@NotNull ItemStack itemStack, @NotNull Timestamp expireTime);
    
    @Nullable
    Timestamp getExpireTime(@NotNull ItemStack itemStack);
    
    boolean isAttribution(@NotNull ItemStack itemStack);
    
    @NotNull
    ItemStack setAttribution(@NotNull ItemStack itemStack, @NotNull UUID playerUUID);
    
    @Nullable
    UUID getAttribution(@NotNull ItemStack itemStack);
    
    boolean isExpired(@NotNull ItemStack itemStack);
    
    @NotNull
    ItemStack removeExpireTime(@NotNull ItemStack itemStack);
    
    @NotNull
    ItemStack removeAttribution(@NotNull ItemStack itemStack);
    
    boolean canUse(@NotNull ItemStack itemStack, @NotNull UUID playerUUID);
    
    @NotNull
    ItemStack extendExpireTime(@NotNull ItemStack itemStack, long additionalMillis);
    
    boolean hasConflictingNBT(@NotNull ItemStack itemStack);
    
    @NotNull
    ItemStack copyWithAttribution(@NotNull ItemStack itemStack, @NotNull UUID newOwner);
    
    boolean isValidItemForAttribution(@NotNull ItemStack itemStack);
    
    long getRemainingTime(@NotNull ItemStack itemStack);
    
    @NotNull
    ItemStack refreshExpireLore(@NotNull ItemStack itemStack);
    
    boolean compareAttributions(@NotNull ItemStack item1, @NotNull ItemStack item2);
    
    @NotNull
    List<ItemStack> removeAllExpiredItems(@NotNull org.bukkit.inventory.Inventory inventory);
    
    int countAttributedItems(@NotNull org.bukkit.inventory.Inventory inventory, @NotNull UUID playerUUID);
}