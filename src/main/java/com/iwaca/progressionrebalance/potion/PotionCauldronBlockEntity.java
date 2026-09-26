package com.iwaca.progressionrebalance.potion;

import com.iwaca.progressionrebalance.ProgressionRebalance;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

/**
 * The mixture in a {@link PotionCauldronBlock}: the potion kind (drinkable, splash or lingering) and its
 * contents. A single poured potion keeps its base potion; a mixture only has explicit effects. The
 * {@link DurationContributions} record where the durations came from, so more potions can be averaged in exactly.
 *
 * <p>The data is synced to clients, which need it to tint the liquid and to predict interactions.
 */
public class PotionCauldronBlockEntity extends BlockEntity {
    private static final String KIND_KEY = "kind";
    private static final String CONTENTS_KEY = "contents";
    private static final String DURATIONS_KEY = "durations";

    private Item kind = Items.POTION;
    private PotionContentsComponent contents = PotionContentsComponent.DEFAULT;
    private DurationContributions durations = DurationContributions.NONE;

    public PotionCauldronBlockEntity(BlockPos pos, BlockState state) {
        super(PotionCauldrons.BLOCK_ENTITY, pos, state);
    }

    public Item kind() {
        return kind;
    }

    public PotionContentsComponent contents() {
        return contents;
    }

    public DurationContributions durations() {
        return durations;
    }

    public int color() {
        return contents.getColor();
    }

    void setMixture(Item kind, PotionContentsComponent contents, DurationContributions durations) {
        this.kind = kind;
        this.contents = contents;
        this.durations = durations;
        markDirty();
        if (world != null) {
            world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_LISTENERS);
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        nbt.putString(KIND_KEY, Registries.ITEM.getId(kind).toString());
        PotionContentsComponent.CODEC.encodeStart(registries.getOps(NbtOps.INSTANCE), contents)
                .resultOrPartial(error -> ProgressionRebalance.LOGGER.warn("Could not save potion cauldron at {}: {}", pos, error))
                .ifPresent(tag -> nbt.put(CONTENTS_KEY, tag));
        if (!durations.isEmpty()) {
            nbt.put(DURATIONS_KEY, durations.toNbt());
        }
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        Identifier kindId = Identifier.tryParse(nbt.getString(KIND_KEY));
        Item savedKind = kindId == null ? Items.POTION : Registries.ITEM.get(kindId);
        kind = PotionMixing.KINDS.contains(savedKind) ? savedKind : Items.POTION;
        contents = nbt.contains(CONTENTS_KEY)
                ? PotionContentsComponent.CODEC.parse(registries.getOps(NbtOps.INSTANCE), nbt.get(CONTENTS_KEY))
                        .resultOrPartial(error -> ProgressionRebalance.LOGGER.warn("Could not load potion cauldron at {}: {}", pos, error))
                        .orElse(PotionContentsComponent.DEFAULT)
                : PotionContentsComponent.DEFAULT;
        // Cauldrons filled before durations were averaged count their contents as one potion.
        durations = DurationContributions.fromNbt(nbt.getCompound(DURATIONS_KEY))
                .orElseGet(() -> DurationContributions.ofPotionEffects(contents.getEffects()));
        if (world != null && world.isClient) {
            // Adding a potion does not change the block state, so ask for the new tint explicitly.
            world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_ALL);
        }
    }

    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registries) {
        return createNbt(registries);
    }

    /** The liquid tint, read by chunk rendering through Fabric's block view API. */
    @Override
    public @Nullable Object getRenderData() {
        return color();
    }
}
