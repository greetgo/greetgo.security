package kz.greetgo.security.crypto.jdbc;

import kz.greetgo.security.crypto.jdbc.create_table.CreateTable;

import java.sql.SQLException;

public interface DbDialect {

  boolean isNoTable(SQLException e);

  boolean isRecordAlreadyExists(SQLException e);

  String generateCreateTableDDL(CreateTable createTable);
}
