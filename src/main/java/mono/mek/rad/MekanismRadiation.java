package mono.mek.rad;

import com.mojang.logging.LogUtils;
import mono.mek.rad.item.Items;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

import static mono.mek.rad.item.Items.ANTI_RADIATION_PILLS;
import static mono.mek.rad.item.Items.RADIATION_CLEANER;

@Mod(MekanismRadiation.MODID)
public class MekanismRadiation {
    public static final String MODID = "mekanismradiation";
    public static final Logger LOGGER = LogUtils.getLogger();


    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);



    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("radiation_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.mekanismradiation"))
            .icon(() -> RADIATION_CLEANER.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(RADIATION_CLEANER.get());
                output.accept(ANTI_RADIATION_PILLS);
            }).build());

    public MekanismRadiation(IEventBus modEventBus, ModContainer modContainer) {
        Items.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        NeoForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::addCreative);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(RADIATION_CLEANER);
        }
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }
}
