package sol3675.middleearththaumaturgy.focus;

import dev.tc4port.thaumcraft.api.focus.FocusApi;
import dev.tc4port.thaumcraft.api.focus.FocusId;
import dev.tc4port.thaumcraft.api.focus.FocusIdentityItem;
import dev.tc4port.thaumcraft.api.focus.FocusStateView;
import dev.tc4port.thaumcraft.api.focus.FocusUpgradeId;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Base for the two focus families the donor carried.
 *
 * <p>A Thaumcraft 4 focus item was an {@code ItemFocusBasic} subclass holding its Vis cost in Java.
 * In the 1.20.1 reconstruction the cost, the upgrade ranks and the cast pipeline are data: the item
 * only declares which catalog entry it is ({@link FocusIdentityItem}) and keeps the upgrades the
 * player chose. The behaviour lives in {@link BedrockRemoverFocus} and {@link SummonFocus}.
 */
public class ItemMETTFocus extends Item implements FocusIdentityItem {
    private static final String UPGRADES = "FocusUpgrades";
    private static final String PROPERTIES = "FocusProperties";

    private final FocusId focusId;

    public ItemMETTFocus(FocusId focusId) {
        super(new Item.Properties().stacksTo(1));
        this.focusId = focusId;
    }

    @Override
    public FocusId focusId(ItemStack stack) {
        return focusId;
    }

    @Override
    public FocusStateView focusState(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return new FocusStateView(focusId, List.of());
        }
        List<FocusUpgradeId> upgrades = new ArrayList<>();
        for (Tag entry : tag.getList(UPGRADES, Tag.TAG_STRING)) {
            upgrades.add(FocusUpgradeId.parse(entry.getAsString()));
        }
        Map<ResourceLocation, String> properties = new LinkedHashMap<>();
        CompoundTag stored = tag.getCompound(PROPERTIES);
        for (String key : stored.getAllKeys()) {
            ResourceLocation id = ResourceLocation.tryParse(key);
            if (id != null) {
                properties.put(id, stored.getString(key));
            }
        }
        return new FocusStateView(focusId, upgrades, properties);
    }

    @Override
    public ItemStack withFocusState(ItemStack stack, FocusStateView state) {
        ItemStack copy = stack.copyWithCount(1);
        CompoundTag tag = copy.getOrCreateTag();
        ListTag upgrades = new ListTag();
        state.upgradeSlots().forEach(upgrade -> upgrades.add(StringTag.valueOf(upgrade.value().toString())));
        tag.put(UPGRADES, upgrades);
        CompoundTag properties = new CompoundTag();
        state.properties().forEach((key, value) -> properties.putString(key.toString(), value));
        tag.put(PROPERTIES, properties);
        return copy;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        FocusApi.appendFocusTooltip(stack, tooltip);
    }
}
