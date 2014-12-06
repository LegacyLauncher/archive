package ru.turikhay.tlauncher.ui.swing;

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;

public class SimpleComboBoxModel<E> extends DefaultComboBoxModel<E> {
	private static final long serialVersionUID = 5950434966721171811L;

	protected Vector<E> objects;
	protected Object selectedObject;

	public SimpleComboBoxModel() {
		objects = new Vector<E>();
	}

	public SimpleComboBoxModel(E items[]) {
		objects = new Vector<E>(items.length);

		int i,c;
		for(i=0,c=items.length;i<c;i++)
			objects.addElement(items[i]);

		if(getSize() > 0)
			selectedObject = getElementAt( 0 );
	}

	public SimpleComboBoxModel(Vector<E> v) {
		objects = v;

		if (getSize() > 0)
			selectedObject = getElementAt(0);
	}

	@Override
	public void setSelectedItem(Object anObject) {
		if(
				(selectedObject != null && !selectedObject.equals( anObject )) // anObject is not selected already
				|| // or
				selectedObject == null // Selected object is NULL
				&& // and
				anObject != null // anObject is not NULL
				){
			selectedObject = anObject;
			fireContentsChanged(this, -1, -1);
		}
	}

	@Override
	public Object getSelectedItem() {
		return selectedObject;
	}

	@Override
	public int getSize() {
		return objects.size();
	}

	@Override
	public E getElementAt(int index) {
		if ( index >= 0 && index < objects.size() )
			return objects.elementAt(index);
		return null;
	}

	@Override
	public int getIndexOf(Object anObject) {
		return objects.indexOf(anObject);
	}

	@Override
	public void addElement(E anObject) {
		objects.addElement(anObject);

		int size = objects.size(), index = objects.size() - 1;
		fireIntervalAdded(this, index, index);

		if(size == 1 && selectedObject == null && anObject != null)
			setSelectedItem(anObject);
	}

	public void addElements(Collection<E> list) {
		if(list.size() == 0)
			return;

		int
		size = list.size(),
		index0 = objects.size(),
		index1 = index0 + size - 1;

		objects.addAll(list);
		fireIntervalAdded(this, index0, index1);

		if(selectedObject == null) {
			// Take care of selection
			Iterator<E> iterator = list.iterator();
			E elem;

			while(iterator.hasNext()) {
				elem = iterator.next();

				if(elem == null)
					continue;

				setSelectedItem(elem);
				break;
			}
		}
	}

	@Override
	public void insertElementAt(E anObject,int index) {
		objects.insertElementAt(anObject,index);
		fireIntervalAdded(this, index, index);
	}

	@Override
	public void removeElementAt(int index) {
		if (getElementAt(index) == selectedObject) {
			// Take care if this object is selected
			if (index == 0) {
				setSelectedItem(getSize() == 1? null : getElementAt(index + 1));
			} else {
				setSelectedItem(getElementAt(index - 1));
			}
		}

		objects.removeElementAt(index);
		fireIntervalRemoved(this, index, index);
	}

	@Override
	public void removeElement(Object anObject) {
		int index = objects.indexOf(anObject);

		if(index != -1)
			removeElementAt(index);
	}

	@Override
	public void removeAllElements() {
		int size = objects.size();

		if (size > 0) {
			int firstIndex = 0;
			int lastIndex = size - 1;

			objects.removeAllElements();

			selectedObject = null;
			fireIntervalRemoved(this, firstIndex, lastIndex);
		} else {
			selectedObject = null;
		}
	}
}
