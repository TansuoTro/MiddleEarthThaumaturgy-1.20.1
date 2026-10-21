package sol3675.middleearththaumaturgy.common.blocks;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import sol3675.middleearththaumaturgy.common.blocks.entity.EssentiaJarBlockEntity;
import sol3675.middleearththaumaturgy.config.METTConfig;
import sol3675.middleearththaumaturgy.references.LibMisc;

/** Blocks and their block entities. */
public final class METTBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, LibMisc.MODID);
    public static final DeferredRegister<Item> BLOCK_ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, LibMisc.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, LibMisc.MODID);

    public static final RegistryObject<Block> COMPRESSED_ESSENTIA_JAR = registerJar(
            "compressed_essentia_jar", METTConfig::compressedJarCapacity, 48, 56, true);
    public static final RegistryObject<Block> X2_COMPRESSED_ESSENTIA_JAR = registerJar(
            "x2_compressed_essentia_jar", METTConfig::x2CompressedJarCapacity, 48, 56, true);
    public static final RegistryObject<Block> EXTRALARGE_ESSENTIA_JAR = registerJar(
            "extralarge_essentia_jar", METTConfig::extralargeJarCapacity, 56, 64, false);

    public static final RegistryObject<BlockEntityType<EssentiaJarBlockEntity>> ESSENTIA_JAR_ENTITY =
            BLOCK_ENTITIES.register("essentia_jar", () -> BlockEntityType.Builder.of(
                    EssentiaJarBlockEntity::new,
                    COMPRESSED_ESSENTIA_JAR.get(),
                    X2_COMPRESSED_ESSENTIA_JAR.get(),
                    EXTRALARGE_ESSENTIA_JAR.get()).build(null));

    private static RegistryObject<Block> registerJar(String path, java.util.function.IntSupplier capacity,
                                                     int suction, int filteredSuction, boolean scaled) {
        RegistryObject<Block> block = BLOCKS.register(path, () -> new EssentiaJarBlock(
                path, capacity, suction, filteredSuction, scaled, EssentiaJarBlock.jarProperties()));
        BLOCK_ITEMS.register(path, () -> new BlockItem(block.get(), new Item.Properties()));
        return block;
    }

    private METTBlocks() {
    }
}
