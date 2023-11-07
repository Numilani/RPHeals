package me.numilani.rpheals;

import com.bergerkiller.bukkit.common.cloud.CloudSimpleHandler;
import com.bergerkiller.bukkit.common.config.FileConfiguration;
import me.numilani.rpheals.data.IDataSourceConnector;
import me.numilani.rpheals.data.SqliteDataSourceConnector;
import me.numilani.rpheals.listeners.CampfireListener;
import me.numilani.rpheals.listeners.DuelListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

public final class RPHeal extends JavaPlugin {

    public CloudSimpleHandler cmdHandler = new CloudSimpleHandler();
    public FileConfiguration cfg;
    public IDataSourceConnector dataSource;

    @Override
    public void onEnable() {
        // First run setup
        var isFirstRun = false;
        if (!(new FileConfiguration(this, "config.yml").exists())) {
            isFirstRun = true;
            doPluginInit();
        }

        cfg = new FileConfiguration(this, "config.yml");
        cfg.load();

        // do a check for datasourcetype once that's added to config
        // for now, just set datasource to sqlite always
        try {
            dataSource = new SqliteDataSourceConnector(this);
            if (isFirstRun) dataSource.initDatabase();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // Register events
        getServer().getPluginManager().registerEvents(new CampfireListener(this), this);
        getServer().getPluginManager().registerEvents(new DuelListener(this), this);

        // Register commands
        cmdHandler.enable(this);
        //cmdHandler.getParser().parse(new ChatCommandHandler(this));
    }

    private void doPluginInit() {
        var cfgFile = new FileConfiguration(this, "config.yml");
        cfgFile.addHeader("How long the cooldown between campfire uses should be, in minutes.");
        cfgFile.set("cooldownInterval", 90);

        cfgFile.saveSync();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
