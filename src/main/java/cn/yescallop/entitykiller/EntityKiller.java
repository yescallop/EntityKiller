package cn.yescallop.entitykiller;

import cn.nukkit.entity.Entity;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.level.format.LevelProvider;
import cn.nukkit.level.format.anvil.Anvil;
import cn.nukkit.level.format.anvil.RegionLoader;
import cn.nukkit.plugin.PluginBase;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EntityKiller extends PluginBase {

    @Override
    public void onEnable() {
        this.getLogger().info("I'm killing all entities");
        this.kill();
    }

    public void kill() {
        this.getServer().getLevels().values().forEach(level -> {
            LevelProvider provider = level.getProvider();
            if (!(provider instanceof Anvil)) return;
            int count = 0;
            for (File file : new File(provider.getPath() + "region/").listFiles()) {
                Matcher m = Pattern.compile("-?\\d+").matcher(file.getName());
                int regionX, regionZ;
                try {
                    if (m.find()) {
                        regionX = Integer.parseInt(m.group());
                    } else continue;
                    if (m.find()) {
                        regionZ = Integer.parseInt(m.group());
                    } else continue;
                } catch (NumberFormatException e) {
                    continue;
                }
                try {
                    RegionLoader region = new RegionLoader(provider, regionX, regionZ);
                    for (Integer index : region.getLocationIndexes()) {
                        int chunkX = index & 0x1f;
                        int chunkZ = index >> 5;
                        FullChunk chunk = region.readChunk(chunkX, chunkZ);
                        if (chunk == null) continue;
                        chunk.initChunk();
                        int chunkCount = 0;
                        for (Entity entity : chunk.getEntities().values().stream().toArray(Entity[]::new)) {
                            entity.close();
                            chunkCount++;
                        }
                        if (chunkCount != 0) region.writeChunk(chunk);
                        count += chunkCount;
                    }
                    region.close();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            this.getLogger().info("World " + level.getName() + ": killed " + count + " entities");
        });
    }
}