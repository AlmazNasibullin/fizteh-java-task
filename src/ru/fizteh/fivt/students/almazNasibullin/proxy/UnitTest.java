package ru.fizteh.fivt.students.almazNasibullin.proxy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import ru.fizteh.fivt.proxy.Collect;
import ru.fizteh.fivt.proxy.DoNotProxy;

/**
 * 1.12.12
 * @author almaz
 */

public class UnitTest {

    @Test(expected = IllegalArgumentException.class)
    public void testNullPointerTargets() {
        new ShardingProxyFactory().createProxy(null, new Class[2]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullPointerInterfaces() {
        new ShardingProxyFactory().createProxy(new Object[2], null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyTargets() {
        new ShardingProxyFactory().createProxy(new Object[0], new Class[2]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyInterfaces() {
        new ShardingProxyFactory().createProxy(new Object[2], new Class[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullpointerTarget() {
        Class[] interfaces = new Class[1];
        interfaces[0] = Cloneable.class;
        new ShardingProxyFactory().createProxy(new Object[2], interfaces);
    }

    class TestClassBadInterface  {
        // implements no interface
        @Collect
        public void doSmth() {
            System.out.println("Doing smth");
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTargetImplementsNoInterface() {
        Object[] targets = new Object[1];
        targets[0] = new TestClassBadInterface();
        Class[] interfaces = new Class[1];
        interfaces[0] = Cloneable.class;
        new ShardingProxyFactory().createProxy(targets, interfaces);
    }

    interface MyInterface {
        @DoNotProxy
        public int getNumber(int number);

        @Collect
        public void print(String s);

        public Long getCount(Long count);

        public int sum(int num1, int num2);

        public Long multiplication(Long num1, Long num2);

        @Collect
        public int getDay();

        @Collect
        public void addToList(List<Integer> l);

        @Collect
        public void throwRuntimeException();

        @Collect
        public void throwIllegalStateException();
    }

    class MyTestClass implements MyInterface {
        @Override
        public int getNumber(int number) {
            return number;
        }

        @Override
        public void print(String s) {
            System.out.println(s);
        }

        @Override
        public Long getCount(Long count) {
            return -1L;
        }

        @Override
        public int sum(int num1, int num2) {
            return num1 + num2;
        }

        @Override
        public Long multiplication(Long num1, Long num2) {
            return num1 * num2;
        }

        @Override
        public int getDay() {
            return 1;
        }

        @Override
        public void addToList(List<Integer> l) {
            l.add(1);
        }

        @Override
        public void throwRuntimeException() {
            throw new RuntimeException("RuntimeException");
        }

        @Override
        public void throwIllegalStateException() {
            throw new IllegalStateException("IllegalStateException");
        }

    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testNullpointerInterface() {
        Object[] targets = new Object[1];
        targets[0] = new MyTestClass();
        Class[] interfaces = new Class[2];
        interfaces[0] = MyInterface.class;
        new ShardingProxyFactory().createProxy(targets, interfaces);
    }

    @Test(expected = IllegalStateException.class)
    public void  testDoNotProxyMethod() {
        Object[] targets = new Object[1];
        targets[0] = new MyTestClass();
        Class[] interfaces = new Class[1];
        interfaces[0] = MyInterface.class;
        ShardingProxyFactory spf = new ShardingProxyFactory();
        MyInterface mi = (MyInterface)spf.createProxy(targets, interfaces);
        mi.getNumber(-1);
    }

    @Test
    public void  testShardingProxyFactory() {
        Object[] targets = new Object[3];
        targets[0] = new MyTestClass();
        targets[1] = new MyTestClass();
        targets[2] = new MyTestClass();
        Class[] interfaces = new Class[1];
        interfaces[0] = MyInterface.class;
        ShardingProxyFactory spf = new ShardingProxyFactory();
        MyInterface mi = (MyInterface)spf.createProxy(targets, interfaces);
        Assert.assertTrue(mi.getCount(0L) == -1L);
        Assert.assertEquals(mi.sum(2, 5), 7);
        Assert.assertTrue(mi.multiplication(1L, 10L) == 10L);
        // сливаем результаты, getDay возвращает 1, targets.length == 3
        Assert.assertEquals(mi.getDay(), 3);
        List<Integer> l = new ArrayList<Integer>();
        mi.addToList(l);
        Assert.assertEquals(l.size(), 3);
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testThrowRuntimeException() {
        thrown.expect(RuntimeException.class);
        thrown.expectMessage("RuntimeException");
        Object[] targets = new Object[1];
        targets[0] = new MyTestClass();
        Class[] interfaces = new Class[1];
        interfaces[0] = MyInterface.class;
        ShardingProxyFactory spf = new ShardingProxyFactory();
        MyInterface mi = (MyInterface)spf.createProxy(targets, interfaces);
        mi.throwRuntimeException();
    }

    @Test
    public void testThrowIllegalStateException() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("IllegalStateException");
        Object[] targets = new Object[1];
        targets[0] = new MyTestClass();
        Class[] interfaces = new Class[1];
        interfaces[0] = MyInterface.class;
        ShardingProxyFactory spf = new ShardingProxyFactory();
        MyInterface mi = (MyInterface)spf.createProxy(targets, interfaces);
        mi.throwIllegalStateException();
    }
    
    interface BadCollectShardingInterface {
        @Collect
        Map collect(int a);
    }

    class BadCollectShardingClass implements BadCollectShardingInterface {
        @Override
        public Map collect(int a) {
            return null;
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testBadAnnotation() {
        Object[] targets = new Object[1];
        targets[0] = new BadCollectShardingClass();
        Class[] interfaces = new Class[1];
        interfaces[0] = BadCollectShardingInterface.class;
        ShardingProxyFactory spf = new ShardingProxyFactory();
        BadCollectShardingInterface bsi =
                (BadCollectShardingInterface)spf.createProxy(targets, interfaces);
    }
}
