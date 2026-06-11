package mono.mek.rad;

import mekanism.api.text.EnumColor;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.lib.radiation.RadiationLevelData;
import mekanism.common.lib.radiation.RadiationManager;
import mekanism.common.lib.radiation.RadiationScale;
import mekanism.common.lib.radiation.RadiationSource;
import mekanism.common.registries.MekanismAttachmentTypes;
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;


public class RadiationCleaner extends Item {
    public static final Function<Double, Double> MAGNITUDE_TRANSFORMATION = magnitude -> -Math.max(magnitude * 0.2, 1e-4);
    private static final Supplier<Integer> DISTANCE_GETTER = () -> 8;
    private static final Supplier<Integer> COOLDOWN_GETTER = () -> 20;

    public RadiationCleaner(Properties properties) {
        super(properties);
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

    public static double cleanRadiation(Level level, BlockPos pos) {
        RadiationLevelData data = level.getData(MekanismAttachmentTypes.RADIATION_LEVEL_DATA);
        double baseLineRadiation = RadiationManager.INSTANCE.baselineRadiation();
        int distance = DISTANCE_GETTER.get();

        int minX = pos.getX() - distance;
        int maxX = pos.getX() + distance;
        int minZ = pos.getZ() - distance;
        int maxZ = pos.getZ() + distance;

        int minChunkX = toChunk(minX);
        int minChunkZ = toChunk(minZ);
        int maxChunkX = toChunk(maxX);
        int maxChunkZ = toChunk(maxZ);

        MekanismRadiation.LOGGER.debug("Cleaning radiation at {} with distance {}", pos, distance);

        double removed = 0;
        for (int x = minChunkX; x <= maxChunkX; x++) {
            for (int z = minChunkZ; z <= maxChunkZ; z++) {
                Iterator<RadiationSource> radiationSourceIterator = data.getSources(x, z);
                List<RadiationSource> sourcesToProcess = new ArrayList<>();
                radiationSourceIterator.forEachRemaining(sourcesToProcess::add);

                for (RadiationSource source : sourcesToProcess) {
                    BlockPos blockPos = source.getPosition();
                    int dx = Math.abs(blockPos.getX() - pos.getX());
                    int dz = Math.abs(blockPos.getZ() - pos.getZ());

                    if (dx <= distance && dz <= distance) {
                        double sourceMagnitude = source.getMagnitude();
                        double transformation = MAGNITUDE_TRANSFORMATION.apply(sourceMagnitude);
                        double haBeenRemoved = Math.max(baseLineRadiation - sourceMagnitude, transformation);
                        removed -= haBeenRemoved;
                        source.radiate(haBeenRemoved);
                    }
                }
            }
        }
        return removed;
    }

    private static int toChunk(int coordinate) {
        return coordinate >> 4;
    }
}
