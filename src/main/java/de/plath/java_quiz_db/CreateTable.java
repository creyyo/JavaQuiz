package de.plath.java_quiz_db;

import java.sql.Connection;
import java.sql.Statement;

public class CreateTable {

	public static void main(String[] args) {
		Connection c = null;
		Statement stmt = null;
		try {
			c = DBConnection.getConnection();
			System.out.println("Opened database successfully");

			stmt = c.createStatement();
			String sql = "CREATE TABLE IF NOT EXISTS USER_ANSWERS (" + 
							"ID 			SERIAL 		PRIMARY KEY,"	+ 
							"USER_ID		VARCHAR(50) NOT NULL,"	+	
							"QUESTION_ID	INT 		NOT NULL," +
							"GIVEN_ANSWERS 	TEXT[] 		NOT NULL," +
							"IS_CORRECT 	BOOLEAN 	NOT NULL," +
							"ANSWERED_AT	TIMESTAMP 	NOT NULL DEFAULT NOW()" +
							")";
			stmt.executeUpdate(sql);
			stmt.close();
			c.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		System.out.println("Table created successfully");
	}
}