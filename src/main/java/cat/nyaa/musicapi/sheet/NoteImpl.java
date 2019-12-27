package cat.nyaa.musicapi.sheet;

import cat.nyaa.musicapi.api.INote;
import org.bukkit.Sound;

public class NoteImpl implements INote {
    private final Sound sound;
    private final float volume;
    private final float pitch;

    public NoteImpl(Sound sound, float volume, float pitch){
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
    }

    @Override
    public float getVolume() {
        return volume;
    }

    @Override
    public float getPitch() {
        return pitch;
    }

    @Override
    public Sound getSound() {
        return sound;
    }
}
