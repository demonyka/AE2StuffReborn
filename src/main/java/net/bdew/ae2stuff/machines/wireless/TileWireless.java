package net.bdew.ae2stuff.machines.wireless;

import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.events.MENetworkPowerIdleChange;
import net.bdew.ae2stuff.AE2Defs;
import net.bdew.ae2stuff.AE2Stuff;
import net.bdew.ae2stuff.Registration;
import net.bdew.ae2stuff.Tuning;
import net.bdew.ae2stuff.grid.GridTile;
import net.bdew.ae2stuff.misc.MachineBlock;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.EnumSet;

public class TileWireless extends GridTile {
    private static final String LINK_TAG = "link";

    @Nullable
    private BlockPos link;
    @Nullable
    private IGridConnection connection;
    private double currentPowerUsage;

    public TileWireless() {
        super(Registration.WIRELESS_TILE.get());
    }

    public boolean isLinked() {
        return link != null;
    }

    @Nullable
    public BlockPos getLinkPos() {
        return link;
    }

    @Nullable
    public IGridConnection getConnection() {
        return connection;
    }

    @Nullable
    private TileWireless getLink() {
        if (link == null)
            return null;
        TileEntity tile = world.getTileEntity(link);
        return tile instanceof TileWireless ? (TileWireless) tile : null;
    }

    private void setLink(@Nullable BlockPos value) {
        link = value;
        markSaveDirty();
        if (world != null && !world.isRemote) {
            BlockState state = getBlockState();
            world.notifyBlockUpdate(pos, state, state, 3);
        }
    }

    @Override
    public EnumSet<GridFlags> getFlags() {
        return EnumSet.of(GridFlags.DENSE_CAPACITY);
    }

    @Override
    protected void serverTick() {
        super.serverTick();
        if (connection == null && link != null)
            setupConnection();
    }

    public boolean doLink(TileWireless other) {
        if (other.link == null) {
            other.setLink(pos);
            setLink(other.getPos());
            return setupConnection();
        }
        return false;
    }

    public void doUnlink() {
        breakConnection();
        TileWireless that = getLink();
        if (that != null) {
            setLink(null);
            that.setLink(null);
        }
    }

    private boolean setupConnection() {
        TileWireless that = getLink();
        if (that != null) {
            try {
                connection = AE2Defs.api().grid().createGridConnection(getNode(), that.getNode());
                that.connection = connection;
                // Not BlockPos#distanceSq, on 1.16 that offsets only one of the two positions by 0.5
                double dx = pos.getX() - that.pos.getX();
                double dy = pos.getY() - that.pos.getY();
                double dz = pos.getZ() - that.pos.getZ();
                double power = Tuning.WIRELESS_POWER_BASE.get() + Tuning.WIRELESS_POWER_DISTANCE_MULTIPLIER.get() * (dx * dx + dy * dy + dz * dz);
                setIdlePowerUse(power);
                that.setIdlePowerUse(power);
                MachineBlock.setActive(world, pos, true);
                MachineBlock.setActive(world, that.pos, true);
                return true;
            } catch (Exception e) {
                AE2Stuff.LOG.warn("Failed setting up wireless link {} <-> {}", pos, that.pos, e);
                doUnlink();
            }
        }
        return false;
    }

    private void breakConnection() {
        if (connection != null)
            connection.destroy();
        connection = null;
        setIdlePowerUse(0D);
        TileWireless other = getLink();
        if (other != null) {
            other.connection = null;
            other.setIdlePowerUse(0D);
            MachineBlock.setActive(world, other.pos, false);
        }
        MachineBlock.setActive(world, pos, false);
    }

    @Override
    public ItemStack getMachineRepresentation() {
        return new ItemStack(Registration.WIRELESS.get());
    }

    @Override
    public double getIdlePowerUsage() {
        return currentPowerUsage;
    }

    private void setIdlePowerUse(double value) {
        if (value != currentPowerUsage) {
            currentPowerUsage = value;
            markSaveDirty();
            if (node != null && node.getGrid() != null)
                node.getGrid().postEvent(new MENetworkPowerIdleChange(node));
        }
    }

    private void writeLink(CompoundNBT tag) {
        if (link != null)
            tag.put(LINK_TAG, NBTUtil.writeBlockPos(link));
    }

    private void readLink(CompoundNBT tag) {
        link = tag.contains(LINK_TAG, Constants.NBT.TAG_COMPOUND) ? NBTUtil.readBlockPos(tag.getCompound(LINK_TAG)) : null;
    }

    @Override
    protected void saveData(CompoundNBT tag) {
        super.saveData(tag);
        writeLink(tag);
        tag.putDouble("_poweruse", currentPowerUsage);
    }

    @Override
    protected void loadData(CompoundNBT tag) {
        super.loadData(tag);
        readLink(tag);
        setIdlePowerUse(tag.getDouble("_poweruse"));
    }

    // The link is synced to clients for the overlay

    @Override
    public CompoundNBT getUpdateTag() {
        CompoundNBT tag = new CompoundNBT();
        tag.putInt("x", pos.getX());
        tag.putInt("y", pos.getY());
        tag.putInt("z", pos.getZ());
        writeLink(tag);
        return tag;
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT tag) {
        readLink(tag);
    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(pos, 0, getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet) {
        readLink(packet.getNbtCompound());
    }
}
