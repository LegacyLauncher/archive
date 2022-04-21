package ru.turikhay.tlauncher.ui.editor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class EditorGroupHandler {
    private final List<EditorFieldChangeListener> listeners;
    private final int checkedLimit;
    private int changedFlag;
    private int checkedFlag;

    public EditorGroupHandler(List<? extends EditorHandler> handlers) {
        if (handlers == null) {
            throw new NullPointerException();
        } else {
            checkedLimit = handlers.size();
            EditorFieldListener listener = new EditorFieldListener() {
                protected void onChange(EditorHandler handler, String oldValue, String newValue) {
                    if (newValue != null) {
                        if (!newValue.equals(oldValue)) {
                            changedFlag = changedFlag + 1;
                        }

                        checkedFlag = checkedFlag + 1;
                        if (checkedFlag == checkedLimit) {
                            if (changedFlag > 0) {

                                for (EditorFieldChangeListener listener : listeners) {
                                    listener.onChange(null, null);
                                }
                            }

                            EditorGroupHandler var10000 = EditorGroupHandler.this;
                            changedFlag = 0;
                            var10000.checkedFlag = 0;
                        }

                    }
                }
            };

            for (int i = 0; i < handlers.size(); ++i) {
                EditorHandler handler = handlers.get(i);
                if (handler == null) {
                    throw new NullPointerException("Handler is NULL at " + i);
                }

                handler.addListener(listener);
            }

            listeners = Collections.synchronizedList(new ArrayList<>());
        }
    }

    public EditorGroupHandler(EditorHandler... handlers) {
        this(Arrays.asList(handlers));
    }

    public boolean addListener(EditorFieldChangeListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        } else {
            return listeners.add(listener);
        }
    }

    public boolean removeListener(EditorFieldChangeListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        } else {
            return listeners.remove(listener);
        }
    }
}
