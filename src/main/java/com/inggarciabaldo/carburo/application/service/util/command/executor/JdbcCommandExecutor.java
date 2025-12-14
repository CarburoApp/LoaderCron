package com.inggarciabaldo.carburo.application.service.util.command.executor;

import com.inggarciabaldo.carburo.application.persistance.PersistenceException;
import com.inggarciabaldo.carburo.application.service.util.command.Command;
import com.inggarciabaldo.carburo.config.persistencia.jdbc.Jdbc;

import java.sql.Connection;
import java.sql.SQLException;

public class JdbcCommandExecutor {

	public <T> T execute(Command<T> cmd) {
		try {
			Connection c = Jdbc.createThreadConnection();

			try {
				c.setAutoCommit(false);

				T res = cmd.execute();

				c.commit();
				return res;

			} catch (Exception e) {
				c.rollback();
				throw e;

			} finally {
				c.close();
			}

		} catch (SQLException e) {
			throw new PersistenceException(e);
		}
	}

}
