package com.zelaux.betterdiagram.struct;

import java.util.Collection;

public class CounterBlackHoleList<T> extends BlackHoleList<T> {
    @SuppressWarnings("rawtypes")
    public static final CounterBlackHoleList instance=new CounterBlackHoleList();

    public static <T> CounterBlackHoleList<T> typedWithZero(){
        instance.counter=0;
        //noinspection unchecked
        return instance;
    }
    public int counter=0;
    @Override
    public boolean add(T t) {
        counter++;
        return super.add(t);
    }

    @Override
    public void add(int index, T element) {
        counter++;
        super.add(index, element);
    }

    @Override
    public void addFirst(T element) {
        counter++;
        super.addFirst(element);
    }

    @Override
    public void addLast(T element) {
        counter++;
        super.addLast(element);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        counter+=c.size();
        return super.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        counter+=c.size();
        return super.addAll(index, c);
    }
}
