package net.bdew.ae2stuff.waila;

import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IDataAccessor;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.IServerDataProvider;
import net.bdew.ae2stuff.machines.wireless.TileWireless;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import java.util.List;

public class WailaWirelessDataProvider implements IComponentProvider, IServerDataProvider<TileEntity> {
    private static final String TAG = "wireless_waila";

    @Override
    public void appendServerData(CompoundNBT tag, ServerPlayerEntity player, World world, TileEntity tile) {
        if (tile instanceof TileWireless) {
            TileWireless wireless = (TileWireless) tile;
            CompoundNBT data = new CompoundNBT();
            BlockPos link = wireless.getLinkPos();
            data.putBoolean("connected", link != null);
            if (link != null) {
                data.put("target", NBTUtil.writeBlockPos(link));
                data.putInt("channels", wireless.getConnection() != null ? wireless.getConnection().getUsedChannels() : 0);
                data.putDouble("power", wireless.getIdlePowerUsage());
            }
            tag.put(TAG, data);
        }
    }

    @Override
    public void appendBody(List<ITextComponent> tooltip, IDataAccessor accessor, IPluginConfig config) {
        CompoundNBT nbt = accessor.getServerData();
        if (!nbt.contains(TAG))
            return;
        CompoundNBT data = nbt.getCompound(TAG);
        if (data.getBoolean("connected")) {
            BlockPos pos = NBTUtil.readBlockPos(data.getCompound("target"));
            tooltip.add(new TranslationTextComponent("ae2stuff.waila.wireless.connected", pos.getX(), pos.getY(), pos.getZ()));
            tooltip.add(new TranslationTextComponent("ae2stuff.waila.wireless.channels", data.getInt("channels")));
            tooltip.add(new TranslationTextComponent("ae2stuff.waila.wireless.power", DecFormat.shortFormat(data.getDouble("power"))));
        } else {
            tooltip.add(new TranslationTextComponent("ae2stuff.waila.wireless.notconnected"));
        }
    }
}
