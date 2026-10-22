package sol3675.middleearththaumaturgy.common;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import sol3675.middleearththaumaturgy.common.blocks.METTBlocks;
import sol3675.middleearththaumaturgy.common.items.METTItems;
import sol3675.middleearththaumaturgy.references.LibMisc;

/**
 * The mod's own creative tab, replacing the donor's.
 *
 * <p>Without one, every item and block would be reachable only by id: Forge does not add a mod's
 * content to the vanilla tabs on its own.
 */
public final class METTCreativeTab {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, LibMisc.MODID);

    public static final RegistryObject<CreativeModeTab> MAIN = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.mett.main"))
                    .icon(() -> new ItemStack(METTItems.WAND_CORES.get("gondor").get()))
                    .displayItems((parameters, output) -> {
                        METTItems.ITEMS.getEntries().forEach(item -> output.accept(item.get()));
                        METTBlocks.BLOCK_ITEMS.getEntries().forEach(item -> output.accept(item.get()));
                    })
                    .build());

    private METTCreativeTab() {
    }
}
