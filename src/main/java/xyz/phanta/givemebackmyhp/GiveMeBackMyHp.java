package xyz.phanta.givemebackmyhp;

import gnu.trove.impl.Constants;
import gnu.trove.map.TObjectFloatMap;
import gnu.trove.map.hash.TObjectFloatHashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.logging.log4j.Logger;

import java.util.Deque;
import java.util.LinkedList;
import java.util.UUID;

@Mod(modid = GiveMeBackMyHp.MOD_ID, version = GiveMeBackMyHp.VERSION, useMetadata = true)
public class GiveMeBackMyHp {

    public static final String MOD_ID = "givemebackmyhp";
    public static final String VERSION = "1.0.0";

    @Mod.Instance(MOD_ID)
    public static GiveMeBackMyHp INSTANCE;

    @SuppressWarnings("NotNullFieldNotInitialized")
    private Logger logger;
    private final Deque<EntityPlayer> healthCheckQueue = new LinkedList<>();
    private final TObjectFloatMap<UUID> healthCache = new TObjectFloatHashMap<>(
            Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, -1F);

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.player.world.isRemote) {
            healthCheckQueue.offer(event.player);
        }
    }

    public void onPlayerDeserialize(EntityPlayerMP player, NBTTagCompound tag) {
        if (tag.hasKey("Health", net.minecraftforge.common.util.Constants.NBT.TAG_FLOAT)) {
            float actualHealth = tag.getFloat("Health");
            if (actualHealth >= 0F) {
                healthCache.put(player.getUniqueID(), actualHealth);
            } else {
                logger.warn("Player {} had negative health value {} serialized???", player.getName(), actualHealth);
            }
        } else {
            logger.warn("Player {} had no health data serialized???", player.getName());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            while (!healthCheckQueue.isEmpty()) {
                EntityPlayer player = healthCheckQueue.poll();
                float actualHealth = healthCache.get(player.getUniqueID());
                if (actualHealth >= 0F) {
                    if (actualHealth > player.getHealth()) {
                        logger.info("Health disparity detected: player {} has {} HP but should have {}",
                                player.getName(), player.getHealth(), actualHealth);
                        player.setHealth(Math.min(actualHealth, player.getMaxHealth()));
                    }
                } else {
                    logger.warn("Player {} was queued for a health disparity check, but had no cached health data!",
                            player.getName());
                }
            }
            if (!healthCache.isEmpty()) {
                healthCache.clear();
            }
        }
    }

}
