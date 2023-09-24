package me.numilani.rpheals.data;

import java.sql.SQLException;
import java.time.LocalDateTime;

public interface IDataSourceConnector {
    void initDatabase() throws SQLException;
    boolean playerExistsInTable(String playerId) throws SQLException;
    void initPlayer(String playerId) throws SQLException;

    LocalDateTime getPlayerCampfireInteractionTime(String playerId) throws SQLException;

    void updatePlayerCampfireInteractionTime(String playerId) throws SQLException;
}
