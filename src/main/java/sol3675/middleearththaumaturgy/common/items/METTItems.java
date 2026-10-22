package sol3675.middleearththaumaturgy.common.items;

import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import sol3675.middleearththaumaturgy.references.LibMisc;
import sol3675.middleearththaumaturgy.wand.FactionWand;
import sol3675.middleearththaumaturgy.wand.FactionWands;
import sol3675.middleearththaumaturgy.wand.WandCapMaterial;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Items that are not blocks.
 *
 * <p>The donor carried three metadata families of wand cores - the ordinary rod, the staff rod and
 * the Thaumic Tinkerer rod - plus one family of caps. Metadata variants do not exist in 1.20.1, so
 * every variant becomes its own item, which also gives each rod its own model and translation key.
 */
public final class METTItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, LibMisc.MODID);

    public static final Map<String, RegistryObject<Item>> WAND_CORES;
    public static final Map<String, RegistryObject<Item>> STAFF_CORES;
    public static final Map<String, RegistryObject<Item>> TINKERER_CORES;
    public static final Map<String, RegistryObject<Item>> WAND_CAPS;

    /** The donor's only bauble: the warpward ring, worn in a Curios ring slot. */
    public static final RegistryObject<Item> RING_WARPWARD =
            ITEMS.register("ring_warpward", ItemWarpwardRing::new);

    static {
        Map<String, RegistryObject<Item>> wands = new LinkedHashMap<>();
        Map<String, RegistryObject<Item>> staffs = new LinkedHashMap<>();
        Map<String, RegistryObject<Item>> tinkerer = new LinkedHashMap<>();
        for (FactionWand wand : FactionWands.KEPT) {
            wands.put(wand.key(), ITEMS.register("wand_core_" + wand.key(), METTItems::partItem));
            staffs.put(wand.key(), ITEMS.register("staff_core_" + wand.key(), METTItems::partItem));
            tinkerer.put(wand.key(), ITEMS.register("tinkerer_core_" + wand.key(), METTItems::partItem));
        }
        WAND_CORES = Collections.unmodifiableMap(wands);
        STAFF_CORES = Collections.unmodifiableMap(staffs);
        TINKERER_CORES = Collections.unmodifiableMap(tinkerer);

        Map<String, RegistryObject<Item>> caps = new LinkedHashMap<>();
        for (WandCapMaterial cap : WandCapMaterial.ALL) {
            caps.put(cap.key(), ITEMS.register("wand_cap_" + cap.key(), METTItems::partItem));
        }
        WAND_CAPS = Collections.unmodifiableMap(caps);
    }

    private static Item partItem() {
        return new Item(new Item.Properties().stacksTo(64));
    }

    private METTItems() {
    }
}
