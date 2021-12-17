package kr.cosmoisland.cosmoislands.level.bukkit;

import com.minepalm.arkarangutils.bukkit1_16.v1_16InventorySerializer;
import kr.cosmoisland.cosmoislands.level.RewardDataAdapter;
import lombok.RequiredArgsConstructor;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class MinecraftItemRewardDataAdapter implements RewardDataAdapter<MinecraftItemRewardData> {

    private final Logger logger;

    @Override
    public MinecraftItemRewardData deserialize(int id, int requiredLevel, String str) {
        try {
            ItemStack[] items = v1_16InventorySerializer.itemStackArrayFromBase64(str);
            return new MinecraftItemRewardData(id, requiredLevel, items);
        }catch (IOException e){
            logger.warning("exception found at "+MinecraftItemRewardData.class.getSimpleName()+", "+e.getMessage());
            return null;
        }
    }

    @Override
    public String serialize(MinecraftItemRewardData data) {
        return v1_16InventorySerializer.itemStackArrayToBase64(data.items);
    }
}
