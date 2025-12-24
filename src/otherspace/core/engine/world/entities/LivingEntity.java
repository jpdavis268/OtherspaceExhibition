package otherspace.core.engine.world.entities;

import org.joml.Vector2d;
import otherspace.core.engine.world.entities.components.Health;

/**
 * Defines mobile entities with a health bar, capable of taking damage and dying.
 */
public abstract class LivingEntity extends MobileEntity {
    private Health<? extends LivingEntity> health;

    public LivingEntity(Vector2d position) {
        super(position);
        health = new Health<>(this);
        this.addComponent(health);
    }

    /**
     * Get this entity's current HP.
     *
     * @return Entity's current HP.
     */
    public int getHP() {
        return health.getHP();
    }

    /**
     * Get this entity's maximum HP.
     *
     * @return Entity's maximum HP.
     */
    public int getMaxHP() {
        return health.getMaxHP();
    }


    /**
     * Set this entity's HP.
     *
     * @param HP New HP, (will be adjusted to prevent illegal values).
     */
    public void setHP(int HP) {
        health.setHP(HP);
    }

    /**
     * Set this entity's maximum HP.
     *
     * @param maxHP New maximum HP, (will be adjusted to prevent illegal values).
     */
    public void setMaxHP(int maxHP) {
        health.setMaxHP(maxHP);
    }


    /**
     * Recover this entity's HP by a set amount.
     *
     * @param amount Amount to heal, clamped between 0 and the difference between current and maximum HP.
     */
    public void heal(int amount) {
        health.heal(amount);
    }



    /**
     * Damage this entity by a set amount.
     *
     * @param amount How much damage to inflict on this enemy.
     */
    public void damage(int amount) {
        // TODO: Figure out how defense values will play into this.
        health.damage(amount);
    }

    /**
     * Kill this entity! Kill them now!
     */
    public void kill() {
        // This is default behavior, in most cases it will be overridden.
        destroy();
    }
}
