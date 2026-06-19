package mono.mek.rad;

import mekanism.api.radiation.capability.IRadiationEntity;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.lib.radiation.RadiationLevelData;
import mekanism.common.lib.radiation.RadiationManager;
import mekanism.common.lib.radiation.RadiationSource;
import mekanism.common.registries.MekanismAttachmentTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class RadiationUtil {
    public static final Supplier<Integer> DISTANCE_GETTER = () -> 8;
    public static final Function<Double, Double> SOURCE_MAGNITUDE_TRANSFORMATION = magnitude -> -Math.max(magnitude * 0.2, 1e-4);
    public static final Function<Double, Double> ENTITY_MAGNITUDE_TRANSFORMATION = magnitude -> -Math.max(magnitude * 0.4, 1e-4);

    public static double cleanRadiation(Level level, BlockPos pos) {
        if (!RadiationManager.isGlobalRadiationEnabled()) return 0.0;
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
                        double transformation = SOURCE_MAGNITUDE_TRANSFORMATION.apply(sourceMagnitude);
                        double haBeenRemoved = Math.max(baseLineRadiation - sourceMagnitude, transformation);
                        removed -= haBeenRemoved;
                        source.radiate(haBeenRemoved);
                    }
                }
            }
        }
        return removed;
    }

    public static double cleanEntity(Entity entity) {
        if (RadiationManager.isGlobalRadiationEnabled()) {
            double baseLineRadiation = RadiationManager.INSTANCE.baselineRadiation();
            IRadiationEntity radiationCap = (IRadiationEntity) entity.getCapability(Capabilities.RADIATION_ENTITY);
            if (radiationCap != null) {
                double sourceMagnitude = radiationCap.getRadiation();
                double transRemoved = ENTITY_MAGNITUDE_TRANSFORMATION.apply(sourceMagnitude);
                double hasBeenRemoved = Math.max(baseLineRadiation - sourceMagnitude, transRemoved);
                radiationCap.set(sourceMagnitude + hasBeenRemoved);
                return -hasBeenRemoved;
            }
        }
        return 0.0;
    }

    private static int toChunk(int coordinate) {
        return coordinate >> 4;
    }
}
