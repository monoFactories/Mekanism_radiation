package mono.mek.rad.item;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

import static mono.mek.rad.MekanismRadiation.MODID;

public class Items {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);

    public static final DeferredItem<RadiationCleaner> RADIATION_CLEANER = reg("radiation_cleaner", RadiationCleaner::new);
    public static final DeferredItem<AntiRadiationPills> ANTI_RADIATION_PILLS = reg("anti_radiation_pills", AntiRadiationPills::new);

    private static <T extends Item> DeferredItem<T> reg(String name, Supplier<T> supplier) {
        return ITEMS.register(name, supplier);
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
