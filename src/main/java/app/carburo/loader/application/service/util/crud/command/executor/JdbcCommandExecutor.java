package app.carburo.loader.application.service.util.crud.command.executor;

import app.carburo.loader.application.persistance.PersistenceException;
import app.carburo.loader.application.service.util.crud.command.Command;
import app.carburo.loader.config.persistencia.jdbc.Jdbc;

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
