package me.numilani.rpheals.data;

import me.numilani.rpheals.RPHeal;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SqliteDataSourceConnector implements IDataSourceConnector{
    private RPHeal plugin;
    private String dbFilename = "rpheal.db";
    private Connection conn;

    public SqliteDataSourceConnector(RPHeal plugin) throws SQLException {
        this.plugin = plugin;
        conn = DriverManager.getConnection("jdbc:sqlite:" + new File(plugin.getDataFolder(), dbFilename).getPath());
    }

    public void initDatabase(){}

    public void initPlayer(String playerId) throws SQLException {
        var statement = conn.prepareStatement("INSERT INTO CampfireTimers (playerId, time) values (?, '2023-01-01 00:00.000')");
        statement.setString(1, playerId);

        statement.execute();
    }

    public void updatePlayerTime(String playerId) throws SQLException {
        var statement = conn.prepareStatement("UPDATE CampfireTimers SET time = current_timestamp WHERE playerId = ?");
        statement.setString(1, playerId);

        var x = statement.executeUpdate();
        if (x == 0){
            plugin.getLogger().warning("couldn't update campfire timer, this might be indicative of a problem!");
        }
    }

}
