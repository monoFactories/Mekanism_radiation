package mono.mek.rad.item;

import mekanism.api.text.EnumColor;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.lib.radiation.RadiationScale;
import mekanism.common.util.UnitDisplayUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

import static mono.mek.rad.RadiationUtil.DISTANCE_GETTER;
import static mono.mek.rad.RadiationUtil.cleanRadiation;


public class RadiationCleaner extends Item {
    private static final Supplier<Integer> COOLDOWN_GETTER = () -> 20;
    public RadiationCleaner() {
        super(new Item.Properties().stacksTo(16));
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        if (level.isClientSide) {
            return InteractionResult.PASS;
        }
        ItemStack stack = context.getItemInHand();
        BlockPos blockPos = context.getClickedPos();
        double removed = cleanRadiation(level, blockPos);
        boolean hasRemoved = removed > 0;
        if (hasRemoved) stack.shrink(1);
        if (player != null) {
            player.getCooldowns().addCooldown(this, COOLDOWN_GETTER.get());
            if (hasRemoved) {
                EnumColor severityColor = RadiationScale.getSeverityColor(removed);
                Component SVHComponent = UnitDisplayUtils.getDisplayShort(removed, UnitDisplayUtils.RadiationUnit.SVH, 3);
                player.displayClientMessage(TextComponentUtil.smartTranslate("item.mekanismradiation.radiation_cleaner.clean_message_removed", severityColor, SVHComponent), true);
            }
            else
                player.displayClientMessage(Component.translatable("item.mekanismradiation.radiation_cleaner.clean_message_no_radiation", DISTANCE_GETTER.get()), true);
        }
        return InteractionResult.CONSUME;
    }

}
