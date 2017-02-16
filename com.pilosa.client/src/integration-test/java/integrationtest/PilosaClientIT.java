package integrationtest;

import com.pilosa.client.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

// Note that this integration test creates many random databases.
// It's recommended to run an ephemeral Pilosa server.
// E.g., with docker:
// $ docker run -it --rm --name pilosa -p 15555:15000 pilosa:latest

@Category(IntegrationTest.class)
public class PilosaClientIT {
    private String db;
    private final static String SERVER_ADDRESS = ":15555";

    @Before
    public void setUp() {
        this.db = getRandomDatabaseName();
    }

    @After
    public void tearDown() {
        PilosaClient client = getClient();
        client.deleteDatabase(this.db);
    }

    @Test
    public void queryTest() {
        PilosaClient client = getClient();
        PilosaResponse response = client.query(db, "SetBit(id=555, frame=\"query-test\", profileID=10)");
        assertEquals(true, response.getResult());
    }

    @Test(expected = PilosaException.class)
    public void failedConnectionTest() {
        PilosaClient client = new PilosaClient("http://non-existent-sub.pilosa.com:22222");
        client.query("test2DB", "SetBit(id=15, frame=\"test\", profileID=10)");
    }

    @Test(expected = PilosaException.class)
    public void parseErrorTest() {
        PilosaClient client = getClient();
        client.query("testDB", "SetBit(id=5, frame=\"test\", profileID:=10)");
    }

    @Test
    public void ormTest() {
        PilosaClient client = getClient();
        PilosaResponse response;
        Map<String, Object> attrs;
        List<Integer> bits;
        BitmapResult bitmapResult;

        response = client.query(db, Pql.clearBit(5, "test", 10));
        assertEquals(false, response.getResult());

        response = client.query(db, Pql.setBit(5, "test", 10));
        assertEquals(true, response.getResult());

        response = client.query(db, Pql.setBit(5, "test", 10));
        assertEquals(false, response.getResult());

        attrs = new HashMap<>(0);
        bits = new ArrayList<>(1);
        bits.add(10);
        response = client.query(db, Pql.bitmap(5, "test"));
        bitmapResult = (BitmapResult)response.getResult();
        assertNotNull(bitmapResult);
        assertEquals(attrs, bitmapResult.getAttributes());
        assertEquals(bits, bitmapResult.getBits());

        response = client.query(db, Pql.clearBit(5, "test", 10));
        assertEquals(true, response.getResult());

        attrs = new HashMap<>(0);
        bits = new ArrayList<>(0);
        response = client.query(db, Pql.bitmap(5, "test"));
        bitmapResult = (BitmapResult)response.getResult();
        assertNotNull(bitmapResult);
        assertEquals(attrs, bitmapResult.getAttributes());
        assertEquals(bits, bitmapResult.getBits());
    }

    @Test(expected = PilosaException.class)
    public void failedDeleteDatabaseTest() {
        PilosaClient client = new PilosaClient("http://non-existent-sub.pilosa.com:22222");
        client.deleteDatabase("non-existent");
    }

    private PilosaClient getClient() {
        return new PilosaClient(SERVER_ADDRESS);
    }

    private static int counter = 0;
    private static String getRandomDatabaseName() {
        return String.format("testdb-%d", ++counter);
    }
}