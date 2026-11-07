package net.bdew.ae2stuff.waila;

import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IDataAccessor;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.IServerDataProvider;
import net.bdew.ae2stuff.grid.PoweredTile;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import java.util.List;

public class WailaPoweredDataProvider implements IComponentProvider, IServerDataProvider<TileEntity> {
    @Override
    public void appendServerData(CompoundNBT tag, ServerPlayerEntity player, World world, TileEntity tile) {
        if (tile instanceof PoweredTile) {
            PoweredTile powered = (PoweredTile) tile;
            tag.putDouble("waila_power_stored", powered.getPowerStored());
            tag.putDouble("waila_power_capacity", powered.getPowerCapacity());
            tag.putBoolean("waila_power_sleep", powered.isSleeping());
        }
    }

    @Override
    public void appendBody(List<ITextComponent> tooltip, IDataAccessor accessor, IPluginConfig config) {
        CompoundNBT nbt = accessor.getServerData();
        if (nbt.contains("waila_power_stored")) {
            tooltip.add(new TranslationTextComponent("ae2stuff.waila.power",
                    DecFormat.shortFormat(nbt.getDouble("waila_power_stored")), DecFormat.shortFormat(nbt.getDouble("waila_power_capacity"))));
            if (nbt.getBoolean("waila_power_sleep"))
                tooltip.add(new TranslationTextComponent("ae2stuff.waila.sleep.true").mergeStyle(TextFormatting.RED));
            else
                tooltip.add(new TranslationTextComponent("ae2stuff.waila.sleep.false").mergeStyle(TextFormatting.GREEN));
        }
    }
}
