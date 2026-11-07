package net.bdew.ae2stuff.waila;

import mcp.mobius.waila.api.IRegistrar;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.api.WailaPlugin;
import net.bdew.ae2stuff.grid.PoweredTile;
import net.bdew.ae2stuff.machines.wireless.TileWireless;

@WailaPlugin
public class WailaHandler implements IWailaPlugin {
    @Override
    public void register(IRegistrar registrar) {
        WailaPoweredDataProvider powered = new WailaPoweredDataProvider();
        registrar.registerComponentProvider(powered, TooltipPosition.BODY, PoweredTile.class);
        registrar.registerBlockDataProvider(powered, PoweredTile.class);

        WailaWirelessDataProvider wireless = new WailaWirelessDataProvider();
        registrar.registerComponentProvider(wireless, TooltipPosition.BODY, TileWireless.class);
        registrar.registerBlockDataProvider(wireless, TileWireless.class);
    }
}
