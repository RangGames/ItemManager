package wiki.creeper.itemManager.example;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import wiki.creeper.itemManager.ItemManager;
import wiki.creeper.itemManager.api.ItemManagerAPI;
import wiki.creeper.itemManager.util.TimeUtil;

import java.sql.Timestamp;
import java.util.UUID;

public class ItemManagerExample {
    
    public void exampleUsage(Player player) {
        ItemManagerAPI api = ItemManager.getInstance().getItemManagerAPI();
        
        ItemStack diamond = new ItemStack(Material.DIAMOND, 1);
        
        Timestamp expireTime = TimeUtil.parseDurationToTimestamp("1h30m");
        ItemStack expirableDiamond = api.setExpireTime(diamond, expireTime);
        
        player.getInventory().addItem(expirableDiamond);
        
        
        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD, 1);
        
        ItemStack attributedSword = api.setAttribution(sword, player.getUniqueId());
        
        player.getInventory().addItem(attributedSword);
        
        
        ItemStack specialItem = new ItemStack(Material.NETHERITE_INGOT, 1);
        
        Timestamp expire = TimeUtil.parseDurationToTimestamp("7d");
        specialItem = api.setExpireTime(specialItem, expire);
        specialItem = api.setAttribution(specialItem, player.getUniqueId());
        
        player.getInventory().addItem(specialItem);
        
        
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        
        if (api.isExpireItem(itemInHand)) {
            Timestamp itemExpireTime = api.getExpireTime(itemInHand);
            if (itemExpireTime != null && api.isExpired(itemInHand)) {
                player.sendMessage("이 아이템은 만료되었습니다!");
            }
        }
        
        if (api.isAttribution(itemInHand)) {
            UUID owner = api.getAttribution(itemInHand);
            if (owner != null && !owner.equals(player.getUniqueId())) {
                player.sendMessage("이 아이템은 다른 플레이어의 소유입니다!");
            }
        }
        
        if (!api.canUse(itemInHand, player.getUniqueId())) {
            player.sendMessage("이 아이템을 사용할 수 없습니다!");
        }
        
        
        ItemStack cleanItem = api.removeExpireTime(itemInHand);
        cleanItem = api.removeAttribution(cleanItem);
        
        player.getInventory().setItemInMainHand(cleanItem);
        
        
        ItemStack customItem = new ItemStack(Material.GOLDEN_APPLE, 1);
        
        Timestamp specificTime = new Timestamp(System.currentTimeMillis() + 3600000);
        customItem = api.setExpireTime(customItem, specificTime);
        
        UUID targetPlayerUUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        customItem = api.setAttribution(customItem, targetPlayerUUID);
        
        player.getInventory().addItem(customItem);
        
        
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && api.isExpired(item)) {
                player.getInventory().remove(item);
            }
        }
    }
}