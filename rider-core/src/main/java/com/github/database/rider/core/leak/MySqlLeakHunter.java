package com.github.database.rider.core.leak;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by pestano on 07/09/16.
 */
class MySqlLeakHunter extends AbstractLeakHunter {

    private final String sql = "SELECT COUNT(*) FROM v$session WHERE status = 'INACTIVE'";

    public MySqlLeakHunter(Connection connection, String methodName, boolean cacheConnection) {
        super(connection, methodName, cacheConnection);
    }

    @Override
    public int openConnections() {
        try (Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery(
                    "SHOW PROCESSLIST")) {
                int count = 0;
                while (resultSet.next()) {
                    String state = resultSet.getString("command");
                    if ("sleep".equalsIgnoreCase(state)) {
                        count++;
                    }
                }
                return count;
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    protected String leakCountSql() {
        return sql;
    }

}
