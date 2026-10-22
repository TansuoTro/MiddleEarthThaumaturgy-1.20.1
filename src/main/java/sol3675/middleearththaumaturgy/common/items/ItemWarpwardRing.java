package sol3675.middleearththaumaturgy.common.items;

import dev.tc4port.thaumcraft.api.aspect.VisChannel;
import dev.tc4port.thaumcraft.api.item.VisDiscountGear;
import dev.tc4port.thaumcraft.api.item.WarpingGear;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

/**
 * The warpward ring: a ring slot trinket that holds warp at bay and discounts every Vis cost.
 *
 * <p>The donor ring kept Thaumcraft 4's warp ward potion on the wearer and granted a flat five
 * percent discount. The reconstruction has no warp ward potion, so the ring instead reports a
 * negative {@link WarpingGear#thaumcraftWarp warp}, which Thaumcraft subtracts from the wearer's
 * total, and keeps the donor's five percent discount through {@link VisDiscountGear}.
 *
 * <p>Thaumcraft scans every equipped Curios slot for both interfaces, so the ring only needs to be
 * tagged into Curios' ring slot; no Curios class is referenced here.
 */
public class ItemWarpwardRing extends Item implements WarpingGear, VisDiscountGear {
    /** Warp the ring removes while worn. The donor's ward effect was stronger than this; the port keeps it modest. */
    private static final int WARD = 2;
    /** Percent taken off every Vis cost, as in the donor. */
    private static final int DISCOUNT = 5;

    public ItemWarpwardRing() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public int thaumcraftWarp(ItemStack stack, Optional<Player> wearer) {
        return -WARD;
    }

    @Override
    public int thaumcraftVisDiscount(ItemStack stack, Player player, VisChannel channel) {
        return DISCOUNT;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("mett.tooltip.ring_warpward", DISCOUNT, WARD));
    }
}
