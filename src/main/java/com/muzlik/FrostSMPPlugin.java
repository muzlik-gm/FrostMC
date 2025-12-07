package com.muzlik;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import com.muzlik.power.PowerManager;
import com.muzlik.listener.PlayerPowerListener;
import com.muzlik.command.PowerCommand;
import com.muzlik.powers.FirePower;

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
    
    // Legacy systems
    private PowerManager powerManager;
    private PlayerPowerListener playerPowerListener;
    private com.muzlik.ui.PowerGUI powerGUI;
    private com.muzlik.ui.PowerHUD powerHUD;
    
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
    
    // Block Manipulation System
    private com.muzlik.block.BlockManipulationEngine blockManipulationEngine;
    
    // Flight System
    private com.muzlik.fragment.ability.FlightManager flightManager;
    
    // Character Level System (affects max mana)
    private CharacterLevelManager characterLevelManager;

    @Override
    public void onEnable() {
        getLogger().info("================================");
        getLogger().info("FrostSMP Plugin starting...");
        getLogger().info("================================");

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
        
        // Initialize block manipulation system
        blockManipulationEngine = new com.muzlik.block.BlockManipulationEngine(this, effectRegistry, damageAttributionManager);
        blockManipulationEngine.registerCleanupListener();
        blockManipulationEngine.startUpdateTask();
        
        abilityVFXHelper = new AbilityVFXHelper(this, vfxEngine, soundEngine);
        
        // Initialize core managers
        manaManager = new ManaManager(this);
        levelManager = new LevelManager(this);
        rankManager = new RankManager(this);
        
        // Set cross-references for auto rank-up
        levelManager.setRankManager(rankManager);
        
        // Initialize Character Level system (affects max mana)
        characterLevelManager = new CharacterLevelManager(this);
        manaManager.setCharacterLevelManager(characterLevelManager);
        
        // Initialize Fragment manager
        fragmentManager = new FragmentManager(this, manaManager, levelManager, rankManager);
        
        // Initialize ability systems
        abilityRegistry = new AbilityRegistry(this);
        cooldownAPI = new CooldownAPI();
        
        // Initialize flight system
        flightManager = new com.muzlik.fragment.ability.FlightManager(this);
        
        // Initialize FX Library
        fxLibrary = new FXLibrary(this);
        fxLibrary.setDebugMode(configManager.isFXDebugMode());
        fxLibrary.setParticleDensityMultiplier(configManager.getParticleDensity());
        
        // Initialize legacy power manager FIRST (needed for CooldownManager)
        powerManager = new PowerManager(this);
        powerManager.registerPower(new FirePower());
        
        // Initialize ritual system
        ritualManager = new RitualManager(this, fragmentManager, fxLibrary);
        ritualManager.setProximityDistance(configManager.getFragmentCreationProximity());
        
        // Initialize damage API
        damageAPI = new DamageAPI(this);
        
        // Initialize recipe manager
        recipeManager = new RecipeManager(this);
        recipeManager.registerRecipes();
        
        // Initialize UI manager (with CooldownManager from PowerManager)
        uiManager = new UIManager(this, fragmentManager, manaManager, levelManager, rankManager, 
                powerManager.getCooldownManager());
        
        // Initialize data persistence
        dataPersistence = new DataPersistence(this);
        
        // Register all 10 Fragments
        fragmentRegistry = new FragmentRegistry(this, fragmentManager, fxLibrary);
        fragmentRegistry.registerAllFragments();
        
        // Register placeholder powers for all fragments (prevents "non-existent power" warnings)
        registerPlaceholderPowers();

        // Initialize legacy GUI
        powerGUI = new com.muzlik.ui.PowerGUI(powerManager);
        getServer().getPluginManager().registerEvents(powerGUI, this);

        // Initialize legacy HUD
        powerHUD = new com.muzlik.ui.PowerHUD(this, powerManager);
        powerHUD.start();
        
        // Initialize Fragment HUD (with CharacterLevelManager)
        fragmentHUD = new com.muzlik.ui.FragmentActionBarHUD(this, fragmentManager, manaManager, powerManager.getCooldownManager());
        fragmentHUD.setCharacterLevelManager(characterLevelManager);
        fragmentHUD.start();

        // Register listeners
        playerPowerListener = new PlayerPowerListener(powerManager);
        getServer().getPluginManager().registerEvents(
                playerPowerListener,
                this
        );
        
        // Create and register FragmentAbilityListener with LevelManager for level bonuses
        com.muzlik.listener.FragmentAbilityListener fragmentAbilityListener = 
            new com.muzlik.listener.FragmentAbilityListener(fragmentManager, manaManager, powerManager.getCooldownManager());
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
                new com.muzlik.listener.FirstJoinListener(this, fragmentManager),
                this
        );
        getServer().getPluginManager().registerEvents(
                new com.muzlik.ui.FragmentGUIListener(fragmentManager, uiManager),
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
                new com.muzlik.listener.PassiveAbilityListener(this, fragmentManager, rankManager, manaManager, powerManager.getCooldownManager()),
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
        
        // Register this class as listener for player join
        getServer().getPluginManager().registerEvents(this, this);

        // Register commands
        PowerCommand powerCommand = new PowerCommand(powerManager);
        powerCommand.setPowerGUI(powerGUI);
        getCommand("power").setExecutor(powerCommand);
        getCommand("power").setTabCompleter(powerCommand);
        
        com.muzlik.command.FragmentCommand fragmentCommand = new com.muzlik.command.FragmentCommand(
            this, fragmentManager, recipeManager, uiManager, manaManager, levelManager, rankManager,
            powerManager.getCooldownManager()
        );
        getCommand("fragment").setExecutor(fragmentCommand);
        getCommand("fragment").setTabCompleter(fragmentCommand);

        getLogger().info("Fragment System initialized successfully!");
        getLogger().info("Fragment ActionBar HUD initialized!");
        getLogger().info("FrostSMP Plugin enabled successfully!");
    }
    
    /**
     * Register placeholder powers for all fragments to prevent warnings
     */
    private void registerPlaceholderPowers() {
        // These are placeholder powers that do nothing but prevent "non-existent power" warnings
        // The actual abilities are handled by the Fragment system
        String[] fragmentPowers = {
            "water_power", "air_power", "earth_power", "dark_power", 
            "light_power", "void_power", "mob_power", "dragon_power", "storm_power"
        };
        
        for (String powerName : fragmentPowers) {
            powerManager.registerPower(new com.muzlik.powers.PlaceholderPower(powerName));
        }
        
        getLogger().info("Registered placeholder powers for all fragments");
    }

    @Override
    public void onDisable() {
        getLogger().info("================================");
        getLogger().info("FrostSMP Power Plugin disabled");
        getLogger().info("================================");
        
        // Shutdown Flight system
        if (flightManager != null) {
            flightManager.shutdown();
        }
        
        // Shutdown Block Manipulation system
        if (blockManipulationEngine != null) {
            blockManipulationEngine.shutdown();
        }
        
        // Shutdown VFX system
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
        if (cooldownAPI != null) {
            cooldownAPI.shutdown();
        }
        
        // Shutdown legacy systems
        if (powerHUD != null) {
            powerHUD.stop();
        }
        if (powerManager != null) {
            powerManager.shutdown();
        }
        
        getLogger().info("All systems shutdown complete");
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Load player's saved power (legacy)
        powerManager.loadPlayerPower(event.getPlayer());
    }

    // Getters for managers
    public PowerManager getPowerManager() { return powerManager; }
    public PlayerPowerListener getPlayerPowerListener() { return playerPowerListener; }
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
    public CharacterLevelManager getCharacterLevelManager() { return characterLevelManager; }
}


