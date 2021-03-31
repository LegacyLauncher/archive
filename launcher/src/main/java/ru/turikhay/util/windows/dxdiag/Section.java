package ru.turikhay.util.windows.dxdiag;

import org.jdom2.Element;

class Section {
    private final Element elem;

    public Section(Element elem) {
        this.elem = elem;
    }

    public Section(Element rootElem, String key) {
        this(rootElem.getChild(key));
    }

    public String get(String childName) {
        if (elem == null) {
            return null;
        }
        Element child = elem.getChild(childName);
        return child == null ? null : child.getValue();
    }

    public boolean isEmpty() {
        return elem == null;
    }


    @Override
    public String toString() {
        return "Section{" +
                "elem=" + elem +
                '}';
    }
}
