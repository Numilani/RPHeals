package me.numilani.rpheals.data;

import java.sql.SQLException;
import java.time.LocalDateTime;

public interface IDataSourceConnector {
    void initDatabase() throws SQLException;
    boolean playerExistsInCampfireTable(String playerId) throws SQLException;
    void initPlayer(String playerId) throws SQLException;
    LocalDateTime getPlayerCampfireInteractionTime(String playerId) throws SQLException;
    void updatePlayerCampfireInteractionTime(String playerId) throws SQLException;
    Duel createDuel(String playerOneUuid, String playerTwoUuid) throws SQLException;
    boolean playerInActiveDuel(String uuid) throws SQLException;
    Duel getDuel(String playerOneUuid) throws SQLException;
    void acceptDuel(String playerOneUuid, String playerTwoUuid, int playerOneHp, int playerTwoHp) throws SQLException;
    void removeDuel(String playerOneUuid, String playerTwoUuid) throws SQLException;
    void removeDuel(int Id) throws SQLException;
    void purgeDuelList() throws SQLException;
}
