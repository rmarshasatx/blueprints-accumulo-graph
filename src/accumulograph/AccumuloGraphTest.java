package accumulograph;

import java.lang.reflect.Method;

import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.Instance;
import org.apache.accumulo.core.client.TableExistsException;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.apache.accumulo.core.client.admin.TableOperations;
import org.apache.accumulo.core.client.mock.MockInstance;

import com.tinkerpop.blueprints.EdgeTestSuite;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.GraphTestSuite;
import com.tinkerpop.blueprints.IndexTestSuite;
import com.tinkerpop.blueprints.IndexableGraphTestSuite;
import com.tinkerpop.blueprints.KeyIndexableGraphTestSuite;
import com.tinkerpop.blueprints.TestSuite;
import com.tinkerpop.blueprints.VertexTestSuite;
import com.tinkerpop.blueprints.impls.GraphTest;
import com.tinkerpop.blueprints.util.io.gml.GMLReaderTestSuite;
import com.tinkerpop.blueprints.util.io.graphml.GraphMLReaderTestSuite;
import com.tinkerpop.blueprints.util.io.graphson.GraphSONReaderTestSuite;

public class AccumuloGraphTest extends GraphTest {

	public void testVertexTestSuite() throws Exception {
		this.stopWatch();
		doTestSuite(new VertexTestSuite(this));
		printTestPerformance("VertexTestSuite", this.stopWatch());
	}

	public void testEdgeTestSuite() throws Exception {
		this.stopWatch();
		doTestSuite(new EdgeTestSuite(this));
		printTestPerformance("EdgeTestSuite", this.stopWatch());
	}

	public void testGraphTestSuite() throws Exception {
		this.stopWatch();
		doTestSuite(new GraphTestSuite(this));
		printTestPerformance("GraphTestSuite", this.stopWatch());
	}

	public void testKeyIndexableGraphTestSuite() throws Exception {
		this.stopWatch();
		doTestSuite(new KeyIndexableGraphTestSuite(this));
		printTestPerformance("KeyIndexableGraphTestSuite", this.stopWatch());
	}

	public void testIndexableGraphTestSuite() throws Exception {
		this.stopWatch();
		doTestSuite(new IndexableGraphTestSuite(this));
		printTestPerformance("IndexableGraphTestSuite", this.stopWatch());
	}

	public void testIndexTestSuite() throws Exception {
		this.stopWatch();
		doTestSuite(new IndexTestSuite(this));
		printTestPerformance("IndexTestSuite", this.stopWatch());
	}

	public void testGraphMLReaderTestSuite() throws Exception {
		this.stopWatch();
		doTestSuite(new GraphMLReaderTestSuite(this));
		printTestPerformance("GraphMLReaderTestSuite", this.stopWatch());
	}

	public void testGMLReaderTestSuite() throws Exception {
		this.stopWatch();
		doTestSuite(new GMLReaderTestSuite(this));
		printTestPerformance("GMLReaderTestSuite", this.stopWatch());
	}

	public void testGraphSONReaderTestSuite() throws Exception {
		this.stopWatch();
		doTestSuite(new GraphSONReaderTestSuite(this));
		printTestPerformance("GraphSONReaderTestSuite", this.stopWatch());
	}

	@Override
	public Graph generateGraph() {
		return generateGraph("testAccumuloGraph");
	}

	@Override
	public void doTestSuite(final TestSuite testSuite) throws Exception {
		String doTest = System.getProperty("testAccumuloGraph");
		if (doTest == null || doTest.equals("true")) {
			for (Method method : testSuite.getClass().getDeclaredMethods()) {
				if (method.getName().startsWith("test")) {
					System.out.println("Testing " + method.getName() + "...");
					method.invoke(testSuite);
				}
			}
		}
	}

	@Override
	public Graph generateGraph(String graphDirectoryName) {
		try {
			Instance instance = new MockInstance(graphDirectoryName);
			Connector connector = instance.getConnector("", "".getBytes());

			TableOperations ops = connector.tableOperations();

			if (ops.exists(graphDirectoryName)) {
				ops.delete(graphDirectoryName);
			}

			ops.create(graphDirectoryName);

			return new AccumuloGraph(connector, graphDirectoryName);

		} catch (AccumuloException e) {
			throw new RuntimeException(e);
		} catch (AccumuloSecurityException e) {
			throw new RuntimeException(e);
		} catch (TableNotFoundException e) {
			throw new RuntimeException(e);
		} catch (TableExistsException e) {
			throw new RuntimeException(e);
		}
	}
}