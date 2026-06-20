package com.zombiesdots;

import com.zombiesdots.command.*;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.io.File;

@Mod(modid = ZombiesDotsMod.MODID, version = ZombiesDotsMod.VERSION,
     name = ZombiesDotsMod.NAME, clientSideOnly = true,
     acceptedMinecraftVersions = "[1.8.9]")
public class ZombiesDotsMod {

    public static final String MODID   = "zombiesdots";
    public static final String VERSION = "1.0.0";
    public static final String NAME    = "Zombies Dots";

    @Mod.Instance(MODID)
    public static ZombiesDotsMod instance;

    public static ProfileManager profileManager;

    public static GuiScreen pendingGui = null;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        File configDir = new File(event.getModConfigurationDirectory(), "zombies-dots");
        profileManager = new ProfileManager(configDir);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new RenderHandler());

        ClientCommandHandler.instance.registerCommand(new CommandAddDot());
        ClientCommandHandler.instance.registerCommand(new CommandRemoveDot());
        ClientCommandHandler.instance.registerCommand(new CommandProfile());
        ClientCommandHandler.instance.registerCommand(new CommandProfiles());
        ClientCommandHandler.instance.registerCommand(new CommandMarkerGUI());
        ClientCommandHandler.instance.registerCommand(new CommandDotHelp());
        ClientCommandHandler.instance.registerCommand(new CommandDotColor());
    }
}
