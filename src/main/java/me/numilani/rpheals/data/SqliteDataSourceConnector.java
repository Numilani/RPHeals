package me.numilani.rpheals.data;

import me.numilani.rpheals.RPHeal;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDateTime;

public class SqliteDataSourceConnector implements IDataSourceConnector{
    private RPHeal plugin;
    private String dbFilename = "rpheal.db";
    private Connection conn;

    public SqliteDataSourceConnector(RPHeal plugin) throws SQLException {
        this.plugin = plugin;
        conn = DriverManager.getConnection("jdbc:sqlite:" + new File(plugin.getDataFolder(), dbFilename).getPath());
    }

    public void initDatabase() throws SQLException {
        var statement = conn.createStatement();
        statement.execute("CREATE TABLE CampfireTimers (playerid TEXT PRIMARY KEY, last_time TEXT)");
    }

    public boolean playerExistsInTable(String playerId) throws SQLException{
        var existStatement = conn.prepareStatement("SELECT * FROM CampfireTimers where playerId = ?");
        existStatement.setString(1,playerId);
        var x = existStatement.executeQuery();
        return x.next();
    }

    public void initPlayer(String playerId) throws SQLException {
        var statement = conn.prepareStatement("INSERT INTO CampfireTimers (playerId, last_time) values (?, ?)");
        statement.setString(1, playerId);
        statement.setString(2, LocalDateTime.now(Clock.systemUTC()).minusMinutes(100).toString());

        statement.execute();
    }

    public LocalDateTime getPlayerCampfireInteractionTime(String playerId) throws SQLException{
        var statement = conn.prepareStatement("SELECT last_time FROM CampfireTimers WHERE playerId = ?");
        statement.setString(1, playerId);

        var x = statement.executeQuery();
        if (!x.next()){
            plugin.getLogger().warning("couldn't get campfire timer, user was not initialized in db?");
        }
        return LocalDateTime.parse(x.getString(1));
    }

    public void updatePlayerCampfireInteractionTime(String playerId) throws SQLException {
        var statement = conn.prepareStatement("UPDATE CampfireTimers SET last_time = ? WHERE playerId = ?");
        statement.setString(1, LocalDateTime.now(Clock.systemUTC()).toString());
        statement.setString(2, playerId);

        var x = statement.executeUpdate();
        if (x == 0){
            plugin.getLogger().warning("couldn't update campfire timer, this might be indicative of a problem!");
        }
    }

}
