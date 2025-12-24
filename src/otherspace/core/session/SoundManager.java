package otherspace.core.session;

import org.joml.Vector2d;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;
import otherspace.core.engine.Sound;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.LinkedList;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.AL11.AL_LINEAR_DISTANCE_CLAMPED;
import static org.lwjgl.openal.ALC10.*;

/**
 * Singleton responsible for managing currently playing audio sources.
 */
public class SoundManager {
    private static SoundManager singleton;

    private final HashSet<AudioSource> currentSounds = new HashSet<>();
    private long device;
    private long context;
    private boolean initialized = false;

    private float masterVolume;

    public SoundManager() {
        singleton = this;
        masterVolume = SettingsManager.get("master_volume").getAsFloat();
    }

    /**
     * Initialize the sound manager.
     *
     * @param sounds Registered sounds.
     */
    public void init(LinkedList<Sound> sounds) {
        if (initialized) {
            return;
        }

        this.device = alcOpenDevice((ByteBuffer) null);
        if (device == 0) {
            throw new IllegalStateException("ERROR: Failed to open the default OpenAL device.");
        }

        ALCCapabilities deviceCaps = ALC.createCapabilities(device);
        this.context = alcCreateContext(device, (IntBuffer) null);
        if (context == 0) {
            throw new IllegalStateException("ERROR: Failed to create OpenAL context.");
        }

        alcMakeContextCurrent(context);
        AL.createCapabilities(deviceCaps);
        alDistanceModel(AL_LINEAR_DISTANCE_CLAMPED);

        // Initialize sounds
        for (Sound s : sounds) {
            s.init();
        }

        // Audio source garbage collection.
        new Thread(() -> {
            while (true) {
                try {
                    for (AudioSource s : currentSounds) {
                        if (!s.isPlaying()) {
                            currentSounds.remove(s);
                            s.destroy();
                        }
                    }
                    Thread.sleep(1000);
                }
                catch (ConcurrentModificationException ignored) {
                    // If a concurrency issue is encountered, skip this cycle.
                }
                catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();

        initialized = true;
    }

    /**
     * Play a sound directly using the current audio settings.
     *
     * @param sound Sound to play.
     * @param loop Whether to loop sound.
     * @return Sound source ID.
     */
    public static AudioSource playSound(Sound sound, boolean loop) {
        AudioSource source = new AudioSource(sound, loop, false);
        source.play();

        return source;
    }

    /**
     * Play a sound at a localized position.
     *
     * @param position Position of audio emitter.
     * @param sound Sound to play.
     * @param loop Whether sound should loop.
     * @param falloffStart How far the listener should be from the source in game tiles before the audio starts to fade.
     * @param falloffMax How far the listener should be from the source in game tiles before the audio stops completely.
     * @param falloffFactor Speed at which the audio should drop in the falloff range (1 for default behavior).
     * @return Sound source ID.
     */
    public static AudioSource playSoundAt(Vector2d position, Sound sound, boolean loop, float pitch, float falloffStart, float falloffMax, float falloffFactor) {
        AudioSource source = new AudioSource(sound, loop, true);
        source.setPosition(position);
        source.setPitch(pitch);
        source.setFalloffStart(falloffStart);
        source.setFalloffMax(falloffMax);
        source.setFalloffFactor(falloffFactor);
        source.play();
        return source;
    }

    /**
     * Stop a currently playing sound.
     *
     * @param source Audio source to stop.
     */
    public static void stopSound(AudioSource source) {
        if (source != null) {
            source.stop();
        }
    }

    /**
     * Set the volume of a currently playing sound.
     *
     * @param source Source to adjust gain of.
     * @param gain New gain level for sound.
     */
    public static void setGain(AudioSource source, float gain) {
        if (source != null) {
            source.setGain(gain);
        }
    }

    /**
     * Get whether an audio source is still active.
     *
     * @param source Source object to check.
     * @return Whether this sound source is still active, or has been removed.
     */
    public static boolean isPlaying(AudioSource source) {
        return singleton.currentSounds.contains(source) && source.isPlaying();
    }

    /**
     * Set the master volume for all audio.
     *
     * @param masterVolume New master volume (will be clamped).
     */
    public static void setMasterVolume(float masterVolume) {
        singleton.masterVolume = Math.clamp(masterVolume, 0, 1);
    }

    /**
     * Stop every sound currently playing.
     */
    public static void stopAllAudio() {
        for (AudioSource s : singleton.currentSounds) {
            s.stop();
        }
        singleton.currentSounds.clear();
    }

    /**
     * Stores data about a currently playing sound.
     */
    public static final class AudioSource {
        private final Sound sound;
        private final int SOURCE_ID;

        private AudioSource(Sound sound, boolean loop, boolean localized) {
            this.sound = sound;

            this.SOURCE_ID = alGenSources();
            alSourcei(SOURCE_ID, AL_LOOPING, loop ? AL_TRUE : AL_FALSE);
            alSourcei(SOURCE_ID, AL_SOURCE_RELATIVE, localized ? AL_FALSE : AL_TRUE);
            alSourcei(SOURCE_ID, AL_BUFFER, sound.getSoundID());
            setGain(1);

            SoundManager.singleton.currentSounds.add(this);
        }

        /**
         * Play this sound.
         */
        private void play() {
            alSourcePlay(SOURCE_ID);
        }

        /**
         * Stop playing this sound.
         */
        private void stop() {
            alSourceStop(SOURCE_ID);
            destroy();
        }

        /**
         * Set the volume of this sound.
         *
         * @param gain New volume for sound, clamped between 0 and 1.
         */
        private void setGain(float gain) {
            gain = Math.clamp(gain, 0, 1);
            alSourcef(SOURCE_ID, AL_GAIN, gain * singleton.masterVolume * sound.getAudioGroup().getGain());
        }

        /**
         * Set the pitch of this sound.
         *
         * @param pitch New pitch for sound.
         */
        private void setPitch(float pitch) {
            alSourcef(SOURCE_ID, AL_PITCH, pitch);
        }

        /**
         * Set the position of a relative audio source.
         *
         * @param newPos New source position.
         */
        private void setPosition(Vector2d newPos) {
            alSource3f(SOURCE_ID, AL_POSITION, (float) newPos.x, (float) newPos.y, 0);
        }

        /**
         * Set the falloff distance for a relative audio source.
         *
         * @param distance Distance at which the audio source should begin to fall off.
         */
        private void setFalloffStart(float distance) {
            alSourcef(SOURCE_ID, AL_REFERENCE_DISTANCE, distance);
        }

        /**
         * Set the maximum falloff distance for a relative audio source.
         *
         * @param distance Distance at which the audio source should become inaudible.
         */
        private void setFalloffMax(float distance) {
            alSourcef(SOURCE_ID, AL_MAX_DISTANCE, distance);
        }

        /**
         * Set how fast a relative audio source falls off in the fall off range.
         *
         * @param factor How fast the audio source should fall off.
         */
        private void setFalloffFactor(float factor) {
            alSourcef(SOURCE_ID, AL_ROLLOFF_FACTOR, factor);
        }

        /**
         * Get whether the sound source is playing something.
         *
         * @return Whether source is playing something.
         */
        private boolean isPlaying() {
            return alGetSourcei(SOURCE_ID, AL_SOURCE_STATE) == AL_PLAYING;
        }

        /**
         * Free this sound source.
         */
        private void destroy() {
            alDeleteSources(new int[] {SOURCE_ID});
        }
    }
}
