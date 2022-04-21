package ru.turikhay.tlauncher.ui.swing;

import javax.swing.*;
import java.util.Collection;
import java.util.Vector;

public class SimpleListModel<E> extends AbstractListModel<E> {
    private static final long serialVersionUID = 727845864028652893L;
    protected final Vector<E> vector = new Vector<>();

    public int getSize() {
        return vector.size();
    }

    public E getElementAt(int index) {
        return index >= 0 && index < getSize() ? vector.get(index) : null;
    }

    public void add(E elem) {
        int index = vector.size();
        vector.add(elem);
        fireIntervalAdded(this, index, index);
    }

    public boolean remove(E elem) {
        int index = indexOf(elem);
        boolean rv = vector.removeElement(elem);
        if (index >= 0) {
            fireIntervalRemoved(this, index, index);
        }

        return rv;
    }

    public void addAll(Collection<E> elem) {
        int size = elem.size();
        if (size != 0) {
            int index0 = vector.size();
            int index1 = index0 + size - 1;
            vector.addAll(elem);
            fireIntervalAdded(this, index0, index1);
        }
    }

    public void clear() {
        int index1 = vector.size() - 1;
        vector.clear();
        if (index1 >= 0) {
            fireIntervalRemoved(this, 0, index1);
        }

    }

    public boolean isEmpty() {
        return vector.isEmpty();
    }

    public boolean contains(E elem) {
        return vector.contains(elem);
    }

    public int indexOf(E elem) {
        return vector.indexOf(elem);
    }

    public int indexOf(E elem, int index) {
        return vector.indexOf(elem, index);
    }

    public E elementAt(int index) {
        return vector.elementAt(index);
    }

    public String toString() {
        return vector.toString();
    }
}
