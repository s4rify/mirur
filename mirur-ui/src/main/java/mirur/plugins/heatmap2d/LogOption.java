/*
 * This file is part of Mirur.
 *
 * Mirur is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Mirur is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Mirur.  If not, see <http://www.gnu.org/licenses/>.
 */
package mirur.plugins.heatmap2d;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

import mirur.plugins.DataPainterImpl.ResetAction;

public abstract class LogOption extends Action implements IMenuCreator, ResetAction {
	private ScaleOperator cur;

	public LogOption() {
		super("Color Axis Scale", IAction.AS_RADIO_BUTTON);
		setId(LogOption.class.getName());
		setMenuCreator(this);
		setToolTipText("Select Color Axis Scale");

		cur = ScaleOperator.NORMAL;
	}

	private void addScaleOption(Menu menu, String name, final ScaleOperator op) {
		Action action = new Action(name) {
			@Override
			public void run() {
				select(cur, op);
				cur = op;
			}
		};

		action.setChecked(op == cur);
		ActionContributionItem item = new ActionContributionItem(action);
		item.fill(menu, -1);
	}
	
	@Override
	public void reset() {
		select(cur, ScaleOperator.NORMAL);
		cur = ScaleOperator.NORMAL;
	}

	@Override
	public void validate() {
		// nop
	}

	protected abstract void select(ScaleOperator old, ScaleOperator op);

	@Override
	public void dispose() {
	}

	@Override
	public Menu getMenu(Control parent) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Menu getMenu(Menu parent) {
		Menu menu = new Menu(parent);
		addScaleOption(menu, "normal", ScaleOperator.NORMAL);
		addScaleOption(menu, "exp(x)", ScaleOperator.EXP);
		addScaleOption(menu, "exp10(x)", ScaleOperator.EXP10);
		addScaleOption(menu, "log(x)", ScaleOperator.LOG);
		addScaleOption(menu, "log10(x)", ScaleOperator.LOG10);

		return menu;
	}
}