package ru.turikhay.tlauncher.ui.accounts.helper;

import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.Point;
import java.util.Arrays;

import ru.turikhay.tlauncher.ui.MainPane;
import ru.turikhay.tlauncher.ui.accounts.AccountHandler;
import ru.turikhay.tlauncher.ui.loc.LocalizableLabel;
import ru.turikhay.tlauncher.ui.scenes.AccountEditorScene;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedLayeredPane;

public class AccountEditorHelper extends ExtendedLayeredPane //implements LocalizableComponent {
{
	static final int MARGIN = 5;
	static final byte LEFT = 0;
	static final byte UP = 1;
	static final byte RIGHT = 2;
	static final byte DOWN = 3;

	private final MainPane pane;
	private final AccountHandler handler;

	private final HelperTip[] tips;

	//private final HelperTip remind;
	//private final EditorPane remindPane;

	private HelperState state;

	public AccountEditorHelper(AccountEditorScene scene) {
		super(scene);

		this.handler = scene.handler;
		this.pane = scene.getMainPane();

		this.tips = new HelperTip[] {
				new HelperTip("add", handler.list.add, handler.list, DOWN, HelperState.PREMIUM, HelperState.FREE),
				new HelperTip("username", handler.editor.username, handler.editor, LEFT, HelperState.PREMIUM, HelperState.FREE),
				new HelperTip("checkbox", handler.editor.premiumBox, handler.editor, LEFT, HelperState.PREMIUM, HelperState.FREE),
				new HelperTip("password", handler.editor.password, handler.editor, LEFT, HelperState.PREMIUM),
				new HelperTip("button", handler.editor.save, handler.editor, LEFT, HelperState.PREMIUM, HelperState.FREE),
				new HelperTip("exit", handler.list.back, handler.list, RIGHT, HelperState.PREMIUM, HelperState.FREE),
				new HelperTip("help", handler.list.help, handler.list, DOWN, HelperState.HELP)
		};

		//this.remind = new HelperTip("remind", handler.editor.password, handler.editor, DOWN);
		//remind.remove(remind.label);

		//this.remindPane = new EditorPane();
		//remindPane.setText("");
		//remind.add()

		add(tips);
		setState(HelperState.NONE);
	}

	public HelperState getState() {
		return state;
	}

	void updateState() {
		setState(state);
	}

	public void setState(HelperState state) {
		if (state == null)
			throw new NullPointerException();

		this.state = state;

		for (HelperState st : HelperState.values())
			st.item.setEnabled(!st.equals(state));

		if (state == HelperState.NONE) {
			for (HelperTip step : tips)
				if (step.isShowing())
					step.setVisible(false);
			return;
		}

		if(handler.editor.premiumBox.isEnabled() && handler.editor.premiumBox.isSelected()) {

		}

		for (HelperTip step : tips) {
			if (Arrays.binarySearch(step.states, 0, step.states.length, state) < 0) {
				step.setVisible(false);
				continue;
			}

			LocalizableLabel l = step.label;
			l.setText("auth.helper." + state.toString() + "." + step.name);

			Component c = step.component;
			int cWidth = c.getWidth(), cHeight = c.getHeight();
			Point cp = pane.getLocationOf(c);

			Component p = step.parent;
			int pWidth = p.getWidth(), pHeight = p.getHeight();
			Point pp = pane.getLocationOf(p);

			FontMetrics fm = l.getFontMetrics(l.getFont());
			Insets i = step.getInsets();

			int height = i.top + i.bottom + fm.getHeight();
			int width = i.left + i.right + fm.stringWidth(l.getText());

			int x, y;

			switch (step.alignment) {
			case LEFT:
				x = pp.x - MARGIN - width;
				y = cp.y + cHeight / 2 - height / 2;
				break;
			case UP:
				x = cp.x + cWidth / 2 - width / 2;
				y = pp.y - MARGIN - height;
				break;
			case RIGHT:
				x = pp.x + pWidth + MARGIN;
				y = cp.y + cHeight / 2 - height / 2;
				break;
			case DOWN:
				x = cp.x + cWidth / 2 - width / 2;
				y = pp.y + pHeight + MARGIN;
				break;
			default:
				throw new IllegalArgumentException("Unknown alignment");
			}

			if(x < 0)
				x = 0;
			else if(x + width > getWidth())
				x = getWidth() - width;

			if(y < 0)
				y = 0;
			else if(y + height > getHeight())
				y = getHeight() - height;

			step.setVisible(true);
			step.setBounds(x, y, width, height);
		}

		//this.setVisible(true);
	}

	@Override
	public void onResize() {
		super.onResize();
		updateState();
	}

	/*@Override
	public void updateLocale() {
		remindPane.setText("");
	}*/
}