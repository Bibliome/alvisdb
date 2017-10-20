package fr.inra.maiage.bibliome.alvisdb;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConnectionManager implements AutoCloseable {
	private final Map<String,Closeable> connections = new HashMap<String,Closeable>();
	
	public Closeable getConnection(String name) {
		return connections.get(name);
	}
	
	public void addConnection(String name, Closeable connection) throws ADBException {
		if (connection == null) {
			throw new NullPointerException();
		}
		if (connections.containsKey(name)) {
			throw new ADBException("duplicate connection name: " + name);
		}
		connections.put(name, connection);
	}

	@Override
	public void close() throws IOException {
		for (Closeable connection : connections.values()) {
			connection.close();
		}
		connections.clear();
	}
}
