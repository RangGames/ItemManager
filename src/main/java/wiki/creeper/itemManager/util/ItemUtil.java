package wiki.creeper.itemManager.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wiki.creeper.itemManager.api.ItemManagerAPI;

import java.sql.Timestamp;
import java.util.*;

public class ItemUtil {
    
    private ItemUtil() {
    }
    
    public static int removeExpiredItems(@NotNull Player player, @NotNull ItemManagerAPI api) {
        PlayerInventory inventory = player.getInventory();
        int removedCount = 0;
        
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && api.isExpired(item)) {
                inventory.setItem(i, null);
                removedCount++;
            }
        }
        
        return removedCount;
    }
    
    @NotNull
    public static List<ItemStack> getAttributedItems(@NotNull Player player, @NotNull ItemManagerAPI api) {
        List<ItemStack> attributedItems = new ArrayList<>();
        PlayerInventory inventory = player.getInventory();
        
        for (ItemStack item : inventory.getContents()) {
            if (item != null && api.isAttribution(item)) {
                UUID attribution = api.getAttribution(item);
                if (attribution != null && attribution.equals(player.getUniqueId())) {
                    attributedItems.add(item);
                }
            }
        }
        
        return attributedItems;
    }
    
    @NotNull
    public static List<ItemStack> getExpirableItems(@NotNull Player player, @NotNull ItemManagerAPI api) {
        List<ItemStack> expirableItems = new ArrayList<>();
        PlayerInventory inventory = player.getInventory();
        
        for (ItemStack item : inventory.getContents()) {
            if (item != null && api.isExpireItem(item) && !api.isExpired(item)) {
                expirableItems.add(item);
            }
        }
        
        return expirableItems;
    }
    
    @NotNull
    public static Map<ItemStack, Timestamp> getItemsExpiringSoon(@NotNull Player player, @NotNull ItemManagerAPI api, long withinMillis) {
        Map<ItemStack, Timestamp> expiringSoon = new HashMap<>();
        PlayerInventory inventory = player.getInventory();
        long currentTime = System.currentTimeMillis();
        
        for (ItemStack item : inventory.getContents()) {
            if (item != null && api.isExpireItem(item)) {
                Timestamp expireTime = api.getExpireTime(item);
                if (expireTime != null && !api.isExpired(item)) {
                    long timeUntilExpire = expireTime.getTime() - currentTime;
                    if (timeUntilExpire <= withinMillis) {
                        expiringSoon.put(item, expireTime);
                    }
                }
            }
        }
        
        return expiringSoon;
    }
    
    public static void sendItemInfo(@NotNull Player player, @NotNull ItemStack item, @NotNull ItemManagerAPI api) {
        if (item == null || item.getType() == Material.AIR) {
            player.sendMessage(Component.text("손에 아이템을 들고 있지 않습니다.", NamedTextColor.RED));
            return;
        }
        
        List<Component> info = new ArrayList<>();
        info.add(Component.text("=== 아이템 정보 ===", NamedTextColor.GOLD, TextDecoration.BOLD));
        
        Component itemName = item.displayName();
        if (itemName == null) {
            itemName = Component.translatable(item.getType().translationKey());
        }
        info.add(Component.text("아이템: ", NamedTextColor.GRAY)
                .append(itemName));
        
        info.add(Component.text("수량: ", NamedTextColor.GRAY)
                .append(Component.text(item.getAmount(), NamedTextColor.WHITE)));
        
        if (api.isExpireItem(item)) {
            Timestamp expireTime = api.getExpireTime(item);
            if (expireTime != null) {
                String status = api.isExpired(item) ? "만료됨" : "활성";
                NamedTextColor statusColor = api.isExpired(item) ? NamedTextColor.RED : NamedTextColor.GREEN;
                
                info.add(Component.text("만료시간: ", NamedTextColor.GRAY)
                        .append(Component.text(TimeUtil.formatTimestamp(expireTime), NamedTextColor.YELLOW)));
                
                if (!api.isExpired(item)) {
                    info.add(Component.text("남은시간: ", NamedTextColor.GRAY)
                            .append(Component.text(TimeUtil.formatRemainingTime(expireTime), NamedTextColor.AQUA)));
                }
                
                info.add(Component.text("상태: ", NamedTextColor.GRAY)
                        .append(Component.text(status, statusColor)));
            }
        }
        
        if (api.isAttribution(item)) {
            UUID attribution = api.getAttribution(item);
            if (attribution != null) {
                String ownerName = player.getServer().getOfflinePlayer(attribution).getName();
                ownerName = ownerName != null ? ownerName : attribution.toString();
                
                boolean isOwner = attribution.equals(player.getUniqueId());
                NamedTextColor ownerColor = isOwner ? NamedTextColor.GREEN : NamedTextColor.RED;
                
                info.add(Component.text("귀속: ", NamedTextColor.GRAY)
                        .append(Component.text(ownerName, ownerColor)));
                
                if (!isOwner) {
                    info.add(Component.text("※ 이 아이템은 다른 플레이어에게 귀속되어 있습니다.", NamedTextColor.RED));
                }
            }
        }
        
        info.add(Component.text("================", NamedTextColor.GOLD, TextDecoration.BOLD));
        
        for (Component line : info) {
            player.sendMessage(line);
        }
    }
    
    @Nullable
    public static ItemStack findItemInInventory(@NotNull Player player, @NotNull Material material) {
        PlayerInventory inventory = player.getInventory();
        
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() == material) {
                return item;
            }
        }
        
        return null;
    }
    
    public static int countItems(@NotNull Player player, @NotNull Material material) {
        PlayerInventory inventory = player.getInventory();
        int count = 0;
        
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() == material) {
                count += item.getAmount();
            }
        }
        
        return count;
    }
    
    public static boolean hasSpace(@NotNull Player player, @NotNull ItemStack item) {
        PlayerInventory inventory = player.getInventory();
        
        for (ItemStack invItem : inventory.getStorageContents()) {
            if (invItem == null || invItem.getType() == Material.AIR) {
                return true;
            }
            
            if (invItem.isSimilar(item) && invItem.getAmount() < invItem.getMaxStackSize()) {
                return true;
            }
        }
        
        return false;
    }
}