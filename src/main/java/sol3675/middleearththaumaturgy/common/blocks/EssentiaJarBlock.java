package sol3675.middleearththaumaturgy.common.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import sol3675.middleearththaumaturgy.common.blocks.entity.EssentiaJarBlockEntity;
import sol3675.middleearththaumaturgy.references.LibMisc;

import java.util.function.IntSupplier;

/**
 * A jar that stores more essentia than Thaumcraft's 64-unit warded jar.
 *
 * <p>The donor's three jars were {@code TileJarFillable} subclasses that only overrode the maximum
 * amount and the suction curve. TC4R's jar entity is final, so this block carries the parameters
 * instead: the storage id is what the datapack capacity lookup is keyed by, and the suction values
 * reproduce each donor jar.
 */
public class EssentiaJarBlock extends Block implements EntityBlock {
    private final String storagePath;
    private final IntSupplier defaultCapacity;
    private final int baseSuction;
    private final int filteredSuction;
    private final boolean scaledSuction;

    public EssentiaJarBlock(String storagePath, IntSupplier defaultCapacity, int baseSuction, int filteredSuction,
                            boolean scaledSuction, BlockBehaviour.Properties properties) {
        super(properties);
        this.storagePath = storagePath;
        this.defaultCapacity = defaultCapacity;
        this.baseSuction = baseSuction;
        this.filteredSuction = filteredSuction;
        this.scaledSuction = scaledSuction;
    }

    public static BlockBehaviour.Properties jarProperties() {
        return BlockBehaviour.Properties.of()
                .strength(0.3F)
                .sound(SoundType.GLASS)
                .noOcclusion();
    }

    public ResourceLocation storageId() {
        return new ResourceLocation(LibMisc.MODID, this.storagePath);
    }

    public int defaultCapacity() {
        return Math.max(1, this.defaultCapacity.getAsInt());
    }

    /** Suction this jar advertises, following the donor's per-jar curve. */
    public int suction(boolean filtered, int amount, int capacity) {
        int base = filtered ? this.filteredSuction : this.baseSuction;
        if (!this.scaledSuction || capacity <= 0) {
            return base;
        }
        return base + (int) ((long) amount * 8L / capacity);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EssentiaJarBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return (tickLevel, pos, tickState, entity) -> {
            if (entity instanceof EssentiaJarBlockEntity jar) {
                EssentiaJarBlockEntity.serverTick(tickLevel, pos, tickState, jar);
            }
        };
    }
}
