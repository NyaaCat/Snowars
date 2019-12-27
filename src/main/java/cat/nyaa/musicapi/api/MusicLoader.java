package cat.nyaa.musicapi.api;

import cat.nyaa.musicapi.sheet.MusicSheetImpl;
import cat.nyaa.musicapi.sheet.NoteImpl;
import cat.nyaa.musicapi.sheet.SheetRecord;
import org.bukkit.Sound;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class MusicLoader {
    public static IMusicSheet loadFromFile(File file) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        String string;
        MusicSheetImpl.Builder builder = new MusicSheetImpl.Builder();
        while ((string = bufferedReader.readLine()) != null) {
            try {
                String[] split = string.split("\t");
                String s = split[0];
                String s1 = split[1];
                String s2 = split[2];
                String s3 = split[3];
                int tick = Integer.parseInt(s);
                s1 = s1.toUpperCase();
                Sound sound = Sound.valueOf("BLOCK_NOTE_BLOCK_" + s1);
                float volume = Float.parseFloat(s2) * 0.8f;
                float pitch = Float.parseFloat(s3);
                NoteImpl note = new NoteImpl(sound, volume, pitch);
                SheetRecord record = new SheetRecord(tick, note);
                builder.add(record);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        MusicSheetImpl build = builder.build();
        build.compile();
        return build;
    }
}
