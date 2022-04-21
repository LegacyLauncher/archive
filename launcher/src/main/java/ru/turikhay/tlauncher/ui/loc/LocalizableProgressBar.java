package ru.turikhay.tlauncher.ui.loc;

import ru.turikhay.tlauncher.ui.progress.ProgressBar;

import java.awt.*;

public class LocalizableProgressBar extends ProgressBar implements LocalizableComponent {
    private static final long serialVersionUID = 7393243528402135898L;
    private String westPath;
    private String centerPath;
    private String eastPath;
    private String[] westVars;
    private String[] centerVars;
    private String[] eastVars;

    protected LocalizableProgressBar(Component parentComp) {
        super(parentComp);
    }

    protected LocalizableProgressBar() {
        this(null);
    }

    public void setWestString(String path, boolean update, Object... vars) {
        westPath = path;
        westVars = Localizable.checkVariables(vars);
        super.setWestString(Localizable.get(westPath, (Object[]) westVars), update);
    }

    public void setWestString(String path, boolean update) {
        setWestString(path, update, Localizable.EMPTY_VARS);
    }

    public void setWestString(String path, Object... vars) {
        setWestString(path, true, vars);
    }

    public void setCenterString(String path, boolean update, Object... vars) {
        centerPath = path;
        centerVars = Localizable.checkVariables(vars);
        super.setCenterString(Localizable.get(centerPath, (Object[]) centerVars), update);
    }

    public void setCenterString(String path, boolean update) {
        setCenterString(path, update, Localizable.EMPTY_VARS);
    }

    public void setCenterString(String path, Object... vars) {
        setCenterString(path, true, vars);
    }

    public void setEastString(String path, boolean update, Object... vars) {
        eastPath = path;
        eastVars = Localizable.checkVariables(vars);
        super.setEastString(Localizable.get(eastPath, (Object[]) eastVars), update);
    }

    public void setEastString(String path, boolean update) {
        setEastString(path, update, Localizable.EMPTY_VARS);
    }

    public void setEastString(String path, Object... vars) {
        setEastString(path, true, vars);
    }

    public void setStrings(String west, String center, String east, boolean acceptNull, boolean repaint, boolean saveVars) {
        if (acceptNull || west != null) {
            setWestString(west, false, saveVars ? westVars : Localizable.EMPTY_VARS);
        }

        if (acceptNull || center != null) {
            setCenterString(center, false, saveVars ? centerVars : Localizable.EMPTY_VARS);
        }

        if (acceptNull || east != null) {
            setEastString(east, false, saveVars ? eastVars : Localizable.EMPTY_VARS);
        }

        repaint();
    }

    public void setStrings(String west, String center, String east, boolean acceptNull, boolean repaint) {
        setStrings(west, center, east, acceptNull, repaint, false);
    }

    public void updateLocale() {
        setStrings(westPath, centerPath, eastPath, true, true);
    }
}
