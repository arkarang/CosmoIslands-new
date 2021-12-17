package kr.cosmoislands.cosmoislands.bank;

import lombok.RequiredArgsConstructor;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@RequiredArgsConstructor
public class IslandInventoryView {

    final int level;
    final List<ItemStack> list;
}
