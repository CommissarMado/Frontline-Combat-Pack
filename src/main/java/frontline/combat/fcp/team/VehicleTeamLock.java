package frontline.combat.fcp.team;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.scores.Team;
import net.minecraftforge.registries.ForgeRegistries;


public final class VehicleTeamLock {

    public static final String TEAM_KEY = "fcp_team";

    private VehicleTeamLock() {
    }

    public static String getTeam(VehicleEntity vehicle) {
        CompoundTag data = vehicle.getPersistentData();
        if (data.contains(TEAM_KEY, Tag.TAG_STRING)) {
            String s = data.getString(TEAM_KEY);
            return s.isEmpty() ? null : s;
        }
        return null;
    }

    public static void setTeam(VehicleEntity vehicle, String teamName) {
        vehicle.getPersistentData().putString(TEAM_KEY, teamName);
    }

    public static void clearTeam(VehicleEntity vehicle) {
        vehicle.getPersistentData().remove(TEAM_KEY);
    }

    public static String teamNameOf(Entity entity) {
        Team team = entity.getTeam();
        return team == null ? null : team.getName();
    }

    public static boolean isFcpVehicle(VehicleEntity vehicle) {
        ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey(vehicle.getType());
        return key != null && key.getNamespace().equals("fcp");
    }
}