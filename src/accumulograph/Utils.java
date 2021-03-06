package accumulograph;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.BatchWriter;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.MutationsRejectedException;
import org.apache.accumulo.core.client.Scanner;
import org.apache.accumulo.core.client.TableExistsException;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.apache.accumulo.core.client.admin.TableOperations;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Value;
import org.apache.hadoop.io.Text;

import accumulograph.Const.Type;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * Various utility methods.
 * @author Mike Lieberman (http://mikelieberman.org)
 */
public final class Utils {

	private static final Kryo KRYO = new Kryo();

	private Utils() {

	}

	public static Object makeId() {
		return UUID.randomUUID().toString();
	}

	public static <T> byte[] toBytes(T obj) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Output output = new Output(baos);
		KRYO.writeClassAndObject(output, obj);
		output.close();
		return baos.toByteArray();
	}

	public static <T> T fromBytes(byte[] bytes) {
		Input input = new Input(new ByteArrayInputStream(bytes));
		T obj = (T) KRYO.readClassAndObject(input);
		input.close();
		return obj;
	}

	public static <T> Value objectToValue(T obj) {
		return new Value(toBytes(obj));
	}

	public static <T> T valueToObject(Value value) {
		return fromBytes(value.get());
	}

	public static <T> Text typedObjectToText(Type type, T obj) {
		byte[] bytes = toBytes(obj);
		ByteBuffer buffer = ByteBuffer.allocate(1 + bytes.length);
		buffer.put((byte) type.ordinal());
		buffer.put(bytes);
		return new Text(buffer.array());
	}

	public static <T> T textToTypedObject(Text text) {
		byte[] bytes = text.getBytes();
		// Read past type code at beginning.
		return fromBytes(Arrays.copyOfRange(bytes, 1, bytes.length));
	}
	
	public static Value textToValue(Text text) {
		return new Value(text.getBytes());
	}

	public static Text valueToText(Value value) {
		return new Text(value.get());
	}

	public static Text stringToText(String str) {
		return new Text(str);
	}

	public static String textToString(Text text) {
		return text.toString();
	}
	
	public static Value stringToValue(String str) {
		return new Value(str.getBytes());
	}
	
	public static String valueToString(Value value) {
		return new String(value.get());
	}
	
	public static <T> Text objectToText(T obj) {
		return new Text(toBytes(obj));
	}
	
	public static <T> T textToObject(Text text) {
		return fromBytes(text.getBytes());
	}
	
	public static void addMutation(BatchWriter writer, Mutation mut) {
		addMutation(writer, mut, 0L);
	}

	public static void addMutation(BatchWriter writer, Mutation mut, long sleep) {
		try {
			writer.addMutation(mut);
			Thread.sleep(sleep); // Needed for timing issues
		} catch (MutationsRejectedException e) {
			throw new RuntimeException(e);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void flush(BatchWriter writer) {
		try {
			writer.flush();
		} catch (MutationsRejectedException e) {
			throw new RuntimeException(e);
		}
	}

	public static Map.Entry<Key, Value> firstEntry(Scanner scanner) {
		Iterator<Map.Entry<Key, Value>> i = scanner.iterator();
		return i.hasNext() ? i.next() : null;
	}
	
	public static void deleteAllEntries(Scanner scanner, BatchWriter writer) {
		Text row = new Text();
		Text cf = new Text();
		Text cq = new Text();
		
		for (Map.Entry<Key, Value> entry : scanner) {
			entry.getKey().getRow(row);
			entry.getKey().getColumnFamily(cf);
			entry.getKey().getColumnQualifier(cq);
			
			Mutation m = new Mutation(row);
			m.putDelete(cf, cq);
			addMutation(writer, m);
		}
	}
	
	public static void createTableIfNotExists(Connector conn, String table) throws AccumuloException, AccumuloSecurityException, TableExistsException {
		// Check whether table exists already and create if not.
		TableOperations ops = conn.tableOperations();
		if (!ops.exists(table)) {
			ops.create(table);
		}
	}
	
	public static void recreateTable(Connector conn, String table)
			throws AccumuloException, AccumuloSecurityException, TableNotFoundException, TableExistsException {
		TableOperations ops = conn.tableOperations();
		
		if (ops.exists(table)) {
			ops.delete(table);
		}
		
		ops.create(table);
	}
	
	public static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

}
