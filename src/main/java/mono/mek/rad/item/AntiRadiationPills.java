package mono.mek.rad.item;

import mekanism.api.text.EnumColor;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.lib.radiation.RadiationScale;
import mekanism.common.util.UnitDisplayUtils;
import mono.mek.rad.RadiationUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class AntiRadiationPills extends Item {
    public AntiRadiationPills() {
        super(new Item.Properties()
                .stacksTo(16)
                .food(
                        new FoodProperties.Builder()
                        .alwaysEdible()
                        .effect(
                                () -> new MobEffectInstance(MobEffects.REGENERATION, 30, 0),
                                1.0f
                        )
                        .effect(
                                () -> new MobEffectInstance(MobEffects.ABSORPTION, 30, 0),
                                1.0f
                        )
                        .build())
        );
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (entity instanceof Player player) {
            double rad = RadiationUtil.cleanEntity(player);
            System.out.println(rad);
            if (rad > 0) {
                EnumColor severityColor = RadiationScale.getSeverityColor(rad);
                Component SVHComponent = UnitDisplayUtils.getDisplayShort(rad, UnitDisplayUtils.RadiationUnit.SVH, 3);
                player.displayClientMessage(TextComponentUtil.smartTranslate("item.mekanismradiation.radiation_cleaner.clean_message_removed", severityColor, SVHComponent), true);
            }
        }
        return result;
    }
}
