package cat.nyaa.musicapi.player;

import cat.nyaa.musicapi.api.*;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;

public class MusicPlayer implements MusicApi {
    public MusicPlayer() {

    }

    public void submit(IMusicSheet sheet) {

    }

    public IMusicTask play(IMusicSheet sheet, Location location) {
        PlayTask playTask = new PlayTask(sheet, sheet.getLength(), location);
        return playTask;
    }


    public IMusicTask play(IMusicSheet sheet, Entity entity) {
        PlayTask playTask = new PlayTask(sheet, sheet.getLength(), entity);
        return playTask;
    }

    @Override
    public IMusicTask startPlaying(IMusicSheet musicSheet, Location location) {
        return play(musicSheet, location);
    }

    @Override
    public IMusicTask startPlaying(IMusicSheet musicSheet, Entity entity) {
        return play(musicSheet, entity);
    }

    public static class PlayTask extends BukkitRunnable implements IMusicTask{
        private final IMusicSheet iMusicSheet;
        private int finishTick;
        private int currentTick = 0;
        private Location location;
        private Entity entity;
        private boolean stopped = false;

        public PlayTask(IMusicSheet iMusicSheet, int finishTick, Location location) {
            this.iMusicSheet = iMusicSheet;
            this.finishTick = finishTick;
            this.location = location;
        }

        public PlayTask(IMusicSheet iMusicSheet, int finishTick, Entity entity) {
            this.iMusicSheet = iMusicSheet;
            this.finishTick = finishTick;
            this.entity = entity;
        }

        @Override
        public void run() {
            try {
                if (stopped){
                    this.cancel();
                    return;
                }
                Collection<ISheetRecord> notesForTick = iMusicSheet.getNotesForTick(currentTick);
                Location sourceLocation;
                if (location == null){
                    sourceLocation = entity.getLocation();
                }else {
                    sourceLocation = location;
                }
                World world = sourceLocation.getWorld();
                notesForTick.forEach(sheetRecord -> {
                    INote note = sheetRecord.getNote();

                    if (world!=null){
                        world.playSound(sourceLocation, note.getSound(), note.getVolume(), note.getPitch());
                    }
                });
                currentTick++;
                if (currentTick > finishTick) {
                    this.cancel();
                }
            } catch (Exception e) {
                e.printStackTrace();
                this.cancel();
            }
        }

        @Override
        public void play(JavaPlugin plugin) {
            this.runTaskTimer(plugin, 0, 1);
        }
    }
}
