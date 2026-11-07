package net.bdew.ae2stuff.grid;

import appeng.api.networking.GridFlags;
import appeng.api.networking.GridNotification;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridBlock;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.events.MENetworkEvent;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import net.bdew.ae2stuff.AE2Defs;
import net.bdew.ae2stuff.misc.ItemUtils;
import net.bdew.ae2stuff.misc.MachineBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;

import javax.annotation.Nullable;
import java.util.EnumSet;

public abstract class GridTile extends TileEntity implements ITickableTileEntity, IGridHost, IGridBlock {
    private static final String NODE_TAG = "ae_node";

    protected IGridNode node;
    private boolean initialized;
    private boolean chunkUnloaded;
    @Nullable
    public PlayerEntity placingPlayer;

    // TileEntity#read now runs before the world is assigned, so the saved node is applied on first access
    @Nullable
    private CompoundNBT pendingNodeData;

    protected GridTile(TileEntityType<?> type) {
        super(type);
    }

    @Override
    public final void tick() {
        if (world != null && !world.isRemote)
            serverTick();
    }

    protected void serverTick() {
        if (!initialized) {
            IGridNode gridNode = getNode();
            if (placingPlayer != null)
                gridNode.setPlayerID(Security.getPlayerId(placingPlayer));
            gridNode.updateState();
            initialized = true;
        }
    }

    protected void saveData(CompoundNBT tag) {
        if (node != null)
            node.saveToNBT(NODE_TAG, tag);
        else if (pendingNodeData != null)
            tag.put(NODE_TAG, pendingNodeData.getCompound(NODE_TAG));
    }

    protected void loadData(CompoundNBT tag) {
        unRegisterNode();
        pendingNodeData = tag.contains(NODE_TAG) ? tag : null;
        initialized = false;
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag);
        saveData(tag);
        return tag;
    }

    @Override
    public void read(BlockState state, CompoundNBT tag) {
        super.read(state, tag);
        loadData(tag);
    }

    /**
     * Marks the chunk for saving, without the side effects that markDirty has on inventories.
     */
    protected void markSaveDirty() {
        if (world != null)
            world.markChunkDirty(pos, this);
    }

    /**
     * Updates the active texture. Destroying the node on chunk unload notifies the tile, and touching the
     * block at that point would load the chunk right back.
     */
    protected void setBlockActive(boolean active) {
        if (world != null && !world.isRemote && !chunkUnloaded)
            MachineBlock.setActive(world, pos, active);
    }

    public void unRegisterNode() {
        if (node != null) {
            node.destroy();
            node = null;
            initialized = false;
        }
    }

    @Override
    public void remove() {
        unRegisterNode();
        super.remove();
    }

    @Override
    public void onChunkUnloaded() {
        chunkUnloaded = true;
        unRegisterNode();
        super.onChunkUnloaded();
    }

    @Nullable
    public IGridNode getNode() {
        if (world == null || world.isRemote)
            return null;
        if (node == null) {
            node = AE2Defs.api().grid().createGridNode(this);
            if (pendingNodeData != null) {
                node.loadFromNBT(NODE_TAG, pendingNodeData);
                pendingNodeData = null;
            }
        }
        return node;
    }

    public boolean isNodeActive() {
        IGridNode gridNode = getNode();
        return gridNode != null && gridNode.isActive();
    }

    protected boolean safePostEvent(MENetworkEvent event) {
        IGridNode gridNode = getNode();
        IGrid grid = gridNode != null ? gridNode.getGrid() : null;
        if (grid == null)
            return false;
        grid.postEvent(event);
        return true;
    }

    // IGridHost

    @Nullable
    @Override
    public IGridNode getGridNode(AEPartLocation location) {
        return getNode();
    }

    @Override
    public AECableType getCableConnectionType(AEPartLocation location) {
        return AECableType.COVERED;
    }

    @Override
    public void securityBreak() {
        ItemUtils.throwItemAt(world, pos, new ItemStack(getBlockState().getBlock()));
        world.removeBlock(pos, false);
    }

    // IGridBlock

    @Override
    public double getIdlePowerUsage() {
        return 0;
    }

    @Override
    public EnumSet<GridFlags> getFlags() {
        return EnumSet.noneOf(GridFlags.class);
    }

    @Override
    public AEColor getGridColor() {
        return AEColor.TRANSPARENT;
    }

    @Override
    public EnumSet<Direction> getConnectableSides() {
        return EnumSet.allOf(Direction.class);
    }

    @Override
    public IGridHost getMachine() {
        return this;
    }

    @Override
    public boolean isWorldAccessible() {
        return true;
    }

    @Override
    public DimensionalCoord getLocation() {
        return new DimensionalCoord(this);
    }

    @Override
    public void onGridNotification(GridNotification notification) {
    }

    @Override
    public void gridChanged() {
    }
}
