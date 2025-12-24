package otherspace.core.engine;

import org.joml.Vector2d;
import otherspace.game.entities.Player;

import static org.lwjgl.openal.AL10.*;

/**
 * Maintains the current position of the audio listener.
 */
public class SoundListener {
    public SoundListener(Player player) {
        setPosition(player.position);

        // These will never be modified again outside of this constructor,
        // position changes should be handled using setPosition.
        alListener3f(AL_VELOCITY, 0, 0, 0);
        alListenerfv(AL_ORIENTATION, new float[]{0, 0, 1, 0, -1, 0});
    }

    /**
     * Set the position of the audio listener.
     *
     * @param position New audio listener position.
     */
    public void setPosition(Vector2d position) {
        // Was considering using a floating origin for this,
        // but I don't think the loss of precision would make a noticeable difference even at the world's edge.
        alListener3f(AL_POSITION, (float) position.x, (float) position.y, 0);
    }
}
