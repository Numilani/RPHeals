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
        statement.execute("CREATE TABLE Duels(Id INTEGER PRIMARY KEY AUTOINCREMENT, PlayerOneId TEXT, PlayerOneHealth INTEGER, PlayerOneConfirm INTEGER, PlayerTwoId TEXT, PlayerTwoHealth INTEGER, PlayerTwoConfirm INTEGER)");
    }

    public boolean playerExistsInCampfireTable(String playerId) throws SQLException{
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

    public void createDuel(String playerOneUuid, String playerTwoUuid) throws SQLException {
        var statement = conn.prepareStatement("INSERT INTO Duels (PlayerOneId, PlayerTwoId, PlayerOneConfirm, PlayerTwoConfirm) VALUES (?, ?, 1, 0)");
        statement.setString(1, playerOneUuid);
        statement.setString(2, playerTwoUuid);
        statement.execute();
    }

    public boolean playerInActiveDuel(String uuid) throws SQLException {
        var statement = conn.prepareStatement("SELECT Id FROM Duels WHERE PlayerOneId = ? OR PlayerTwoId = ?");
        statement.setString(1, uuid);
        statement.setString(2, uuid);
        var x = statement.executeQuery();

        if (!x.next()) return false;
        return true;
    }

    public Duel getDuel(String playerUuid) throws SQLException {
        var statement = conn.prepareStatement("SELECT Id, PlayerOneId, PlayerOneHealth, PlayerOneConfirm, PlayerTwoId, PlayerTwoHealth, PlayerTwoConfirm FROM Duels WHERE PlayerOneId = ? OR PlayerTwoId = ?");
        statement.setString(1, playerUuid);
        statement.setString(2, playerUuid);
        var x = statement.executeQuery();

        if (!x.next()){
            return null;
        }
        var duel = new Duel();

        duel.Id = x.getInt(1);
        duel.PlayerOneId = x.getString(2);
        duel.PlayerOneHealth = x.getInt(3);
        duel.PlayerOneConfirm = x.getBoolean(4);
        duel.PlayerTwoId = x.getString(5);
        duel.PlayerTwoHealth = x.getInt(6);
        duel.PlayerTwoConfirm = x.getBoolean(7);

        return duel;
    }

    public void acceptDuel(String playerOneUuid, String playerTwoUuid, int playerOneHp, int playerTwoHp) throws SQLException {
        var statement = conn.prepareStatement("UPDATE Duels SET PlayerOneHealth = ?, PlayerTwoHealth = ?, PlayerTwoConfirm = 1 WHERE PlayerOneId = ?");
        statement.setInt(1, playerOneHp);
        statement.setInt(2, playerTwoHp);
        statement.setString(3, playerOneUuid);
        statement.execute();
    }

    public void removeDuel(String playerOneUuid, String playerTwoUuid) throws SQLException {
        var statement = conn.prepareStatement("DELETE FROM Duels WHERE (PlayerOneId = ? AND PlayerTwoId = ?) OR (PlayerTwoId = ? AND PlayerOneId = ?)");
        statement.setString(1, playerOneUuid);
        statement.setString(2, playerTwoUuid);
        statement.setString(3, playerOneUuid);
        statement.setString(4, playerTwoUuid);
        statement.execute();
    }

    public void removeDuel(int Id) throws SQLException{
        var statement = conn.prepareStatement("DELETE FROM Duels WHERE Id = ?");
        statement.setInt(1, Id);
        statement.execute();
    }

    public void purgeDuelList() throws SQLException {
        var statement = conn.createStatement().execute("DELETE FROM Duels");
    }

}
