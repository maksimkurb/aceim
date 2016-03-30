package ru.cubly.aceim.app.core;

import java.util.Collection;

public interface BasicListAdapter<E>  {

    public boolean add(E object);

    public boolean addAll(Collection<? extends E> collection);

    public void clear();

    public boolean contains(Object object);

    public boolean isEmpty();

    public boolean remove(Object object);
    public boolean remove(int position);

    public boolean removeAll(Collection<?> collection);

    public int getItemCount();
}

