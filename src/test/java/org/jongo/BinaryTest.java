package org.jongo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mongodb.BasicDBObject;
import org.bson.types.Binary;
import org.jongo.util.JSONResultHandler;
import org.jongo.util.JongoTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jongo.util.JSONResultHandler.jsonify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class BinaryTest extends JongoTestCase {

    private Binary friendId;
    private MongoCollection collection;

    @Before
    public void setUp() throws Exception {
        friendId = new Binary("jongo".getBytes());
        collection = createEmptyCollection("friends");
    }

    @After
    public void tearDown() throws Exception {
        dropCollection("friends");
    }


    @Test
    public void testSave() throws Exception {
        BinaryFriend expected = new BinaryFriend();
        Binary expectedId = new Binary("friend2".getBytes());
        expected.setId(expectedId);
        expected.setName("friend2");

        collection.save(expected);

        BinaryFriend actual = collection.findOne("{ _id: #}", expectedId).as(BinaryFriend.class);
        assertEquals(expected, actual);
    }

    @Test
    public void testUpdate() throws Exception {
        BinaryFriend binaryFriend = new BinaryFriend();
        binaryFriend.setId(friendId);
        binaryFriend.setName("jongo");
        collection.save(binaryFriend);

        binaryFriend.setName("new friend");
        collection.update("{ _id: #}", friendId).with("#", binaryFriend);

        BinaryFriend actual = collection.findOne("{ _id: #}", friendId).as(BinaryFriend.class);
        assertEquals(binaryFriend, actual);
    }

    @Test
    public void testInsert() throws Exception {
        BinaryFriend expected = new BinaryFriend();
        Binary expectedId = new Binary("friend2".getBytes());
        expected.setId(expectedId);
        expected.setName("friend2");

        collection.insert("#", expected);

        BinaryFriend actual = collection.findOne("{ _id: #}", expectedId).as(BinaryFriend.class);
        assertEquals(expected, actual);

    }

    @Test
    public void testRemove() throws Exception {

        collection.remove("{ _id: #}", friendId);

        BinaryFriend actual = collection.findOne("{ _id: #}", friendId).as(BinaryFriend.class);
        assertNull(actual);
    }

    @Test
    public void canMarhsallBinary() throws Exception {

        BinaryFriend doc = new BinaryFriend();
        doc.id = new Binary("abcde".getBytes());

        collection.save(doc);

        assertHasBeenPersistedAs(jsonify("'_id' : { '$binary' : 'YWJjZGU=' , '$type' : 0}"));
        BinaryFriend result = collection.findOne().as(BinaryFriend.class);

        assertThat(result.id.getType()).isEqualTo(doc.id.getType());
        assertThat(result.id.getData()).isEqualTo(doc.id.getData());
    }

    private void assertHasBeenPersistedAs(String expectedPersistedJSON) {
        String result = collection.findOne().map(new JSONResultHandler());
        assertThat(result).contains(expectedPersistedJSON);
    }

    private static class BinaryFriend {
        @JsonProperty("_id")
        private Binary id;
        private String name;

        public Binary getId() {
            return new Binary(id.getData());
        }

        public void setId(Binary id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof BinaryFriend)) return false;

            BinaryFriend binaryFriend = (BinaryFriend) o;

            if (id != null ? !id.equals(binaryFriend.id) : binaryFriend.id != null) return false;
            if (name != null ? !name.equals(binaryFriend.name) : binaryFriend.name != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = id != null ? id.hashCode() : 0;
            result = 31 * result + (name != null ? name.hashCode() : 0);
            return result;
        }
    }
}
