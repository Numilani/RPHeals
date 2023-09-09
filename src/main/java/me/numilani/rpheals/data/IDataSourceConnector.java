package me.numilani.rpheals.data;

import java.sql.SQLException;

public interface IDataSourceConnector {
    void initDatabase();
    void initPlayer(String playerId) throws SQLException;
    void updatePlayerTime(String playerId) throws SQLException;
}
