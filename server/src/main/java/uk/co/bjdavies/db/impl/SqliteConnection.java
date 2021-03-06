/*
 * MIT License
 *
 * Copyright (c) 2020 Ben Davies
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package uk.co.bjdavies.db.impl;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import uk.co.bjdavies.api.config.IDatabaseConfig;
import uk.co.bjdavies.api.db.*;
import uk.co.bjdavies.db.DBRecord;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ben.davies99@outlook.com (Ben Davies)
 * @since 1.0.0
 */
@Log4j2
public class SqliteConnection implements IConnection {


    private final Connection connection;

    @SneakyThrows
    public SqliteConnection(IDatabaseConfig config) {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        File file = new File(config.getDatabase());
        if (!file.exists()) {
            try {
                //noinspection ResultOfMethodCallIgnored
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        connection = DriverManager.getConnection("jdbc:sqlite:" + config.getDatabase());
    }

    @Override
    public <T extends IDBRecord> List<T> executeQuery(IQueryBuilder<T> query) throws SQLException {
        //noinspection unchecked
        return (List<T>) processResultSet(connection.prepareStatement(query.buildQuery()).executeQuery());
    }

    @Override
    public Object executeCommand(ICommandBuilder command) {
        try {
            ISQLCommand sqlCommand = command.buildCommand();
            PreparedStatement statement = connection.prepareStatement(sqlCommand.getSQL());
            for (int i = 0; i < sqlCommand.getValues().size(); i++) {
                statement.setString(i + 1, sqlCommand.getValues().get(i));
            }
            connection.setAutoCommit(false);
            int result = statement.executeUpdate();
            if (result > 0) connection.commit();
            connection.setAutoCommit(true);
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Connection getSQLConnection() {
        //TODO: Remove this and add table creation to this connection class its DB dependant.
        return connection;
    }

    private List<IDBRecord> processResultSet(ResultSet resultSet) {
        List<IDBRecord> rows = new ArrayList<>();
        try {
            ResultSetMetaData metaData = resultSet.getMetaData();
            while (resultSet.next()) {
                Map<String, Object> objectMap = new HashMap<>();
                for (int i = 1; i < metaData.getColumnCount() + 1; i++) {
                    String columnName = metaData.getColumnName(i);
                    Object columnValue = resultSet.getObject(columnName);
                    objectMap.put(columnName, columnValue);
                }

                rows.add(new DBRecord(objectMap));
            }
        } catch (Exception e) {
            log.error("Error selecting all from the table, most likely the table has not been created.", e);
            return rows;
        }

        return rows;
    }

    @Override
    public <T extends IDBRecord> IQueryBuilder<T> createQueryBuilder(String table, String primaryKey) {
        return createQueryBuilder(table, primaryKey, null);
    }

    @Override
    public ICommandBuilder createCommandBuilder(String tableName) {
        return new SqliteCommandBuilder(tableName, this);
    }

    @Override
    public <T extends IDBRecord> IQueryBuilder<T> createQueryBuilder(String table, String primaryKey, Class<T> model) {
        return new SqliteQueryBuilder<>(primaryKey, table, this, model);
    }
}
