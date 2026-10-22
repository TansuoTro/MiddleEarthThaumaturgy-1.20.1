package sol3675.middleearththaumaturgy.common.blocks;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import sol3675.middleearththaumaturgy.common.blocks.entity.EssentiaJarBlockEntity;
import sol3675.middleearththaumaturgy.config.METTConfig;
import sol3675.middleearththaumaturgy.references.LibMisc;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    /**
     * The donor's metal storage blocks, in its own order.
     *
     * <p>They were one block with ten metadata values, each with its own light level, hardness and
     * blast resistance; metadata does not exist in 1.20.1, so each becomes its own block. The
     * harvest level is carried by the {@code needs_iron_tool} / {@code needs_diamond_tool} block tags
     * rather than by a per-block number.
     */
    public record Metal(String key, int light, float hardness, float resistance) {
    }

    public static final List<Metal> METALS = List.of(
            new Metal("gulduril_black_uruk", 4, 5.0F, 6.0F),
            new Metal("ithildin_blue_dwarven", 4, 5.0F, 6.0F),
            new Metal("ithildin_dwarven", 4, 5.0F, 6.0F),
            new Metal("edhelmir_elven", 5, 5.0F, 6.0F),
            new Metal("daedelos_galvorn", 4, 5.0F, 6.0F),
            new Metal("stared_mithril", 15, 30.0F, 120.0F),
            new Metal("daedelos_morgul", 0, 5.0F, 6.0F),
            new Metal("durnaur_orc", 7, 5.0F, 6.0F),
            new Metal("gulduril_uruk", 4, 5.0F, 6.0F),
            new Metal("brighten_gilded_iron", 10, 5.0F, 6.0F)
    );

    public static final Map<String, RegistryObject<Block>> STORAGE;

    static {
        Map<String, RegistryObject<Block>> storage = new LinkedHashMap<>();
        for (Metal metal : METALS) {
            String path = "storage_" + metal.key();
            RegistryObject<Block> block = BLOCKS.register(path, () -> new Block(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.METAL)
                            .strength(metal.hardness(), metal.resistance())
                            .lightLevel(state -> metal.light())
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.METAL)));
            BLOCK_ITEMS.register(path, () -> new BlockItem(block.get(), new Item.Properties()));
            storage.put(metal.key(), block);
        }
        STORAGE = Collections.unmodifiableMap(storage);
    }

    public static final RegistryObject<Block> BOOKSHELF_INVENTARIUM = registerBlock(
            "bookshelf_inventarium", () -> new InventariumBookshelfBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD).strength(1.0F, 1.0F).sound(SoundType.WOOD)));

    public static final RegistryObject<BlockEntityType<EssentiaJarBlockEntity>> ESSENTIA_JAR_ENTITY =
            BLOCK_ENTITIES.register("essentia_jar", () -> BlockEntityType.Builder.of(
                    EssentiaJarBlockEntity::new,
                    COMPRESSED_ESSENTIA_JAR.get(),
                    X2_COMPRESSED_ESSENTIA_JAR.get(),
                    EXTRALARGE_ESSENTIA_JAR.get()).build(null));

    private static RegistryObject<Block> registerBlock(String path, java.util.function.Supplier<Block> factory) {
        RegistryObject<Block> block = BLOCKS.register(path, factory);
        BLOCK_ITEMS.register(path, () -> new BlockItem(block.get(), new Item.Properties()));
        return block;
    }

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
