package net.bdew.ae2stuff.machines.inscriber;

import net.bdew.ae2stuff.Registration;
import net.bdew.ae2stuff.misc.ContainerUpgradeable;
import net.bdew.ae2stuff.misc.SlotValidating;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IntReferenceHolder;
import net.minecraft.util.math.MathHelper;

public class ContainerInscriber extends ContainerUpgradeable {
    // Window properties are sent as shorts, so the float progress goes through a fixed scale
    private static final int PROGRESS_SCALE = 10000;

    // A tracked int starts at 0 and only changes are sent, so 0 must never be a real value or the
    // client would keep its own default when the GUI opens. All values are shifted by this offset.
    private static final int SYNC_OFFSET = 1;

    public final TileInscriber te;

    public ContainerInscriber(int id, PlayerInventory inventory, TileInscriber te) {
        super(Registration.INSCRIBER_CONTAINER.get(), id, te);
        this.te = te;

        addSlot(new SlotValidating(te, TileInscriber.SLOT_TOP, 45, 16));
        addSlot(new SlotValidating(te, TileInscriber.SLOT_MIDDLE, 63, 39));
        addSlot(new SlotValidating(te, TileInscriber.SLOT_BOTTOM, 45, 62));
        addSlot(new SlotValidating(te, TileInscriber.SLOT_OUTPUT, 113, 40));

        for (int i = 0; i < te.upgrades.getSizeInventory(); i++)
            addSlot(new SlotValidating(te.upgrades, i, 187, 8 + i * 18));

        bindPlayerInventory(inventory, 8, 94, 152);
        initUpgradeable(te, te.upgrades, inventory.player, 186, 113);

        trackInt(new IntReferenceHolder() {
            @Override
            public int get() {
                // Speed cards can push the progress past 1 for a tick, enough to overflow the short
                return Math.round(MathHelper.clamp(te.progress, 0F, 1F) * PROGRESS_SCALE) + SYNC_OFFSET;
            }

            @Override
            public void set(int value) {
                te.progress = (value - SYNC_OFFSET) / (float) PROGRESS_SCALE;
            }
        });
        trackInt(new IntReferenceHolder() {
            @Override
            public int get() {
                return (te.topLocked ? 1 : 0) + SYNC_OFFSET;
            }

            @Override
            public void set(int value) {
                te.topLocked = value - SYNC_OFFSET != 0;
            }
        });
        trackInt(new IntReferenceHolder() {
            @Override
            public int get() {
                return (te.bottomLocked ? 1 : 0) + SYNC_OFFSET;
            }

            @Override
            public void set(int value) {
                te.bottomLocked = value - SYNC_OFFSET != 0;
            }
        });
    }

    public static ContainerInscriber fromNetwork(int id, PlayerInventory inventory, PacketBuffer data) {
        return new ContainerInscriber(id, inventory, (TileInscriber) inventory.player.world.getTileEntity(data.readBlockPos()));
    }
}
