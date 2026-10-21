package sol3675.middleearththaumaturgy.common.items;

import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import sol3675.middleearththaumaturgy.references.LibMisc;
import sol3675.middleearththaumaturgy.wand.FactionWand;
import sol3675.middleearththaumaturgy.wand.FactionWands;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Items that are not blocks. One wand core per surviving faction, as the donor's metadata variants. */
public final class METTItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, LibMisc.MODID);

    public static final Map<String, RegistryObject<Item>> WAND_CORES;

    static {
        Map<String, RegistryObject<Item>> cores = new LinkedHashMap<>();
        for (FactionWand wand : FactionWands.KEPT) {
            cores.put(wand.key(), ITEMS.register("wand_core_" + wand.key(),
                    () -> new Item(new Item.Properties().stacksTo(64))));
        }
        WAND_CORES = Collections.unmodifiableMap(cores);
    }

    private METTItems() {
    }
}
