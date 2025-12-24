package otherspace.core.engine;

import org.lwjgl.stb.STBVorbisInfo;
import org.lwjgl.system.MemoryUtil;

import java.nio.ShortBuffer;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.stb.STBVorbis.*;

public class Sound {
    public final String srcPath;
    private final AudioGroup audioGroup;
    private int soundID;

    public Sound(String srcPath, AudioGroup audioGroup) {
        this.srcPath = srcPath;
        this.audioGroup = audioGroup;
    }

    /**
     * Initialize this sound.
     */
    public void init() {
        soundID = alGenBuffers();

        // Load file data
        try (STBVorbisInfo info = STBVorbisInfo.malloc()) {
            ShortBuffer pcm = readOGG(srcPath, info);

            alBufferData(soundID, info.channels() == 1 ? AL_FORMAT_MONO16 : AL_FORMAT_STEREO16, pcm, info.sample_rate());
        }
    }

    /**
     * Convert an OGG file to buffer data.
     *
     * @return Buffer containing audio data.
     */
    private ShortBuffer readOGG(String path, STBVorbisInfo info) {
        int[] error = new int[1];

        long decoder = stb_vorbis_open_filename(path, error, null);
        if (decoder == 0) {
            System.out.println(error[0]);
            throw new RuntimeException("ERROR: Failed to load OGG file at " + path);
        }

        stb_vorbis_get_info(decoder, info);

        int channels = info.channels();

        int lengthSamples = stb_vorbis_stream_length_in_samples(decoder);

        ShortBuffer result = MemoryUtil.memAllocShort(lengthSamples * channels);

        result.limit(stb_vorbis_get_samples_short_interleaved(decoder, channels, result) * channels);
        stb_vorbis_close(decoder);

        return result;
    }

    /**
     * Get the ID of this sound's audio buffer.
     *
     * @return Audio Buffer ID.
     */
    public int getSoundID() {
        return soundID;
    }

    /**
     * Get the audio group this sound is part of.
     *
     * @return Sound's audio group.
     */
    public AudioGroup getAudioGroup() {
        return audioGroup;
    }
}
