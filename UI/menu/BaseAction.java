package alice.menu;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IActionDelegate;


public abstract class BaseAction implements IActionDelegate {

	private ISelection fSelection;
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	final public void run(IAction action) {
		run(fSelection);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	final public void selectionChanged(IAction action, ISelection selection) {
		fSelection= selection;
		if (action != null)
			action.setEnabled(isEnabled(fSelection));
	}
	
	protected boolean isEnabled(ISelection selection) {
		return false;
	}
	
	abstract protected void run(ISelection selection);
}
