package sol3675.middleearththaumaturgy.common.blocks.entity;

import dev.tc4port.thaumcraft.api.aspect.AspectAmounts;
import dev.tc4port.thaumcraft.api.aspect.AspectId;
import dev.tc4port.thaumcraft.api.aspect.AspectApi;
import dev.tc4port.thaumcraft.api.essentia.EssentiaApi;
import dev.tc4port.thaumcraft.api.essentia.EssentiaSource;
import dev.tc4port.thaumcraft.api.essentia.EssentiaStorageView;
import dev.tc4port.thaumcraft.api.essentia.EssentiaStorageApi;
import dev.tc4port.thaumcraft.api.essentia.EssentiaTransferMode;
import dev.tc4port.thaumcraft.api.essentia.EssentiaTransport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import sol3675.middleearththaumaturgy.common.blocks.EssentiaJarBlock;
import sol3675.middleearththaumaturgy.common.blocks.METTBlocks;

/**
 * Essentia storage for the compressed jars.
 *
 * <p>Reproduces the donor's {@code TileJarFillable} subclasses: one aspect per jar, filling from a
 * transport above, and a suction curve taken from the block. Unlike Thaumcraft's own jar this one
 * never labels itself, which matches the donor - its jars only differed in capacity.
 */
public class EssentiaJarBlockEntity extends BlockEntity
        implements EssentiaTransport, EssentiaSource, EssentiaStorageView {

    private static final String KEY_ASPECT = "Aspect";
    private static final String KEY_AMOUNT = "Amount";
    /** The donor stored the label's aspect under this name. */
    private static final String KEY_FILTER = "AspectFilter";

    @Nullable
    private AspectId aspect;
    private int amount;
    @Nullable
    private AspectId filter;
    private int tick;

    public EssentiaJarBlockEntity(BlockPos pos, BlockState state) {
        super(METTBlocks.ESSENTIA_JAR_ENTITY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, EssentiaJarBlockEntity jar) {
        jar.tick++;
        if (jar.tick % 5 == 0) {
            jar.fillFromAbove();
        }
    }

    private EssentiaJarBlock jar() {
        return this.getBlockState().getBlock() instanceof EssentiaJarBlock block ? block : null;
    }

    public int capacity() {
        EssentiaJarBlock block = jar();
        if (block == null) {
            return 64;
        }
        return EssentiaStorageApi.capacity(block.storageId(), block.defaultCapacity(),
                this.level != null && this.level.isClientSide);
    }

    @Override
    public int essentiaCapacity() {
        return capacity();
    }

    @Nullable
    public AspectId filter() {
        return this.filter;
    }

    @Nullable
    @Override
    public AspectId essentiaFilter() {
        return filter();
    }

    @Nullable
    public AspectId aspect() {
        return this.aspect;
    }

    public int amount() {
        return this.amount;
    }

    /** @return whether the label was applied */
    public boolean setFilter(@Nullable AspectId selected) {
        if (selected == null || this.filter != null) {
            return false;
        }
        this.filter = selected;
        this.setChanged();
        this.sync();
        return true;
    }

    /** @return the aspect that was filtered, or null if there was no label */
    @Nullable
    public AspectId clearFilter() {
        if (this.filter == null) {
            return null;
        }
        AspectId removed = this.filter;
        this.filter = null;
        this.setChanged();
        this.sync();
        return removed;
    }

    @Override
    public AspectAmounts visibleAspects() {
        return this.aspect == null || this.amount <= 0 ? AspectAmounts.EMPTY : AspectAmounts.of(this.aspect, this.amount);
    }

    // ------------------------------------------------------------------ storage

    /** @return how much of {@code requested} did not fit */
    private int insert(AspectId candidate, int requested) {
        if (requested <= 0 || !AspectApi.contains(candidate)) {
            return requested;
        }
        if (this.amount > 0 && !candidate.equals(this.aspect)) {
            return requested;
        }
        int accepted = (int) Math.min(requested, Math.max(0L, (long) capacity() - this.amount));
        if (accepted <= 0) {
            return requested;
        }
        this.aspect = candidate;
        this.amount += accepted;
        this.setChanged();
        this.sync();
        return requested - accepted;
    }

    private boolean remove(AspectId candidate, int requested) {
        if (requested <= 0 || this.amount < requested || this.aspect == null || !candidate.equals(this.aspect)) {
            return false;
        }
        this.amount -= requested;
        if (this.amount <= 0) {
            this.amount = 0;
            this.aspect = null;
        }
        this.setChanged();
        this.sync();
        return true;
    }

    @Override
    public int extractEssentia(AspectId aspect, int amount, EssentiaTransferMode mode) {
        if (mode.executes()) {
            return remove(aspect, amount) ? amount : 0;
        }
        return this.amount >= amount && aspect.equals(this.aspect) ? amount : 0;
    }

    // ---------------------------------------------------------------- transport

    @Override
    public boolean isConnectable(Direction face) {
        return face == Direction.UP;
    }

    @Override
    public boolean canInputFrom(Direction face) {
        return face == Direction.UP;
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return face == Direction.UP;
    }

    @Nullable
    @Override
    public AspectId suctionType(Direction face) {
        if (this.aspect != null && AspectApi.contains(this.aspect)) {
            return this.aspect;
        }
        return this.filter != null && AspectApi.contains(this.filter) ? this.filter : null;
    }

    @Override
    public int suctionAmount(Direction face) {
        EssentiaJarBlock block = jar();
        if (block == null) {
            return 0;
        }
        if (this.amount >= capacity()) {
            return 0;
        }
        // A labelled jar advertises the filtered curve, as TileJarFillable did.
        return block.suction(this.filter != null, this.amount, capacity());
    }

    @Override
    public int minimumSuction() {
        EssentiaJarBlock block = jar();
        return block == null ? 48 : block.suction(this.filter != null, this.amount, capacity());
    }

    @Override
    public int takeEssentia(AspectId aspect, int amount, Direction face, EssentiaTransferMode mode) {
        if (!canOutputTo(face) || this.aspect == null || !aspect.equals(this.aspect)) {
            return 0;
        }
        if (mode.executes()) {
            return remove(aspect, amount) ? amount : 0;
        }
        return this.amount >= amount ? amount : 0;
    }

    @Override
    public int addEssentia(AspectId aspect, int amount, Direction face, EssentiaTransferMode mode) {
        if (!canInputFrom(face) || amount <= 0) {
            return 0;
        }
        if (this.amount > 0 && !aspect.equals(this.aspect)) {
            return 0;
        }
        int accepted = (int) Math.min(amount, Math.max(0L, (long) capacity() - this.amount));
        if (accepted > 0 && mode.executes()) {
            insert(aspect, accepted);
        }
        return accepted;
    }

    @Nullable
    @Override
    public AspectId essentiaType(Direction face) {
        return this.aspect != null && AspectApi.contains(this.aspect) ? this.aspect : null;
    }

    @Override
    public int essentiaAmount(Direction face) {
        return this.aspect == null ? 0 : this.amount;
    }

    @Override
    public boolean renderExtendedTube() {
        return true;
    }

    /** Pulls one unit from the transport above when that side can push into this jar. */
    private void fillFromAbove() {
        if (this.level == null || this.level.isClientSide || this.amount >= capacity()) {
            return;
        }
        EssentiaTransport above = EssentiaApi.transport(this.level, this.worldPosition.above(), Direction.DOWN);
        if (above == null || !above.canOutputTo(Direction.DOWN)) {
            return;
        }
        AspectId candidate = this.aspect;
        if (candidate == null) {
            candidate = above.essentiaType(Direction.DOWN);
        }
        if (candidate == null || !AspectApi.contains(candidate)) {
            return;
        }
        if (above.suctionAmount(Direction.DOWN) >= this.suctionAmount(Direction.UP)) {
            return;
        }
        int taken = EssentiaApi.take(this.level, above, candidate, 1, Direction.DOWN, EssentiaTransferMode.EXECUTE);
        if (taken > 0) {
            insert(candidate, taken);
        }
    }

    private void sync() {
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    // --------------------------------------------------------------------- nbt

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.aspect = tag.contains(KEY_ASPECT) ? parseAspect(tag.getString(KEY_ASPECT)) : null;
        this.amount = Math.max(0, tag.getInt(KEY_AMOUNT));
        this.filter = tag.contains(KEY_FILTER) ? parseAspect(tag.getString(KEY_FILTER)) : null;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (this.aspect != null) {
            tag.putString(KEY_ASPECT, this.aspect.serialized());
        }
        if (this.filter != null) {
            tag.putString(KEY_FILTER, this.filter.serialized());
        }
        tag.putInt(KEY_AMOUNT, this.amount);
    }

    @Nullable
    private static AspectId parseAspect(String value) {
        try {
            return AspectId.parse(value);
        } catch (RuntimeException exception) {
            return null;
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
