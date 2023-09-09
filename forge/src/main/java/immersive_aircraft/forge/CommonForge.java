package immersive_aircraft.forge;

import immersive_aircraft.*;
import immersive_aircraft.forge.cobalt.network.NetworkHandlerImpl;
import immersive_aircraft.forge.cobalt.registration.RegistrationImpl;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import static net.minecraft.core.registries.Registries.CREATIVE_MODE_TAB;

@Mod(Main.MOD_ID)
@Mod.EventBusSubscriber(modid = Main.MOD_ID, bus = Bus.MOD)
public final class CommonForge {
    public CommonForge() {
        RegistrationImpl.bootstrap();
        new NetworkHandlerImpl();
        Messages.loadMessages();

        Items.bootstrap();
        Sounds.bootstrap();
        Entities.bootstrap();
        DataLoaders.register();

        DEF_REG.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    public static final DeferredRegister<CreativeModeTab> DEF_REG = DeferredRegister.create(CREATIVE_MODE_TAB, Main.MOD_ID);

    public static final RegistryObject<CreativeModeTab> TAB = DEF_REG.register(Main.MOD_ID, () -> CreativeModeTab.builder()
            .title(ItemGroups.getDisplayName())
            .icon(ItemGroups::getIcon)
            .displayItems((featureFlags, output) -> output.acceptAll(Items.getSortedItems()))
            .build()
    );
}
