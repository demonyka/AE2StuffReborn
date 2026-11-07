package net.bdew.ae2stuff;

import net.bdew.ae2stuff.items.ItemWirelessKit;
import net.bdew.ae2stuff.items.visualiser.ItemVisualiser;
import net.bdew.ae2stuff.machines.encoder.BlockEncoder;
import net.bdew.ae2stuff.machines.encoder.ContainerEncoder;
import net.bdew.ae2stuff.machines.encoder.TileEncoder;
import net.bdew.ae2stuff.machines.grower.BlockGrower;
import net.bdew.ae2stuff.machines.grower.ContainerGrower;
import net.bdew.ae2stuff.machines.grower.TileGrower;
import net.bdew.ae2stuff.machines.inscriber.BlockInscriber;
import net.bdew.ae2stuff.machines.inscriber.ContainerInscriber;
import net.bdew.ae2stuff.machines.inscriber.TileInscriber;
import net.bdew.ae2stuff.machines.wireless.BlockWireless;
import net.bdew.ae2stuff.machines.wireless.TileWireless;
import net.minecraft.block.Block;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

/**
 * Block, item, tile and container registration. Entries of a disabled machine stay null.
 */
public final class Registration {
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, AE2Stuff.MOD_ID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, AE2Stuff.MOD_ID);
    private static final DeferredRegister<TileEntityType<?>> TILES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, AE2Stuff.MOD_ID);
    private static final DeferredRegister<ContainerType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, AE2Stuff.MOD_ID);

    public static final ItemGroup TAB = new ItemGroup("bdew.ae2stuff") {
        @Override
        public ItemStack createIcon() {
            return ENCODER != null ? new ItemStack(ENCODER.get()) : new ItemStack(VISUALISER.get());
        }
    };

    public static RegistryObject<BlockEncoder> ENCODER;
    public static RegistryObject<BlockGrower> GROWER;
    public static RegistryObject<BlockInscriber> INSCRIBER;
    public static RegistryObject<BlockWireless> WIRELESS;

    public static RegistryObject<TileEntityType<TileEncoder>> ENCODER_TILE;
    public static RegistryObject<TileEntityType<TileGrower>> GROWER_TILE;
    public static RegistryObject<TileEntityType<TileInscriber>> INSCRIBER_TILE;
    public static RegistryObject<TileEntityType<TileWireless>> WIRELESS_TILE;

    public static RegistryObject<ContainerType<ContainerEncoder>> ENCODER_CONTAINER;
    public static RegistryObject<ContainerType<ContainerGrower>> GROWER_CONTAINER;
    public static RegistryObject<ContainerType<ContainerInscriber>> INSCRIBER_CONTAINER;

    public static RegistryObject<ItemWirelessKit> WIRELESS_KIT;
    public static RegistryObject<ItemVisualiser> VISUALISER;

    private Registration() {
    }

    static void init(IEventBus modBus) {
        if (Tuning.isEnabled("encoder")) {
            ENCODER = block("encoder", BlockEncoder::new);
            ENCODER_TILE = tile("encoder", TileEncoder::new, ENCODER);
            ENCODER_CONTAINER = CONTAINERS.register("encoder", () -> IForgeContainerType.create(ContainerEncoder::fromNetwork));
        }
        if (Tuning.isEnabled("grower")) {
            GROWER = block("grower", BlockGrower::new);
            GROWER_TILE = tile("grower", TileGrower::new, GROWER);
            GROWER_CONTAINER = CONTAINERS.register("grower", () -> IForgeContainerType.create(ContainerGrower::fromNetwork));
        }
        if (Tuning.isEnabled("inscriber")) {
            INSCRIBER = block("inscriber", BlockInscriber::new);
            INSCRIBER_TILE = tile("inscriber", TileInscriber::new, INSCRIBER);
            INSCRIBER_CONTAINER = CONTAINERS.register("inscriber", () -> IForgeContainerType.create(ContainerInscriber::fromNetwork));
        }
        if (Tuning.isEnabled("wireless")) {
            WIRELESS = block("wireless", BlockWireless::new);
            WIRELESS_TILE = tile("wireless", TileWireless::new, WIRELESS);
            WIRELESS_KIT = ITEMS.register("wireless_kit", ItemWirelessKit::new);
        }
        VISUALISER = ITEMS.register("visualiser", ItemVisualiser::new);

        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        TILES.register(modBus);
        CONTAINERS.register(modBus);
    }

    private static <T extends Block> RegistryObject<T> block(String name, Supplier<T> factory) {
        RegistryObject<T> block = BLOCKS.register(name, factory);
        ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().group(TAB)));
        return block;
    }

    private static <T extends TileEntity> RegistryObject<TileEntityType<T>> tile(String name, Supplier<T> factory, RegistryObject<? extends Block> block) {
        return TILES.register(name, () -> TileEntityType.Builder.create(factory, block.get()).build(null));
    }
}
