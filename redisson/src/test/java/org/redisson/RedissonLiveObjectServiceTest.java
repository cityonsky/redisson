package org.redisson;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.redisson.api.RBlockingDeque;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDeque;
import org.redisson.api.RList;
import org.redisson.api.RLiveObject;
import org.redisson.api.RLiveObjectService;
import org.redisson.api.RMap;
import org.redisson.api.RObject;
import org.redisson.api.RQueue;
import org.redisson.api.RSet;
import org.redisson.api.RSortedSet;
import org.redisson.api.RedissonClient;
import org.redisson.api.annotation.REntity;
import org.redisson.api.annotation.RFieldAccessor;
import org.redisson.api.annotation.RId;
import org.redisson.liveobject.resolver.DefaultNamingScheme;
import org.redisson.liveobject.resolver.DistributedAtomicLongIdGenerator;

/**
 *
 * @author Rui Gu (https://github.com/jackygurui)
 */
public class RedissonLiveObjectServiceTest extends BaseTest {

    @REntity
    public static class TestREntity implements Comparable<TestREntity>, Serializable {

        @RId
        private String name;
        private String value;

        public TestREntity(String name) {
            this.name = name;
        }

        public TestREntity(String name, String value) {
            super();
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setValue(String value) {
            this.value = value;
        }

        @Override
        public int compareTo(TestREntity o) {
            int res = name.compareTo(o.name);
            if (res == 0) {
                return value.compareTo(o.value);
            }
            return res;
        }
    }

    @REntity
    public static class TestREntityWithRMap implements Comparable<TestREntityWithRMap>, Serializable {

        @RId
        private String name;
        private RMap value;

        public TestREntityWithRMap(String name) {
            this.name = name;
        }

        public TestREntityWithRMap(String name, RMap value) {
            super();
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public RMap getValue() {
            return value;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setValue(RMap value) {
            this.value = value;
        }

        @Override
        public int compareTo(TestREntityWithRMap o) {
            int res = name.compareTo(o.name);
            if (res == 0 || value != null || o.value != null) {
                if (value.getName() == null) {
                    return -1;
                }
                return value.getName().compareTo(o.value.getName());
            }
            return res;
        }
    }

    @REntity
    public static class TestREntityWithMap implements Comparable<TestREntityWithMap>, Serializable {

        @RId
        private String name;
        private Map value;
        
        public TestREntityWithMap() {
        }

        public TestREntityWithMap(String name) {
            this.name = name;
        }

        public TestREntityWithMap(String name, Map value) {
            super();
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public Map getValue() {
            return value;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setValue(Map value) {
            this.value = value;
        }

        @Override
        public int compareTo(TestREntityWithMap o) {
            return name.compareTo(o.name);
        }
    }

    @REntity
    public static class TestREntityIdNested implements Comparable<TestREntityIdNested>, Serializable {

        @RId
        private TestREntity name;
        private String value;

        public TestREntityIdNested(TestREntity name) {
            this.name = name;
        }

        public TestREntityIdNested(TestREntity name, String value) {
            super();
            this.name = name;
            this.value = value;
        }

        public TestREntity getName() {
            return name;
        }

        public String getValue() {
            return value;
        }

        public void setName(TestREntity name) {
            this.name = name;
        }

        public void setValue(String value) {
            this.value = value;
        }

        @Override
        public int compareTo(TestREntityIdNested o) {
            int res = name.compareTo(o.name);
            if (res == 0 || value != null || o.value != null) {
                return value.compareTo(o.value);
            }
            return res;
        }
    }

    @REntity
    public static class TestREntityValueNested implements Comparable<TestREntityValueNested>, Serializable {

        @RId
        private String name;
        private TestREntityWithRMap value;

        public TestREntityValueNested(String name) {
            this.name = name;
        }

        public TestREntityValueNested(String name, TestREntityWithRMap value) {
            super();
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public TestREntityWithRMap getValue() {
            return value;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setValue(TestREntityWithRMap value) {
            this.value = value;
        }

        @Override
        public int compareTo(TestREntityValueNested o) {
            int res = name.compareTo(o.name);
            if (res == 0 || value != null || o.value != null) {
                return value.compareTo(o.value);
            }
            return res;
        }
    }

    @Test
    public void testBasics() {
        RLiveObjectService s = redisson.getLiveObjectService();
        TestREntity t = new TestREntity("1");
        t = s.persist(t);
        assertEquals("1", t.getName());
        
        assertTrue(redisson.getMap(DefaultNamingScheme.INSTANCE.getName(TestREntity.class, String.class, "name", "1")).isExists());
        t.setName("3333");
        
        assertEquals("3333", t.getName());
        assertTrue(redisson.getMap(DefaultNamingScheme.INSTANCE.getName(TestREntity.class, String.class, "name", "3333")).isExists());
        t.setValue("111");
        assertEquals("111", t.getValue());
        assertTrue(redisson.getMap(DefaultNamingScheme.INSTANCE.getName(TestREntity.class, String.class, "name", "3333")).isExists());
        assertTrue(!redisson.getMap(DefaultNamingScheme.INSTANCE.getName(TestREntity.class, String.class, "name", "1")).isExists());
        assertEquals("111", redisson.getMap(DefaultNamingScheme.INSTANCE.getName(TestREntity.class, String.class, "name", "3333")).get("value"));
        
//        ((RLiveObject) t).getLiveObjectLiveMap().put("value", "555");
//        assertEquals("555", redisson.getMap(REntity.DefaultNamingScheme.INSTANCE.getName(TestREntity.class, "name", "3333")).get("value"));
//        assertEquals("3333", ((RObject) t).getName());//field access takes priority over the implemented interface.
    }

    @Test
    public void testLiveObjectWithCollection() {
        RLiveObjectService s = redisson.getLiveObjectService();
        TestREntityWithMap t = new TestREntityWithMap("2");
        t = s.persist(t);
        RMap<String, String> map = redisson.<String, String>getMap("testMap");
        t.setValue(map);
        map.put("field", "123");
        
        TestREntityWithMap t2 = s.get(TestREntityWithMap.class, "2");
        
        assertEquals("123", t2.getValue().get("field"));
        
        TestREntityWithMap t3 = s.get(TestREntityWithMap.class, "2");
        t3.getValue().put("field", "333");

        t3 = s.get(TestREntityWithMap.class, "2");
        assertEquals("333", t3.getValue().get("field"));
        
        HashMap<String, String> map2 = new HashMap<>();
        map2.put("field", "hello");
        t.setValue(map2);
        
        t3 = s.get(TestREntityWithMap.class, "2");
        assertEquals("hello", t3.getValue().get("field"));
    }

    @Test
    public void testLiveObjectWithRObject() {
        RLiveObjectService s = redisson.getLiveObjectService();
        TestREntityWithRMap t = new TestREntityWithRMap("2");
        t = s.persist(t);

        RMap<String, String> map = redisson.<String, String>getMap("testMap");
        t.setValue(map);
        map.put("field", "123");
        assertEquals("123",
                s.<TestREntityWithRMap, String>get(TestREntityWithRMap.class, "2")
                .getValue().get("field"));
        t = s.get(TestREntityWithRMap.class, "2");
        t.getValue().put("field", "333");
        assertEquals("333",
                s.<TestREntityWithRMap, String>get(TestREntityWithRMap.class, "2")
                .getValue().get("field"));
    }

    @Test
    public void testLiveObjectWithNestedLiveObjectAsId() {
        RLiveObjectService s = redisson.getLiveObjectService();
        TestREntity t1 = new TestREntity("1");
        t1 = s.persist(t1);
        
        try {
            s.persist(new TestREntityIdNested(t1));
            fail("Should not be here");
        } catch (Exception e) {
            assertEquals("Field with RId annotation cannot be a type of which class is annotated with REntity.", e.getMessage());
        }
    }

    @Test
    public void testLiveObjectWithNestedLiveObjectAsValue() throws Exception {
        RLiveObjectService s = redisson.getLiveObjectService();
        
        TestREntityWithRMap t1 = new TestREntityWithRMap("111");
        t1 = s.persist(t1);
        
        TestREntityValueNested t2 = new TestREntityValueNested("122");
        t2 = s.persist(t2);

        RMap<String, String> map = redisson.<String, String>getMap("32123");
        t2.setValue(t1);
        t2.getValue().setValue(map);
        map.put("field", "123");
        assertEquals("123",
                s.get(TestREntityWithRMap.class, "111")
                .getValue().get("field"));
        assertEquals("123",
                s.get(TestREntityValueNested.class, "122")
                .getValue().getValue().get("field"));
    }

    @REntity
    public static class TestClass {

        private String value;
        private String code;
        private Object content;

        @RId
        private Serializable id;

        public TestClass() {
        }
        
        public TestClass(Serializable id) {
            this.id = id;
        }

        public Serializable getId() {
            return id;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public Object getContent() {
            return content;
        }

        public void setContent(Object content) {
            this.content = content;
        }
        
        @RFieldAccessor
        public <T> void set(String field, T value) {
        }
        
        @RFieldAccessor
        public <T> T get(String field) {
            return null;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || !(obj instanceof TestClass) || !this.getClass().equals(obj.getClass())) {
                return false;
            }
            TestClass o = (TestClass) obj;
            return Objects.equals(this.id, o.id)
                    && Objects.equals(this.code, o.code)
                    && Objects.equals(this.value, o.value)
                    && Objects.equals(this.content, o.content);
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 23 * hash + Objects.hashCode(this.value);
            hash = 23 * hash + Objects.hashCode(this.code);
            hash = 23 * hash + Objects.hashCode(this.id);
            hash = 23 * hash + Objects.hashCode(this.content);
            return hash;
        }

    }

    public static class ObjectId implements Serializable {

        private int id;

        public ObjectId() {
        }

        public ObjectId(int id) {
            this.id = id;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof ObjectId)) {
                return false;
            }
            return id == ((ObjectId) obj).id;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 11 * hash + this.id;
            return hash;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        @Override
        public String toString() {
            return "" + id;
        }

    }

    @Test
    public void testSerializerable() {
        RLiveObjectService service = redisson.getLiveObjectService();
        TestClass t = new TestClass("55555");
        t = service.persist(t);
        assertTrue(Objects.equals("55555", t.getId()));

        t = new TestClass(90909l);
        t = service.persist(t);
        assertTrue(Objects.equals(90909l, t.getId()));

        t = new TestClass(90909);
        t = service.persist(t);
        assertTrue(Objects.equals(90909, t.getId()));

        t = new TestClass(new ObjectId(9090909));
        t = service.persist(t);
        assertTrue(Objects.equals(new ObjectId(9090909), t.getId()));

        t = new TestClass(new Byte("0"));
        t = service.persist(t);
        assertEquals(new Byte("0"), Byte.valueOf(t.getId().toString()));

        t = new TestClass((byte)90);
        assertEquals((byte) 90, Byte.parseByte(t.getId().toString()));

        t = new TestClass((Serializable)Arrays.asList(1, 2, 3, 4));
        t = service.persist(t);
        List<Integer> l = new ArrayList();
        l.addAll(Arrays.asList(1, 2, 3, 4));
        assertTrue(l.removeAll((List) t.getId()));
        assertTrue(l.isEmpty());

        try {
            t = new TestClass(new int[]{1, 2, 3, 4, 5});
            t = service.persist(t);
            fail("Should not be here");
        } catch (Exception e) {
            assertEquals("RId value cannot be an array.", e.getMessage());
        }

        try {
            t = new TestClass(new byte[]{1, 2, 3, 4, 5});
            t = service.persist(t);
            fail("Should not be here");
        } catch (Exception e) {
            assertEquals("RId value cannot be an array.", e.getMessage());
        }
    }

    @Test
    public void testPersist() {
        RLiveObjectService service = redisson.getLiveObjectService();
        TestClass ts = new TestClass(new ObjectId(100));
        ts.setValue("VALUE");
        TestClass persisted = service.persist(ts);
        assertEquals(new ObjectId(100), persisted.getId());
        assertEquals("VALUE", persisted.getValue());
        try {
            service.persist(ts);
            fail("Should not be here");
        } catch (Exception e) {
            assertEquals("This REntity already exists.", e.getMessage());
        }
    }

    @Test
    public void testMerge() {
        RLiveObjectService service = redisson.getLiveObjectService();
        TestClass ts = new TestClass(new ObjectId(100));
        ts.setValue("VALUE");
        TestClass merged = service.merge(ts);
        assertEquals(new ObjectId(100), merged.getId());
        assertEquals("VALUE", merged.getValue());
        try {
            service.persist(ts);
        } catch (Exception e) {
            assertEquals("This REntity already exists.", e.getMessage());
        }
        ts = new TestClass(new ObjectId(100));
        ts.setCode("CODE");
        merged = service.merge(ts);
        assertNull(ts.getValue());
        assertEquals("VALUE", merged.getValue());
        assertEquals("CODE", merged.getCode());
    }

    @Test
    public void testDetach() {
        RLiveObjectService service = redisson.getLiveObjectService();
        TestClass ts = new TestClass(new ObjectId(100));
        ts.setValue("VALUE");
        ts.setCode("CODE");
        TestClass merged = service.merge(ts);
        assertEquals("VALUE", merged.getValue());
        assertEquals("CODE", merged.getCode());
        TestClass detach = service.detach(merged);
        assertEquals(ts, detach);
    }

    @Test
    public void testIsPhantom() {
        RLiveObjectService service = redisson.getLiveObjectService();
        assertFalse(service.isExists(new Object()));
        TestClass ts = new TestClass(new ObjectId(100));
        assertFalse(service.isExists(service.get(TestClass.class, new ObjectId(100))));
        assertFalse(service.isExists(ts));
        ts.setValue("VALUE");
        ts.setCode("CODE");
        TestClass persisted = service.persist(ts);
        assertTrue(service.isExists(service.get(TestClass.class, new ObjectId(100))));
        assertFalse(service.isExists(ts));
        assertTrue(service.isExists(persisted));
    }

    @Test
    public void testIsLiveObject() {
        RLiveObjectService service = redisson.getLiveObjectService();
        TestClass ts = new TestClass(new ObjectId(100));
        assertFalse(service.isLiveObject(ts));
        TestClass persisted = service.persist(ts);
        assertFalse(service.isLiveObject(ts));
        assertTrue(service.isLiveObject(persisted));
    }

    @Test
    public void testAsLiveObject() {
        RLiveObjectService service = redisson.getLiveObjectService();
        TestClass instance = new TestClass(new ObjectId(100));
        instance = service.persist(instance);
        
        RLiveObject liveObject = service.asLiveObject(instance);
        assertEquals(new ObjectId(100), liveObject.getLiveObjectId());
        try {
            service.asLiveObject(new Object());
            fail("Should not be here");
        } catch (Exception e) {
            assertTrue(e instanceof ClassCastException);
        }
    }

    @Test
    public void testClassRegistration() {
        RLiveObjectService service = redisson.getLiveObjectService();
        service.registerClass(TestClass.class);
        assertTrue(service.isClassRegistered(TestClass.class));
        RLiveObjectService newService = redisson.getLiveObjectService();
        assertTrue(newService.isClassRegistered(TestClass.class));
        RedissonClient newRedisson = Redisson.create(redisson.getConfig());
        assertFalse(newRedisson.getLiveObjectService().isClassRegistered(TestClass.class));
        newRedisson.shutdown(1, 5, TimeUnit.SECONDS);
    }

    @Test
    public void testClassUnRegistration() {
        RLiveObjectService service = redisson.getLiveObjectService();
        service.registerClass(TestClass.class);
        assertTrue(service.isClassRegistered(TestClass.class));
        RLiveObjectService newService = redisson.getLiveObjectService();
        RedissonClient newRedisson = Redisson.create(redisson.getConfig());
        newRedisson.getLiveObjectService().registerClass(TestClass.class);
        newService.unregisterClass(TestClass.class);
        assertFalse(service.isClassRegistered(TestClass.class));
        assertFalse(newService.isClassRegistered(TestClass.class));
        assertTrue(newRedisson.getLiveObjectService().isClassRegistered(TestClass.class));
        assertFalse(service.isClassRegistered(TestClass.class));
        assertFalse(newService.isClassRegistered(TestClass.class));
        newRedisson.shutdown(1, 5, TimeUnit.SECONDS);
    }

    @Test
    public void testGet() {
        RLiveObjectService service = redisson.getLiveObjectService();
        assertNull(service.get(TestClass.class, new ObjectId(100)));
        TestClass ts = new TestClass(new ObjectId(100));
        TestClass persisted = service.persist(ts);
        assertNotNull(service.get(TestClass.class, new ObjectId(100)));
        persisted.setCode("CODE");
        assertNotNull(service.get(TestClass.class, new ObjectId(100)));
    }

    @Test
    public void testRemoveByInstance() {
        RLiveObjectService service = redisson.getLiveObjectService();
        TestClass ts = new TestClass(new ObjectId(100));
        ts.setCode("CODE");
        TestClass persisted = service.persist(ts);
        assertTrue(service.isExists(persisted));
        service.delete(persisted);
        assertFalse(service.isExists(persisted));
    }

    @Test
    public void testRemoveById() {
        RLiveObjectService service = redisson.getLiveObjectService();
        TestClass ts = new TestClass(new ObjectId(100));
        ts.setCode("CODE");
        TestClass persisted = service.persist(ts);
        assertTrue(service.isExists(persisted));
        service.delete(TestClass.class, new ObjectId(100));
        assertFalse(service.isExists(persisted));
    }

    @REntity
    public static class TestClassID1 {

        @RId(generator = DistributedAtomicLongIdGenerator.class)
        private Long name;

        public TestClassID1() {
        }
        
        public TestClassID1(Long name) {
            this.name = name;
        }

        public Long getName() {
            return name;
        }

    }

    @REntity
    public static class TestClassID2 {

        @RId(generator = DistributedAtomicLongIdGenerator.class)
        private Long name;

        public TestClassID2() {
        }
        
        public TestClassID2(Long name) {
            this.name = name;
        }

        public Long getName() {
            return name;
        }

    }

    @Test
    public void testCreate() {
        RLiveObjectService service = redisson.getLiveObjectService();
        TestClass ts = new TestClass();
        ts = service.persist(ts);
        UUID uuid = UUID.fromString(ts.getId().toString());
        assertEquals(4, uuid.version());
        
        TestClassID1 tc1 = new TestClassID1();
        tc1 = service.persist(tc1);
        assertEquals(new Long(1), tc1.getName());
        TestClassID2 tc2 = new TestClassID2();
        tc2 = service.persist(tc2);
        assertEquals(new Long(1), tc2.getName());
    }

    @Test
    public void testTransformation() {
        RLiveObjectService service = redisson.getLiveObjectService();
        TestClass ts = new TestClass();
        ts = service.persist(ts);

        HashMap<String, String> m = new HashMap<>();
        ts.setContent(m);
        assertFalse(HashMap.class.isAssignableFrom(ts.getContent().getClass()));
        assertTrue(RMap.class.isAssignableFrom(ts.getContent().getClass()));

        HashSet<String> s = new HashSet<>();
        ts.setContent(s);
        assertFalse(HashSet.class.isAssignableFrom(ts.getContent().getClass()));
        assertTrue(RSet.class.isAssignableFrom(ts.getContent().getClass()));

        TreeSet<String> ss = new TreeSet<>();
        ts.setContent(ss);
        assertFalse(TreeSet.class.isAssignableFrom(ts.getContent().getClass()));
        assertTrue(RSortedSet.class.isAssignableFrom(ts.getContent().getClass()));

        ArrayList<String> al = new ArrayList<>();
        ts.setContent(al);
        assertFalse(ArrayList.class.isAssignableFrom(ts.getContent().getClass()));
        assertTrue(RList.class.isAssignableFrom(ts.getContent().getClass()));

        ConcurrentHashMap<String, String> chm = new ConcurrentHashMap<>();
        ts.setContent(chm);
        assertFalse(ConcurrentHashMap.class.isAssignableFrom(ts.getContent().getClass()));
        assertTrue(RMap.class.isAssignableFrom(ts.getContent().getClass()));

        ArrayBlockingQueue<String> abq = new ArrayBlockingQueue<>(10);
        ts.setContent(abq);
        assertFalse(ArrayBlockingQueue.class.isAssignableFrom(ts.getContent().getClass()));
        assertTrue(RBlockingQueue.class.isAssignableFrom(ts.getContent().getClass()));

        ConcurrentLinkedQueue<String> clq = new ConcurrentLinkedQueue<>();
        ts.setContent(clq);
        assertFalse(ConcurrentLinkedQueue.class.isAssignableFrom(ts.getContent().getClass()));
        assertTrue(RQueue.class.isAssignableFrom(ts.getContent().getClass()));

        LinkedBlockingDeque<String> lbdq = new LinkedBlockingDeque<>();
        ts.setContent(lbdq);
        assertFalse(LinkedBlockingDeque.class.isAssignableFrom(ts.getContent().getClass()));
        assertTrue(RBlockingDeque.class.isAssignableFrom(ts.getContent().getClass()));

        LinkedList<String> ll = new LinkedList<>();
        ts.setContent(ll);
        assertFalse(LinkedList.class.isAssignableFrom(ts.getContent().getClass()));
        assertTrue(RDeque.class.isAssignableFrom(ts.getContent().getClass()));

    }

    @REntity(fieldTransformation = REntity.TransformationMode.IMPLEMENTATION_BASED)
    public static class TestClassNoTransformation {

        private String value;
        private String code;
        private Object content;

        @RId
        private Serializable id;

        public TestClassNoTransformation() {
        }
        
        public TestClassNoTransformation(Serializable id) {
            this.id = id;
        }

        public Serializable getId() {
            return id;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public Object getContent() {
            return content;
        }

        public void setContent(Object content) {
            this.content = content;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || !(obj instanceof TestClass) || !this.getClass().equals(obj.getClass())) {
                return false;
            }
            TestClass o = (TestClass) obj;
            return Objects.equals(this.id, o.id)
                    && Objects.equals(this.code, o.code)
                    && Objects.equals(this.value, o.value)
                    && Objects.equals(this.content, o.content);
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 33 * hash + Objects.hashCode(this.value);
            hash = 33 * hash + Objects.hashCode(this.code);
            hash = 33 * hash + Objects.hashCode(this.id);
            hash = 33 * hash + Objects.hashCode(this.content);
            return hash;
        }

    }

    @Test
    public void testNoTransformation() {
        RLiveObjectService service = redisson.getLiveObjectService();
        TestClassNoTransformation ts = new TestClassNoTransformation();
        ts = service.persist(ts);

        HashMap<String, String> m = new HashMap<>();
        ts.setContent(m);
        assertTrue(HashMap.class.isAssignableFrom(ts.getContent().getClass()));
        assertFalse(RMap.class.isAssignableFrom(ts.getContent().getClass()));

        HashSet<String> s = new HashSet<>();
        ts.setContent(s);
        assertTrue(HashSet.class.isAssignableFrom(ts.getContent().getClass()));
        assertFalse(RSet.class.isAssignableFrom(ts.getContent().getClass()));

        TreeSet<String> ss = new TreeSet<>();
        ts.setContent(ss);
        assertTrue(TreeSet.class.isAssignableFrom(ts.getContent().getClass()));
        assertFalse(RSortedSet.class.isAssignableFrom(ts.getContent().getClass()));

        ArrayList<String> al = new ArrayList<>();
        ts.setContent(al);
        assertTrue(ArrayList.class.isAssignableFrom(ts.getContent().getClass()));
        assertFalse(RList.class.isAssignableFrom(ts.getContent().getClass()));

        ConcurrentHashMap<String, String> chm = new ConcurrentHashMap<>();
        ts.setContent(chm);
        assertTrue(ConcurrentHashMap.class.isAssignableFrom(ts.getContent().getClass()));
        assertFalse(RMap.class.isAssignableFrom(ts.getContent().getClass()));

        ArrayBlockingQueue<String> abq = new ArrayBlockingQueue<>(10);
        abq.add("111");
        ts.setContent(abq);
        assertTrue(ArrayBlockingQueue.class.isAssignableFrom(ts.getContent().getClass()));
        assertFalse(RBlockingQueue.class.isAssignableFrom(ts.getContent().getClass()));

        ConcurrentLinkedQueue<String> clq = new ConcurrentLinkedQueue<>();
        ts.setContent(clq);
        assertTrue(ConcurrentLinkedQueue.class.isAssignableFrom(ts.getContent().getClass()));
        assertFalse(RQueue.class.isAssignableFrom(ts.getContent().getClass()));

        LinkedBlockingDeque<String> lbdq = new LinkedBlockingDeque<>();
        ts.setContent(lbdq);
        assertTrue(LinkedBlockingDeque.class.isAssignableFrom(ts.getContent().getClass()));
        assertFalse(RBlockingDeque.class.isAssignableFrom(ts.getContent().getClass()));

        LinkedList<String> ll = new LinkedList<>();
        ts.setContent(ll);
        assertTrue(LinkedList.class.isAssignableFrom(ts.getContent().getClass()));
        assertFalse(RDeque.class.isAssignableFrom(ts.getContent().getClass()));

    }

    @REntity
    public static class MyObject implements Serializable {

        @RId(generator = DistributedAtomicLongIdGenerator.class)
        private Long id;

        private Long myId;
        private String name;

        public MyObject() {
        }

        public MyObject(Long myId) {
            super();
            this.myId = myId;
        }

        public Long getMyId() {
            return myId;
        }

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

    }

    @Test
    public void test() {
        RLiveObjectService service = redisson.getLiveObjectService();

        MyObject object = new MyObject(20L);
        try {
            service.attach(object);
        } catch (Exception e) {
            assertEquals("Non-null value is required for the field with RId annotation.", e.getMessage());
        }
    }

    @Test
    public void testExpirable() throws InterruptedException {
        RLiveObjectService service = redisson.getLiveObjectService();
        TestClass myObject = new TestClass();
        myObject = service.persist(myObject);
        myObject.setValue("123345");
        assertTrue(service.asLiveObject(myObject).isExists());
        service.asRExpirable(myObject).expire(1, TimeUnit.SECONDS);
        Thread.sleep(2000);
        assertFalse(service.asLiveObject(myObject).isExists());
    }

    @Test
    public void testMap() {
        RLiveObjectService service = redisson.getLiveObjectService();
        TestClass myObject = new TestClass();
        myObject = service.persist(myObject);

        myObject.setValue("123345");
        assertEquals("123345", service.asRMap(myObject).get("value"));
        service.asRMap(myObject).put("value", "9999");
        assertEquals("9999", myObject.getValue());
    }
    
    @Test
    public void testRObject() {
        RLiveObjectService service = redisson.getLiveObjectService();
        TestClass myObject = new TestClass();
        myObject = service.persist(myObject);
        try {
            ((RObject) myObject).isExists();
        } catch (Exception e) {
            assertEquals("Please use RLiveObjectService instance for this type of functions", e.getMessage());
        }
    }
    
    @REntity
    public static class SimpleObject {
        
        @RId
        private String id;
        
        private Long value;
        
        public String getId() {
            return id;
        }
        
        public Long getValue() {
            return value;
        }
        
        public void setValue(Long value) {
            this.value = value;
        }
        
    }
    
    @REntity
    public static class ObjectWithList {
        
        @RId
        private String id;
        
        private List<SimpleObject> objects;
        
        private SimpleObject so;
        
        public String getId() {
            return id;
        }
        
        public List<SimpleObject> getObjects() {
            return objects;
        }
        
        public void setSo(SimpleObject so) {
            this.so = so;
        }
        
        public SimpleObject getSo() {
            return so;
        }
        
    }

    @Test
    public void testStoreInnerObject() {
        RLiveObjectService service = redisson.getLiveObjectService();
        ObjectWithList so = new ObjectWithList();
        so = service.persist(so);

        SimpleObject s = new SimpleObject();
        s = service.persist(s);
        
        so.setSo(s);
        assertThat(s.getId()).isNotNull();
        so.getObjects().add(s);
        
        so = redisson.getLiveObjectService().detach(so);
        assertThat(so.getSo().getId()).isEqualTo(s.getId());
        assertThat(so.getObjects().get(0).getId()).isEqualTo(so.getSo().getId());
    }
    
    @Test
    public void testFieldWithoutIdSetter() {
        RLiveObjectService service = redisson.getLiveObjectService();
        SimpleObject so = new SimpleObject();
        so = service.persist(so);
        so.setValue(10L);

        so = redisson.getLiveObjectService().detach(so);
        assertThat(so.getId()).isNotNull();
        assertThat(so.getValue()).isEqualTo(10L);
        
        so = redisson.getLiveObjectService().get(SimpleObject.class, so.getId());
        assertThat(so.getId()).isNotNull();
        assertThat(so.getValue()).isEqualTo(10L);
    }
    
    @Test
    public void testCreateObjectsInRuntime() {
        RLiveObjectService service = redisson.getLiveObjectService();
        
        TestREntityWithMap so = new TestREntityWithMap();
        so = service.persist(so);
        
        so.getValue().put("1", "2");
        
        so = redisson.getLiveObjectService().detach(so);
        assertThat(so.getName()).isNotNull();
        assertThat(so.getValue()).containsKey("1");
        assertThat(so.getValue()).containsValue("2");
        
        so = redisson.getLiveObjectService().get(TestREntityWithMap.class, so.getName());
        assertThat(so.getName()).isNotNull();
        assertThat(so.getValue()).containsKey("1");
        assertThat(so.getValue()).containsValue("2");
    }
    
    @Test
    public void testFieldAccessor() {
        RLiveObjectService service = redisson.getLiveObjectService();
        TestClass myObject = new TestClass();
        myObject = service.persist(myObject);

        myObject.setValue("123345");
        assertEquals("123345", myObject.get("value"));
        myObject.set("value", "9999");
        assertEquals("9999", myObject.get("value"));
        assertEquals("9999", myObject.getValue());
        try {
            myObject.get("555555");
        } catch (Exception e) {
            assertTrue(e instanceof NoSuchFieldException);
        }
        try {
            myObject.set("555555", "999");
        } catch (Exception e) {
            assertTrue(e instanceof NoSuchFieldException);
        }
    }

}
