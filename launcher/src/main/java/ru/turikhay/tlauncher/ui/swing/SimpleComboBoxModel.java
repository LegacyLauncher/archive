package ru.turikhay.tlauncher.ui.swing;

import javax.swing.*;
import java.util.Collection;
import java.util.Vector;

public class SimpleComboBoxModel<E> extends DefaultComboBoxModel<E> {
    private static final long serialVersionUID = 5950434966721171811L;
    protected final Vector<E> objects;
    protected Object selectedObject;

    public SimpleComboBoxModel() {
        objects = new Vector<>();
    }

    public SimpleComboBoxModel(E[] items) {
        objects = new Vector<>(items.length);
        int i = 0;

        for (int c = items.length; i < c; ++i) {
            objects.addElement(items[i]);
        }

        if (getSize() > 0) {
            selectedObject = getElementAt(0);
        }

    }

    public SimpleComboBoxModel(Vector<E> v) {
        objects = v;
        if (getSize() > 0) {
            selectedObject = getElementAt(0);
        }

    }

    public void setSelectedItem(Object anObject) {
        if (selectedObject != null && !selectedObject.equals(anObject) || selectedObject == null && anObject != null) {
            selectedObject = anObject;
            fireContentsChanged(this, -1, -1);
        }

    }

    public Object getSelectedItem() {
        return selectedObject;
    }

    public int getSize() {
        return objects.size();
    }

    public E getElementAt(int index) {
        return index >= 0 && index < objects.size() ? objects.elementAt(index) : null;
    }

    public int getIndexOf(Object anObject) {
        return objects.indexOf(anObject);
    }

    public void addElement(E anObject) {
        objects.addElement(anObject);
        int size = objects.size();
        int index = objects.size() - 1;
        fireIntervalAdded(this, index, index);
        if (size == 1 && selectedObject == null && anObject != null) {
            setSelectedItem(anObject);
        }

    }

    public void addElements(Collection<E> list) {
        if (list.size() != 0) {
            int size = list.size();
            int index0 = objects.size();
            int index1 = index0 + size - 1;
            objects.addAll(list);
            fireIntervalAdded(this, index0, index1);
            if (selectedObject == null) {

                for (E elem : list) {
                    if (elem != null) {
                        setSelectedItem(elem);
                        break;
                    }
                }
            }

        }
    }

    public void insertElementAt(E anObject, int index) {
        objects.insertElementAt(anObject, index);
        fireIntervalAdded(this, index, index);
    }

    public void removeElementAt(int index) {
        if (getElementAt(index) == selectedObject) {
            if (index == 0) {
                setSelectedItem(getSize() == 1 ? null : getElementAt(index + 1));
            } else {
                setSelectedItem(getElementAt(index - 1));
            }
        }

        objects.removeElementAt(index);
        fireIntervalRemoved(this, index, index);
    }

    public void removeElement(Object anObject) {
        int index = objects.indexOf(anObject);
        if (index != -1) {
            removeElementAt(index);
        }

    }

    public void removeAllElements() {
        int size = objects.size();
        if (size > 0) {
            byte firstIndex = 0;
            int lastIndex = size - 1;
            objects.removeAllElements();
            selectedObject = null;
            fireIntervalRemoved(this, firstIndex, lastIndex);
        } else {
            selectedObject = null;
        }

    }
}
