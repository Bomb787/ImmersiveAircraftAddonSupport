package immersive_aircraft.entity;

import immersive_aircraft.item.upgrade.VehicleStat;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * Implements airplane like physics properties and accelerated towards
 */
public abstract class AirplaneEntity extends AircraftEntity {

    protected double aoa;

    public AirplaneEntity(EntityType<? extends AircraftEntity> entityType, Level world, boolean canExplodeOnCrash) {
        super(entityType, world, canExplodeOnCrash);
    }

    @Override
    protected boolean useAirplaneControls() {
        return true;
    }

    @Override
    protected float getGravity() {
        Vector3f direction = getForwardDirection();
        float speed = (float) getDeltaMovement().length() * (1.0f - Math.abs(direction.y));
        return Math.max(0.0f, 1.0f - speed * 1.5f) * super.getGravity();
    }

    protected float getBrakeFactor() {
        return 0.95f;
    }

    @Override
    protected void convertPower(Vec3 direction) {
        Vec3 velocity = getDeltaMovement();
        double drag = Math.abs(direction.dot(velocity.normalize()));
        setDeltaMovement(velocity.normalize()
                .lerp(direction, getProperties().get(VehicleStat.LIFT))
                .scale(velocity.length() * (drag * getProperties().get(VehicleStat.FRICTION) + (1.0 - getProperties().get(VehicleStat.FRICTION)))));
    }

    @Override
    protected void updateVelocity() {
        // get direction
        Vector3f direction = getForwardDirection();

        // glide
        float diff = (float) (lastY - getY());
        if (lastY != 0.0 && getProperties().get(VehicleStat.GLIDE_FACTOR) > 0 && diff != 0.0) {
            setDeltaMovement(getDeltaMovement().add(toVec3d(direction).scale(diff * getProperties().get(VehicleStat.GLIDE_FACTOR) * (1.0f - Math.abs(direction.y)))));
        }
        lastY = (float) getY();

        // convert power
        convertPower(toVec3d(direction));

        // friction
        applyFriction();

        if (onGround()) {
            // Landing
            setXRot((getXRot() + getProperties().get(VehicleStat.GROUND_PITCH)) * 0.9f - getProperties().get(VehicleStat.GROUND_PITCH));
        } else {
            // Wind
            Vector3f effect = getWindEffect();
            setXRot(getXRot() + effect.x);
            setYRot(getYRot() + effect.z);

            float offsetStrength = 0.005f;
            setDeltaMovement(getDeltaMovement().add(effect.x * offsetStrength, 0.0f, effect.z * offsetStrength));
        }
    }

    @Override
    protected void updateController() {
        if (!isVehicle()) {
            return;
        }

        super.updateController();

        this.aoa = -this.getXRot() - (Mth.RAD_TO_DEG * Math.asin(this.getDeltaMovement().y() / this.getDeltaMovement().length()));

        // engine control
        if (movementY != 0) {
            setEngineTarget(Math.max(0.0f, Math.min(1.0f, getEngineTarget() + 0.1f * movementY)));
            if (movementY < 0) {
                setDeltaMovement(getDeltaMovement().scale(getBrakeFactor()));
            }
        }

        // get direction
        Vector3f direction = getForwardDirection();

        /*double a = -this.getXRot();
        double b = Mth.RAD_TO_DEG * Math.asin(this.getDeltaMovement().y() / this.getDeltaMovement().length());
        System.out.println("pitch: " + a);
        System.out.println("movement: " + b);
        System.out.println("aoa: " + (a - b));*/

        // speed
        float thrust = (float) (Math.pow(getEnginePower(), 2.0) * getProperties().get(VehicleStat.ENGINE_SPEED));
        if (onGround() && getEngineTarget() < 1.0) {
            thrust = getProperties().get(VehicleStat.PUSH_SPEED) / (1.0f + (float) getDeltaMovement().length() * 5.0f) * pressingInterpolatedZ.getSmooth() * (1.0f - getEnginePower());
        }

        // accelerate
        setDeltaMovement(getDeltaMovement().add(toVec3d(direction.mul(thrust))));
    }
}
