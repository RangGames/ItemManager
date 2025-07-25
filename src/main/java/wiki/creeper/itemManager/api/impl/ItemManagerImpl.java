package wiki.creeper.itemManager.api.impl;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wiki.creeper.itemManager.api.ItemManagerAPI;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

public class ItemManagerImpl implements ItemManagerAPI {
    
    private final Plugin plugin;
    private final NamespacedKey EXPIRE_TIME_KEY;
    private final NamespacedKey ATTRIBUTION_KEY;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    public ItemManagerImpl(@NotNull Plugin plugin) {
        this.plugin = plugin;
        this.EXPIRE_TIME_KEY = new NamespacedKey(plugin, "expire_time");
        this.ATTRIBUTION_KEY = new NamespacedKey(plugin, "attribution_uuid");
    }
    
    @Override
    public boolean isExpireItem(@NotNull ItemStack itemStack) {
        if (!itemStack.hasItemMeta()) return false;
        
        ItemMeta meta = itemStack.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.has(EXPIRE_TIME_KEY, PersistentDataType.LONG);
    }
    
    @Override
    @NotNull
    public ItemStack setExpireTime(@NotNull ItemStack itemStack, @NotNull Timestamp expireTime) {
        if (itemStack.getType() == org.bukkit.Material.AIR) {
            throw new wiki.creeper.itemManager.exception.ItemManagerException.InvalidItemException("Cannot set expire time on AIR");
        }
        
        if (expireTime.getTime() <= System.currentTimeMillis()) {
            throw new wiki.creeper.itemManager.exception.ItemManagerException.InvalidTimeException("Expire time must be in the future");
        }
        
        ItemStack result = itemStack.clone();
        ItemMeta meta = result.hasItemMeta() ? result.getItemMeta() : plugin.getServer().getItemFactory().getItemMeta(result.getType());
        if (meta == null) return result;
        
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(EXPIRE_TIME_KEY, PersistentDataType.LONG, expireTime.getTime());
        
        updateExpireLore(meta, expireTime);
        
        result.setItemMeta(meta);
        return result;
    }
    
    @Override
    @Nullable
    public Timestamp getExpireTime(@NotNull ItemStack itemStack) {
        if (!itemStack.hasItemMeta()) return null;
        
        ItemMeta meta = itemStack.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        
        Long expireTimeMillis = container.get(EXPIRE_TIME_KEY, PersistentDataType.LONG);
        return expireTimeMillis != null ? new Timestamp(expireTimeMillis) : null;
    }
    
    @Override
    public boolean isAttribution(@NotNull ItemStack itemStack) {
        if (!itemStack.hasItemMeta()) return false;
        
        ItemMeta meta = itemStack.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.has(ATTRIBUTION_KEY, PersistentDataType.STRING);
    }
    
    @Override
    @NotNull
    public ItemStack setAttribution(@NotNull ItemStack itemStack, @NotNull UUID playerUUID) {
        if (itemStack.getType() == org.bukkit.Material.AIR) {
            throw new wiki.creeper.itemManager.exception.ItemManagerException.InvalidItemException("Cannot set attribution on AIR");
        }
        
        ItemStack result = itemStack.clone();
        ItemMeta meta = result.hasItemMeta() ? result.getItemMeta() : plugin.getServer().getItemFactory().getItemMeta(result.getType());
        if (meta == null) return result;
        
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(ATTRIBUTION_KEY, PersistentDataType.STRING, playerUUID.toString());
        
        updateAttributionLore(meta, playerUUID);
        
        result.setItemMeta(meta);
        return result;
    }
    
    @Override
    @Nullable
    public UUID getAttribution(@NotNull ItemStack itemStack) {
        if (!itemStack.hasItemMeta()) return null;
        
        ItemMeta meta = itemStack.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        
        String uuidString = container.get(ATTRIBUTION_KEY, PersistentDataType.STRING);
        if (uuidString == null) return null;
        
        try {
            return UUID.fromString(uuidString);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    @Override
    public boolean isExpired(@NotNull ItemStack itemStack) {
        Timestamp expireTime = getExpireTime(itemStack);
        if (expireTime == null) return false;
        
        return System.currentTimeMillis() > expireTime.getTime();
    }
    
    @Override
    @NotNull
    public ItemStack removeExpireTime(@NotNull ItemStack itemStack) {
        if (!isExpireItem(itemStack)) return itemStack;
        
        ItemStack result = itemStack.clone();
        ItemMeta meta = result.getItemMeta();
        
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.remove(EXPIRE_TIME_KEY);
        
        removeExpireLore(meta);
        
        result.setItemMeta(meta);
        return result;
    }
    
    @Override
    @NotNull
    public ItemStack removeAttribution(@NotNull ItemStack itemStack) {
        if (!isAttribution(itemStack)) return itemStack;
        
        ItemStack result = itemStack.clone();
        ItemMeta meta = result.getItemMeta();
        
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.remove(ATTRIBUTION_KEY);
        
        removeAttributionLore(meta);
        
        result.setItemMeta(meta);
        return result;
    }
    
    @Override
    public boolean canUse(@NotNull ItemStack itemStack, @NotNull UUID playerUUID) {
        if (isExpired(itemStack)) return false;
        
        UUID attribution = getAttribution(itemStack);
        if (attribution == null) return true;
        
        return attribution.equals(playerUUID);
    }
    
    private void updateExpireLore(@NotNull ItemMeta meta, @NotNull Timestamp expireTime) {
        List<Component> lore = meta.hasLore() ? new ArrayList<>(meta.lore()) : new ArrayList<>();
        
        removeExpireLore(meta);
        
        Component expireComponent = Component.text()
                .append(Component.text("⏱ 만료시간: ", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false))
                .append(Component.text(dateFormat.format(expireTime), NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false))
                .build();
        
        lore.add(0, expireComponent);
        meta.lore(lore);
    }
    
    private void updateAttributionLore(@NotNull ItemMeta meta, @NotNull UUID playerUUID) {
        List<Component> lore = meta.hasLore() ? new ArrayList<>(meta.lore()) : new ArrayList<>();
        
        removeAttributionLore(meta);
        
        String playerName = plugin.getServer().getOfflinePlayer(playerUUID).getName();
        playerName = playerName != null ? playerName : playerUUID.toString();
        
        Component attributionComponent = Component.text()
                .append(Component.text("🔒 귀속: ", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false))
                .append(Component.text(playerName, NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false))
                .build();
        
        int insertIndex = hasExpireLore(lore) ? 1 : 0;
        lore.add(insertIndex, attributionComponent);
        meta.lore(lore);
    }
    
    private void removeExpireLore(@NotNull ItemMeta meta) {
        if (!meta.hasLore()) return;
        
        List<Component> lore = new ArrayList<>(meta.lore());
        lore.removeIf(component -> {
            String plainText = component.toString();
            return plainText.contains("⏱ 만료시간:");
        });
        
        meta.lore(lore.isEmpty() ? null : lore);
    }
    
    private void removeAttributionLore(@NotNull ItemMeta meta) {
        if (!meta.hasLore()) return;
        
        List<Component> lore = new ArrayList<>(meta.lore());
        lore.removeIf(component -> {
            String plainText = component.toString();
            return plainText.contains("🔒 귀속:");
        });
        
        meta.lore(lore.isEmpty() ? null : lore);
    }
    
    private boolean hasExpireLore(@NotNull List<Component> lore) {
        return lore.stream().anyMatch(component -> component.toString().contains("⏱ 만료시간:"));
    }
    
    @Override
    @NotNull
    public ItemStack extendExpireTime(@NotNull ItemStack itemStack, long additionalMillis) {
        if (!isExpireItem(itemStack)) {
            throw new wiki.creeper.itemManager.exception.ItemManagerException.InvalidItemException("Item does not have expire time");
        }
        
        Timestamp currentExpireTime = getExpireTime(itemStack);
        if (currentExpireTime == null) {
            throw new wiki.creeper.itemManager.exception.ItemManagerException.InvalidTimeException("Cannot get current expire time");
        }
        
        Timestamp newExpireTime = new Timestamp(currentExpireTime.getTime() + additionalMillis);
        return setExpireTime(itemStack, newExpireTime);
    }
    
    @Override
    public boolean hasConflictingNBT(@NotNull ItemStack itemStack) {
        if (!itemStack.hasItemMeta()) return false;
        
        ItemMeta meta = itemStack.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        
        Set<NamespacedKey> keys = container.getKeys();
        for (NamespacedKey key : keys) {
            if (!key.getNamespace().equals(plugin.getName().toLowerCase())) {
                if (key.getKey().contains("expire") || key.getKey().contains("attribution") || 
                    key.getKey().contains("owner") || key.getKey().contains("bound")) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    @Override
    @NotNull
    public ItemStack copyWithAttribution(@NotNull ItemStack itemStack, @NotNull UUID newOwner) {
        ItemStack copy = itemStack.clone();
        
        if (isAttribution(copy)) {
            copy = removeAttribution(copy);
        }
        
        return setAttribution(copy, newOwner);
    }
    
    @Override
    public boolean isValidItemForAttribution(@NotNull ItemStack itemStack) {
        if (itemStack.getType() == org.bukkit.Material.AIR) return false;
        
        if (itemStack.getType().isEdible()) return true;
        if (itemStack.getType().name().contains("SWORD") || itemStack.getType().name().contains("AXE")) return true;
        if (itemStack.getType().name().contains("PICKAXE") || itemStack.getType().name().contains("SHOVEL")) return true;
        if (itemStack.getType().name().contains("HELMET") || itemStack.getType().name().contains("CHESTPLATE")) return true;
        if (itemStack.getType().name().contains("LEGGINGS") || itemStack.getType().name().contains("BOOTS")) return true;
        if (itemStack.getType().name().contains("BOW") || itemStack.getType().name().contains("CROSSBOW")) return true;
        if (itemStack.getType().name().contains("TRIDENT") || itemStack.getType().name().contains("SHIELD")) return true;
        
        return !itemStack.getType().isBlock();
    }
    
    @Override
    public long getRemainingTime(@NotNull ItemStack itemStack) {
        if (!isExpireItem(itemStack)) return -1;
        
        Timestamp expireTime = getExpireTime(itemStack);
        if (expireTime == null) return -1;
        
        long remaining = expireTime.getTime() - System.currentTimeMillis();
        return Math.max(0, remaining);
    }
    
    @Override
    @NotNull
    public ItemStack refreshExpireLore(@NotNull ItemStack itemStack) {
        if (!isExpireItem(itemStack)) return itemStack;
        
        Timestamp expireTime = getExpireTime(itemStack);
        if (expireTime == null) return itemStack;
        
        ItemStack result = itemStack.clone();
        ItemMeta meta = result.getItemMeta();
        
        updateExpireLore(meta, expireTime);
        result.setItemMeta(meta);
        
        return result;
    }
    
    @Override
    public boolean compareAttributions(@NotNull ItemStack item1, @NotNull ItemStack item2) {
        UUID attr1 = getAttribution(item1);
        UUID attr2 = getAttribution(item2);
        
        if (attr1 == null && attr2 == null) return true;
        if (attr1 == null || attr2 == null) return false;
        
        return attr1.equals(attr2);
    }
    
    @Override
    @NotNull
    public List<ItemStack> removeAllExpiredItems(@NotNull org.bukkit.inventory.Inventory inventory) {
        List<ItemStack> removedItems = new ArrayList<>();
        
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && isExpired(item)) {
                removedItems.add(item.clone());
                inventory.setItem(i, null);
            }
        }
        
        return removedItems;
    }
    
    @Override
    public int countAttributedItems(@NotNull org.bukkit.inventory.Inventory inventory, @NotNull UUID playerUUID) {
        int count = 0;
        
        for (ItemStack item : inventory.getContents()) {
            if (item != null && isAttribution(item)) {
                UUID attribution = getAttribution(item);
                if (attribution != null && attribution.equals(playerUUID)) {
                    count += item.getAmount();
                }
            }
        }
        
        return count;
    }
}