package me.koutachan.bouncy.game.boarder;

import me.koutachan.bouncy.Bouncy;
import org.bukkit.configuration.file.YamlConfiguration;

import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class WorldBorderHandler {
    @Nullable
    private WorldBorder border;

    public WorldBorder.Setting getSetting(String settingName) {
        for (WorldBorder.Setting s : getSettings()) {
            if (s.getName().equals(settingName)) {
                return s;
            }
        }
        return null;
    }

    public List<WorldBorder.Setting> getSettings() {
        List<WorldBorder.Setting> list = new ArrayList<>();

        File bordersFolder = new File(Bouncy.INSTANCE.getDataFolder(), "borders");
        if (!bordersFolder.exists()) {
            bordersFolder.mkdirs();
            return list;
        }

        File[] files = bordersFolder.listFiles();
        if (files == null) {
            return list;
        }

        for (File file : files) {
            if (file.isFile() && (file.getName().endsWith(".yml") || file.getName().endsWith(".yaml"))) {
                try {
                    YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

                    WorldBorder.Setting setting = new WorldBorder.Setting();
                    setting.load(config);

                    list.add(setting);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return list;
    }

    @Nullable
    public WorldBorder getBorder() {
        return border;
    }

    public void start(WorldBorder.Setting setting) {
        if (border != null)
            throw new IllegalStateException("already border started!");

        this.border = new WorldBorder(setting);
        this.border.start();
    }

    public void cancelBorder() {
        if (border != null) {
            try {
                border.cancel();
            } catch (IllegalStateException ignored) {
                /* Ignored */
            }
            border = null;
        }
    }
}
