package otherspace.core.engine;

public class AudioGroup {
    private float gain;

    public AudioGroup(float initialGain) {
        gain = Math.clamp(initialGain, 0, 1);
    }

    /**
     * Get the gain of this audio group.
     *
     * @return Gain for this audio group.
     */
    public float getGain() {
        return gain;
    }

    /**
     * Set the gain of this audio group.
     *
     * @param gain New gain for group (will be clamped between 0 and 1).
     */
    public void setGain(float gain) {
        this.gain = Math.clamp(gain, 0, 1);
    }
}
