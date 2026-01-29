package com.muzlik;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import com.muzlik.cooldown.CooldownManager;

// Fragment System imports
import com.muzlik.fragment.FragmentManager;
import com.muzlik.fragment.FragmentRegistry;
import com.muzlik.fragment.level.LevelManager;
import com.muzlik.fragment.rank.RankManager;
import com.muzlik.fragment.ability.AbilityRegistry;
import com.muzlik.fragment.ability.CooldownAPI;
import com.muzlik.mana.ManaManager;
import com.muzlik.mana.ManaFlaskListener;
import com.muzlik.ritual.RitualManager;
import com.muzlik.fx.FXLibrary;
import com.muzlik.damage.DamageAPI;
import com.muzlik.recipe.RecipeManager;
import com.muzlik.ui.UIManager;
import com.muzlik.config.ConfigManager;
import com.muzlik.data.DataPersistence;
import com.muzlik.vfx.ActiveEffectRegistry;
import com.muzlik.vfx.DamageAttributionManager;
import com.muzlik.vfx.VFXEngine;
import com.muzlik.vfx.SoundEngine;
import com.muzlik.vfx.environment.EnvironmentManager;
import com.muzlik.vfx.AbilityVFXHelper;
import com.muzlik.character.CharacterLevelManager;

/**
 * Main plugin class for FrostSMP Power Plugin
 * Entry point for all plugin functionality
 */
public class FrostSMPPlugin extends JavaPlugin implements Listener {
    
    // Cooldown Manager (replaces legacy PowerManager)
    private CooldownManager cooldownManager;
    
    // Fragment HUD
    private com.muzlik.ui.FragmentActionBarHUD fragmentHUD;
    
    // Fragment System managers
    private ConfigManager configManager;
    private ManaManager manaManager;
    private LevelManager levelManager;
    private RankManager rankManager;
    private FragmentManager fragmentManager;
    private AbilityRegistry abilityRegistry;
    private CooldownAPI cooldownAPI;
    private FXLibrary fxLibrary;
    private RitualManager ritualManager;
    private DamageAPI damageAPI;
    private RecipeManager recipeManager;
    private UIManager uiManager;
    private DataPersistence dataPersistence;
    private FragmentRegistry fragmentRegistry;
    
    // VFX System
    private ActiveEffectRegistry effectRegistry;
    private DamageAttributionManager damageAttributionManager;
    private VFXEngine vfxEngine;
    private SoundEngine soundEngine;
    private EnvironmentManager environmentManager;
    private AbilityVFXHelper abilityVFXHelper;
    private com.muzlik.vfx.VFXPerformanceManager vfxPerformanceManager;
    private com.muzlik.vfx.cinematic.CinematicVFXEngine cinematicVFXEngine;
    private com.muzlik.vfx.cinematic.miniblock.MiniBlockManager miniBlockManager;
    
    // Block Manipulation System
    private com.muzlik.block.BlockManipulationEngine blockManipulationEngine;
    
    // Flight System
    private com.muzlik.fragment.ability.FlightManager flightManager;
    
    // Ability Slot System
    private com.muzlik.fragment.ability.AbilitySlotManager abilitySlotManager;
    
    // Character Level System (affects max mana)
    private CharacterLevelManager characterLevelManager;
    
    // Player Preferences System (control schemes, ability toggle)
    private com.muzlik.player.PlayerPreferencesManager preferencesManager;
    
    // Luck Fragment Passive System
    private com.muzlik.fragment.ability.executors.luck.LuckFragmentPassiveManager luckPassiveManager;
    
    // Health Monitoring System (Task 17)
    private com.muzlik.monitoring.HealthMonitor healthMonitor;

    @Override
    public void onEnable() {
        // Beautiful startup banner with ANSI colors
        String CYAN = "\u001B[36m";
        String WHITE = "\u001B[37m";
        String YELLOW = "\u001B[33m";
        String RESET = "\u001B[0m";
        String BOLD = "\u001B[1m";
        
        System.out.println(CYAN + "╔════════════════════════════════════════════════════════════════╗" + RESET);
        System.out.println(CYAN + "║" + RESET + "                                                                " + CYAN + "║" + RESET);
        System.out.println(CYAN + "║" + WHITE + BOLD + "   ███████╗██████╗  ██████╗ ███████╗████████╗███╗   ███╗ ██████╗" + RESET + CYAN + "║" + RESET);
        System.out.println(CYAN + "║" + WHITE + BOLD + "   ██╔════╝██╔══██╗██╔═══██╗██╔════╝╚══██╔══╝████╗ ████║██╔════╝" + RESET + CYAN + "║" + RESET);
        System.out.println(CYAN + "║" + WHITE + BOLD + "   █████╗  ██████╔╝██║   ██║███████╗   ██║   ██╔████╔██║██║     " + RESET + CYAN + "║" + RESET);
        System.out.println(CYAN + "║" + WHITE + BOLD + "   ██╔══╝  ██╔══██╗██║   ██║╚════██║   ██║   ██║╚██╔╝██║██║     " + RESET + CYAN + "║" + RESET);
        System.out.println(CYAN + "║" + WHITE + BOLD + "   ██║     ██║  ██║╚██████╔╝███████║   ██║   ██║ ╚═╝ ██║╚██████╗" + RESET + CYAN + "║" + RESET);
        System.out.println(CYAN + "║" + WHITE + BOLD + "   ╚═╝     ╚═╝  ╚═╝ ╚═════╝ ╚══════╝   ╚═╝   ╚═╝     ╚═╝ ╚═════╝" + RESET + CYAN + "║" + RESET);
        System.out.println(CYAN + "║" + RESET + "                                                                "  + CYAN + "║" + RESET);
        System.out.println(CYAN + "║" + RESET + "              " + YELLOW + BOLD + "Fragment Power System v1.0.0 "   + RESET + "                     " + CYAN + "║" + RESET);
        System.out.println(CYAN + "║" + RESET + "                                                                "  + CYAN + "║" + RESET);
        System.out.println(CYAN + "║" + RESET + "  " + WHITE + "Author: " + BOLD + "muzlik-gm" + RESET +  "                                             " + CYAN + "║" + RESET);
        System.out.println(CYAN + "║" + RESET + "  " + WHITE + "Platform: Paper/Spigot 1.20.4+" + RESET +  "                               " + CYAN + "║" + RESET);
        System.out.println(CYAN + "║" + RESET + "  " + WHITE + "Features: 10 Fragments | 40+ Abilities | VFX Engine " + RESET + "          " + CYAN + "║" + RESET);
        System.out.println(CYAN + "║" + RESET + "                                                                " + CYAN + "║" + RESET);
        System.out.println(CYAN + "╚════════════════════════════════════════════════════════════════╝" + RESET);
        System.out.println("");
        getLogger().info("▶ Initializing systems...");

        // Initialize configuration
        configManager = new ConfigManager(this);
        
        // Initialize VFX system
        effectRegistry = new ActiveEffectRegistry(this);
        effectRegistry.startCleanupTask();
        
        damageAttributionManager = new DamageAttributionManager(this);
        damageAttributionManager.startCleanupTask();
        
        vfxPerformanceManager = new com.muzlik.vfx.VFXPerformanceManager(this);
        vfxPerformanceManager.startMonitoring();
        
        vfxEngine = new VFXEngine(this, effectRegistry);
        vfxEngine.startUpdateTask();
        
        soundEngine = new SoundEngine(this);
        soundEngine.startUpdateTask();
        
        environmentManager = new EnvironmentManager(this, effectRegistry);
        environmentManager.startRevertTask();
        
        // Initialize cinematic VFX system
        cinematicVFXEngine = new com.muzlik.vfx.cinematic.CinematicVFXEngine(this, effectRegistry);
        miniBlockManager = cinematicVFXEngine.getMiniBlockManager();
        
        // Initialize block manipulation system
        blockManipulationEngine = new com.muzlik.block.BlockManipulationEngine(this, effectRegistry, damageAttributionManager);
        blockManipulationEngine.registerCleanupListener();
        blockManipulationEngine.startUpdateTask();
        
        abilityVFXHelper = new AbilityVFXHelper(this, vfxEngine, soundEngine);
        
        // Initialize core managers
        manaManager = new ManaManager(this);
        
        // Set mana system enabled state from config
        boolean manaSystemEnabled = configManager.isManaSystemEnabled();
        manaManager.setManaSystemEnabled(manaSystemEnabled);
        getLogger().info("Mana System: " + (manaSystemEnabled ? "ENABLED" : "DISABLED"));
        
        levelManager = new LevelManager(this);
        rankManager = new RankManager(this);
        
        // Set cross-references for auto rank-up
        levelManager.setRankManager(rankManager);
        
        // Initialize Character Level system (affects max mana)
        characterLevelManager = new CharacterLevelManager(this);
        manaManager.setCharacterLevelManager(characterLevelManager);
        
        // Initialize Player Preferences system (control schemes, ability toggle)
        preferencesManager = new com.muzlik.player.PlayerPreferencesManager(this);
        
        // Initialize Fragment manager
        fragmentManager = new FragmentManager(this, manaManager, levelManager, rankManager);
        
        // Initialize ability systems
        abilityRegistry = new AbilityRegistry(this);
        cooldownAPI = new CooldownAPI();
        
        // Initialize flight system
        flightManager = new com.muzlik.fragment.ability.FlightManager(this);
        
        // Initialize Ability Slot system (Task 2)
        abilitySlotManager = new com.muzlik.fragment.ability.AbilitySlotManager(this, dataPersistence);
        
        // Initialize Luck Fragment passive system
        luckPassiveManager = new com.muzlik.fragment.ability.executors.luck.LuckFragmentPassiveManager(this, fragmentManager);
        luckPassiveManager.start();
        
        // Initialize FX Library
        fxLibrary = new FXLibrary(this);
        fxLibrary.setDebugMode(configManager.isFXDebugMode());
        fxLibrary.setParticleDensityMultiplier(configManager.getParticleDensity());
        
        // Initialize CooldownManager (replaces legacy PowerManager)
        cooldownManager = new CooldownManager();
        
        // Initialize ritual system
        ritualManager = new RitualManager(this, fragmentManager, fxLibrary, configManager);
        ritualManager.setProximityDistance(configManager.getFragmentCreationProximity());
        ritualManager.setAbilitySlotManager(abilitySlotManager);
        
        // Initialize damage API
        damageAPI = new DamageAPI(this);
        
        // Initialize recipe manager
        recipeManager = new RecipeManager(this);
        recipeManager.registerRecipes();
        
        // Initialize UI manager (with CooldownManager)
        uiManager = new UIManager(this, fragmentManager, manaManager, levelManager, rankManager,
                cooldownManager, configManager);
        
        // Initialize GUI systems (after preferencesManager is available)
        uiManager.initializeGUIs(preferencesManager);
        
        // Initialize data persistence
        dataPersistence = new DataPersistence(this);
        
        // Register all 10 Fragments
        fragmentRegistry = new FragmentRegistry(this, fragmentManager, fxLibrary);
        fragmentRegistry.registerAllFragments();

        // Initialize Fragment HUD (with CharacterLevelManager)
        fragmentHUD = new com.muzlik.ui.FragmentActionBarHUD(this, fragmentManager, manaManager, cooldownManager);
        fragmentHUD.setCharacterLevelManager(characterLevelManager);
        fragmentHUD.start();

        // Register listeners
        
        // Create and register FragmentAbilityListener with LevelManager and PreferencesManager
        com.muzlik.listener.FragmentAbilityListener fragmentAbilityListener = 
            new com.muzlik.listener.FragmentAbilityListener(this, fragmentManager, manaManager, cooldownManager, preferencesManager);
        fragmentAbilityListener.setLevelManager(levelManager);
        getServer().getPluginManager().registerEvents(
                fragmentAbilityListener,
                this
        );
        getServer().getPluginManager().registerEvents(
                new ManaFlaskListener(this, manaManager, recipeManager),
                this
        );
        getServer().getPluginManager().registerEvents(
                new com.muzlik.ritual.RitualListener(this, ritualManager, recipeManager),
                this
        );
        getServer().getPluginManager().registerEvents(
                ritualManager.getStructureProtectionListener(),
                this
        );
        getServer().getPluginManager().registerEvents(
                new com.muzlik.listener.FirstJoinListener(this, fragmentManager),
                this
        );
        getServer().getPluginManager().registerEvents(
                new com.muzlik.ui.FragmentGUIListener(this, fragmentManager, uiManager),
                this
        );
        getServer().getPluginManager().registerEvents(
                new com.muzlik.listener.RecipeGUIListener(),
                this
        );
        // Instantiate and register PlayerDataListener
        com.muzlik.listener.PlayerDataListener playerDataListener = new com.muzlik.listener.PlayerDataListener(
            this, dataPersistence, fragmentManager, manaManager, levelManager, rankManager
        );
        getServer().getPluginManager().registerEvents(playerDataListener, this);
        
        // Load data for currently online players (for reloads)
        for (org.bukkit.entity.Player player : getServer().getOnlinePlayers()) {
            playerDataListener.loadPlayerData(player);
        }
        getServer().getPluginManager().registerEvents(
                new com.muzlik.listener.EffectCleanupListener(this, effectRegistry),
                this
        );
        getServer().getPluginManager().registerEvents(damageAttributionManager, this);
        getServer().getPluginManager().registerEvents(
                new com.muzlik.listener.XPGainListener(this, fragmentManager, levelManager),
                this
        );
        getServer().getPluginManager().registerEvents(
                new com.muzlik.listener.SummonProtectionListener(),
                this
        );
        getServer().getPluginManager().registerEvents(
                new com.muzlik.listener.PassiveAbilityListener(this, fragmentManager, rankManager, manaManager, cooldownManager),
                this
        );
        getServer().getPluginManager().registerEvents(
                new com.muzlik.listener.FragmentPassiveListener(this, fragmentManager),
                this
        );
        getServer().getPluginManager().registerEvents(
                new com.muzlik.listener.FlightControlListener(flightManager),
                this
        );
        getServer().getPluginManager().registerEvents(
                new com.muzlik.listener.FragmentChangerListener(uiManager),
                this
        );
        getServer().getPluginManager().registerEvents(
                new com.muzlik.fragment.ability.executors.dragon.DragonicFuryExecutor.FireballImpactListener(),
                this
        );
        getServer().getPluginManager().registerEvents(
                new com.muzlik.listener.NewPlayerListener(this, fragmentManager),
                this
        );
        getServer().getPluginManager().registerEvents(
                new com.muzlik.listener.FragmentItemListener(ritualManager, configManager, fragmentManager),
                this
        );
        
        // Register Luck Fragment ability listeners
        getServer().getPluginManager().registerEvents(
                new com.muzlik.fragment.ability.executors.luck.FortuneStrikeExecutor.FortuneStrikeListener(),
                this
        );
        getServer().getPluginManager().registerEvents(
                new com.muzlik.fragment.ability.executors.luck.LuckyDodgeExecutor.LuckyDodgeListener(),
                this
        );
        getServer().getPluginManager().registerEvents(
                new com.muzlik.fragment.ability.executors.luck.TreasureHunterExecutor.TreasureHunterListener(),
                this
        );
        getServer().getPluginManager().registerEvents(
                new com.muzlik.fragment.ability.executors.luck.ProbabilityManipulationExecutor.ProbabilityManipulationListener(),
                this
        );
        
        // Register this class as listener for player join
        getServer().getPluginManager().registerEvents(this, this);
        
        // Initialize and start health monitoring (Task 17.2)
        healthMonitor = new com.muzlik.monitoring.HealthMonitor(this);
        healthMonitor.startMonitoring();

        // Register commands
        com.muzlik.command.FragmentCommand fragmentCommand = new com.muzlik.command.FragmentCommand(
            this, fragmentManager, recipeManager, uiManager, manaManager, levelManager, rankManager,
            cooldownManager
        );
        getCommand("fragment").setExecutor(fragmentCommand);
        getCommand("fragment").setTabCompleter(fragmentCommand);
        
        // Register /controls command
        com.muzlik.command.ControlsCommand controlsCommand = new com.muzlik.command.ControlsCommand(uiManager);
        getCommand("controls").setExecutor(controlsCommand);
        
        // Register /testtexture command (debug)
        com.muzlik.command.TestTextureCommand testTextureCommand = new com.muzlik.command.TestTextureCommand();
        getCommand("testtexture").setExecutor(testTextureCommand);

        getLogger().info("✓ Fragment System initialized successfully!");
        getLogger().info("✓ Fragment ActionBar HUD initialized!");
        
        String GREEN = "\u001B[32m";
        // Reuse existing color variables from startup banner
        
        System.out.println("");
        System.out.println(GREEN + "╔═══════════════════════════════════════════════════════════════╗" + RESET);
        System.out.println(GREEN + "║" + RESET + "                                                               " + GREEN + "║" + RESET);
        System.out.println(GREEN + "║" + RESET + "                " + WHITE + BOLD + "✓ PLUGIN ENABLED SUCCESSFULLY" + RESET + "                  " + GREEN + "║" + RESET);
        System.out.println(GREEN + "║" + RESET + "                                                               " + GREEN + "║" + RESET);
        System.out.println(GREEN + "║" + RESET + "  " + WHITE + BOLD + "Systems Active:" + RESET + "                                              " + GREEN + "║" + RESET);
        System.out.println(GREEN + "║" + RESET + "    " + WHITE + "• " + CYAN + "Fragment System" + RESET + " (10 fragments, 40+ abilities)            " + GREEN + "║" + RESET);
        System.out.println(GREEN + "║" + RESET + "    " + WHITE + "• " + CYAN + "Mana System" + RESET + " (resource management)                        " + GREEN + "║" + RESET);
        System.out.println(GREEN + "║" + RESET + "    " + WHITE + "• " + CYAN + "VFX Engine" + RESET + " (5-layer particle system)                     " + GREEN + "║" + RESET);
        System.out.println(GREEN + "║" + RESET + "    " + WHITE + "• " + CYAN + "Ritual System" + RESET + " (multi-block structures)                   " + GREEN + "║" + RESET);
        System.out.println(GREEN + "║" + RESET + "    " + WHITE + "• " + CYAN + "Progression" + RESET + " (levels 1-50, ranks 1-8)                     " + GREEN + "║" + RESET);
        System.out.println(GREEN + "║" + RESET + "                                                               " + GREEN + "║" + RESET);
        System.out.println(GREEN + "║" + RESET + "  " + YELLOW + "Ready to serve players!" + RESET + "                                      " + GREEN + "║" + RESET);
        System.out.println(GREEN + "║" + RESET + "                                                               " + GREEN + "║" + RESET);
        System.out.println(GREEN + "╚═══════════════════════════════════════════════════════════════╝" + RESET);
        System.out.println("");
    }
    
    @Override
    public void onDisable() {
        String RED = "\u001B[31m";
        String GRAY = "\u001B[90m";
        String WHITE = "\u001B[37m";
        String YELLOW = "\u001B[33m";
        String RESET = "\u001B[0m";
        String BOLD = "\u001B[1m";
        
        System.out.println("");
        System.out.println(RED + "╔═══════════════════════════════════════════════════════════════╗" + RESET);
        System.out.println(RED + "║" + RESET + "                                                               " + RED + "║" + RESET);
        System.out.println(RED + "║" + GRAY + "   ███████╗██████╗  ██████╗ ███████╗████████╗███╗   ███╗ ██████╗" + RESET + RED + "║" + RESET);
        System.out.println(RED + "║" + GRAY + "   ██╔════╝██╔══██╗██╔═══██╗██╔════╝╚══██╔══╝████╗ ████║██╔════╝" + RESET + RED + "║" + RESET);
        System.out.println(RED + "║" + GRAY + "   █████╗  ██████╔╝██║   ██║███████╗   ██║   ██╔████╔██║██║     " + RESET + RED + "║" + RESET);
        System.out.println(RED + "║" + GRAY + "   ██╔══╝  ██╔══██╗██║   ██║╚════██║   ██║   ██║╚██╔╝██║██║     " + RESET + RED + "║" + RESET);
        System.out.println(RED + "║" + GRAY + "   ██║     ██║  ██║╚██████╔╝███████║   ██║   ██║ ╚═╝ ██║╚██████╗" + RESET + RED + "║" + RESET);
        System.out.println(RED + "║" + GRAY + "   ╚═╝     ╚═╝  ╚═╝ ╚═════╝ ╚══════╝   ╚═╝   ╚═╝     ╚═╝ ╚═════╝" + RESET + RED + "║" + RESET);
        System.out.println(RED + "║" + RESET + "                                                               " + RED + "║" + RESET);
        System.out.println(RED + "║" + RESET + "                  " + YELLOW + BOLD + "Shutting Down Systems..." + RESET + "                     " + RED + "║" + RESET);
        System.out.println(RED + "║" + RESET + "                                                               " + RED + "║" + RESET);
        System.out.println(RED + "║" + RESET + "  " + WHITE + "Created by: " + BOLD + "muzlik-gm" + RESET + "                                        " + RED + "║" + RESET);
        System.out.println(RED + "║" + RESET + "  " + WHITE + "Thank you for using FrostMC!" + RESET + "                                 " + RED + "║" + RESET);
        System.out.println(RED + "║" + RESET + "                                                               " + RED + "║" + RESET);
        System.out.println(RED + "╚═══════════════════════════════════════════════════════════════╝" + RESET);
        System.out.println("");
        
        // Shutdown Flight system
        if (flightManager != null) {
            flightManager.shutdown();
        }
        
        // Shutdown Luck Fragment passive system
        if (luckPassiveManager != null) {
            luckPassiveManager.stop();
        }
        
        // Shutdown Block Manipulation system
        if (blockManipulationEngine != null) {
            blockManipulationEngine.shutdown();
        }
        
        // Shutdown VFX system
        if (cinematicVFXEngine != null) {
            cinematicVFXEngine.cleanup();
        }
        if (environmentManager != null) {
            environmentManager.shutdown();
        }
        if (soundEngine != null) {
            soundEngine.shutdown();
        }
        if (vfxEngine != null) {
            vfxEngine.shutdown();
        }
        if (vfxPerformanceManager != null) {
            vfxPerformanceManager.shutdown();
        }
        if (damageAttributionManager != null) {
            damageAttributionManager.shutdown();
        }
        if (effectRegistry != null) {
            effectRegistry.shutdown();
        }
        
        // Shutdown Fragment HUD
        if (fragmentHUD != null) {
            fragmentHUD.stop();
        }
        
        // Shutdown Fragment systems
        if (ritualManager != null) {
            ritualManager.shutdown();
        }
        if (manaManager != null) {
            manaManager.shutdown();
        }
        if (levelManager != null) {
            levelManager.shutdown();
        }
        if (rankManager != null) {
            rankManager.shutdown();
        }
        if (fragmentManager != null) {
            fragmentManager.shutdown();
        }
        if (abilityRegistry != null) {
            abilityRegistry.shutdown();
        }
        if (characterLevelManager != null) {
            characterLevelManager.shutdown();
        }
        if (preferencesManager != null) {
            preferencesManager.shutdown();
        }
        if (cooldownAPI != null) {
            cooldownAPI.shutdown();
        }
        
        // Shutdown CooldownManager
        if (cooldownManager != null) {
            cooldownManager.clearAll();
        }
        
        // Shutdown health monitoring
        if (healthMonitor != null) {
            healthMonitor.shutdown();
        }
        
        getLogger().info("All systems shutdown complete");
    }
    
    /**
     * Reload plugin configuration and update runtime state
     */
    public void reloadPluginConfig() {
        // Reload config file
        configManager.reloadConfig();
        
        // Update mana system state
        boolean manaSystemEnabled = configManager.isManaSystemEnabled();
        manaManager.setManaSystemEnabled(manaSystemEnabled);
        
        getLogger().info("Plugin configuration reloaded");
        getLogger().info("Mana System: " + (manaSystemEnabled ? "ENABLED" : "DISABLED"));
    }

    // Getters for managers
    public CooldownManager getCooldownManager() { return cooldownManager; }
    public FragmentManager getFragmentManager() { return fragmentManager; }
    public ManaManager getManaManager() { return manaManager; }
    public LevelManager getLevelManager() { return levelManager; }
    public RankManager getRankManager() { return rankManager; }
    public AbilityRegistry getAbilityRegistry() { return abilityRegistry; }
    public CooldownAPI getCooldownAPI() { return cooldownAPI; }
    public FXLibrary getFXLibrary() { return fxLibrary; }
    public RitualManager getRitualManager() { return ritualManager; }
    public DamageAPI getDamageAPI() { return damageAPI; }
    public RecipeManager getRecipeManager() { return recipeManager; }
    public UIManager getUIManager() { return uiManager; }
    public ConfigManager getConfigManager() { return configManager; }
    public DataPersistence getDataPersistence() { return dataPersistence; }
    public ActiveEffectRegistry getEffectRegistry() { return effectRegistry; }
    public DamageAttributionManager getDamageAttributionManager() { return damageAttributionManager; }
    public VFXEngine getVFXEngine() { return vfxEngine; }
    public SoundEngine getSoundEngine() { return soundEngine; }
    public EnvironmentManager getEnvironmentManager() { return environmentManager; }
    public com.muzlik.block.BlockManipulationEngine getBlockManipulationEngine() { return blockManipulationEngine; }
    public AbilityVFXHelper getAbilityVFXHelper() { return abilityVFXHelper; }
    public com.muzlik.vfx.VFXPerformanceManager getVFXPerformanceManager() { return vfxPerformanceManager; }
    public com.muzlik.fragment.ability.FlightManager getFlightManager() { return flightManager; }
    public com.muzlik.vfx.cinematic.CinematicVFXEngine getCinematicVFXEngine() { return cinematicVFXEngine; }
    public com.muzlik.vfx.cinematic.miniblock.MiniBlockManager getMiniBlockManager() { return miniBlockManager; }
    public CharacterLevelManager getCharacterLevelManager() { return characterLevelManager; }
    public com.muzlik.player.PlayerPreferencesManager getPreferencesManager() { return preferencesManager; }
    public com.muzlik.fragment.ability.executors.luck.LuckFragmentPassiveManager getLuckPassiveManager() { return luckPassiveManager; }
    public com.muzlik.fragment.ability.AbilitySlotManager getAbilitySlotManager() { return abilitySlotManager; }
    public com.muzlik.monitoring.HealthMonitor getHealthMonitor() { return healthMonitor; }
}


