package otherspace.core.engine.world.entities.components;

import com.google.gson.JsonObject;
import otherspace.core.engine.world.entities.LivingEntity;

/**
 * Defines a "health" component for an entity, giving them an HP pool and allowing them to take damage and die.
 * @param <E>
 */
public class Health<E extends LivingEntity> extends EntityComponent<E> {
    public static final int INVINCIBLE = -1;

    int HP;
    int maxHP;

    public Health(E parent) {
        super(parent);

        HP = 100;
        maxHP = 100;
    }


    /**
     * Get this entity's current HP.
     *
     * @return Entity's current HP.
     */
    public int getHP() {
        return HP;
    }

    /**
     * Get this entity's maximum HP.
     *
     * @return Entity's maximum HP.
     */
    public int getMaxHP() {
        return maxHP;
    }


    /**
     * Set this entity's HP.
     *
     * @param HP New HP, (will be adjusted to prevent illegal values).
     */
    public void setHP(int HP) {
        this.HP = Math.clamp(HP, 0, maxHP);
        if (HP <= 0) {
            getParent().kill();
        }
    }

    /**
     * Set this entity's maximum HP.
     *
     * @param maxHP New maximum HP, (will be adjusted to prevent illegal values).
     */
    public void setMaxHP(int maxHP) {
        if (maxHP < 0) {
            this.maxHP = INVINCIBLE;
        }
        else {
            this.maxHP = Math.max(1, maxHP);
        }
    }


    /**
     * Recover this entity's HP by a set amount.
     *
     * @param amount Amount to heal, clamped between 0 and the difference between current and maximum HP.
     */
    public void heal(int amount) {
        HP += Math.clamp(amount, 0, maxHP - HP);
    }

    /**
     * Damage this entity by a set amount.
     *
     * @param amount How much damage to inflict on this enemy.
     */
    public void damage(int amount) {
        // TODO: Figure out how defense values will play into this.
        HP -= Math.clamp(amount, 0, HP);

        if (HP <= 0) {
            getParent().kill();
        }
    }

    @Override
    public JsonObject serialize() {
        JsonObject data = new JsonObject();
        data.addProperty("HP", HP);
        data.addProperty("maxHP", maxHP);
        return data;
    }

    @Override
    public void deserialize(JsonObject json) {
        setHP(json.get("HP").getAsInt());
        setMaxHP(json.get("maxHP").getAsInt());
    }
}
