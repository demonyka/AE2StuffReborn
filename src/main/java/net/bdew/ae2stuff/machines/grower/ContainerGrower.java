package net.bdew.ae2stuff.machines.grower;

import net.bdew.ae2stuff.Registration;
import net.bdew.ae2stuff.misc.ContainerUpgradeable;
import net.bdew.ae2stuff.misc.SlotValidating;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class ContainerGrower extends ContainerUpgradeable {
    public final TileGrower te;

    public ContainerGrower(int id, PlayerInventory inventory, TileGrower te) {
        super(Registration.GROWER_CONTAINER.get(), id, te);
        this.te = te;

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++)
                addSlot(new SlotValidating(te, x + y * 9, 8 + x * 18, 17 + y * 18));
        }

        for (int i = 0; i < te.upgrades.getSizeInventory(); i++)
            addSlot(new SlotValidating(te.upgrades, i, 187, 8 + i * 18));

        bindPlayerInventory(inventory, 8, 84, 142);
        initUpgradeable(te, te.upgrades, inventory.player, 186, 94);
    }

    public static ContainerGrower fromNetwork(int id, PlayerInventory inventory, PacketBuffer data) {
        return new ContainerGrower(id, inventory, (TileGrower) inventory.player.world.getTileEntity(data.readBlockPos()));
    }
}
