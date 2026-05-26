package com.zelaux.betterdiagram.struct;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Predicate;

public class BlackHoleList<T> extends ArrayList<T> {
    @SuppressWarnings("rawtypes")
    public static final BlackHoleList instance = new BlackHoleList();

    public static <T> BlackHoleList<T> typed() {
        //noinspection unchecked
        return instance;
    }

    public BlackHoleList() {
        super(0);
    }


    @Override
    public boolean add(T t) {
        return true;
    }

    @Override
    public void add(int index, T element) {
    }

    @Override
    public void addFirst(T element) {
    }

    @Override
    public void addLast(T element) {
    }

    @Override
    public T remove(int index) {
        return null;
    }

    @Override
    public T removeFirst() {
        return null;
    }

    @Override
    public T removeLast() {
        return null;
    }

    @Override
    public boolean remove(Object o) {
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return false;
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return false;
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex) {
    }


    @Override
    public boolean removeIf(Predicate<? super T> filter) {
        return false;
    }
}
