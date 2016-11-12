package gr.uom.java.jdeodorant.refactoring.actions;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;

import gr.uom.java.jdeodorant.metrics.reusability.ReusabilityV1;
import gr.uom.java.jdeodorant.metrics.reusability.ReusabilityV2;
import gr.uom.java.jdeodorant.refactoring.views.ElementChangedListener;

public class MetricsMenu implements IWorkbenchWindowActionDelegate  {
	private IWorkbenchPart part;
	private ISelection selection;
	
	private IWorkbenchWindow window;
	
	public void run(IAction action) {
		
		System.out.println("Reusability v1: X");
		if(action.getId().equals("gr.uom.java.jdeodorant.actions.ReusabilityV1")) {
			System.out.println("v1:" + ReusabilityV1.calculate());;
		}
		else if(action.getId().equals("gr.uom.java.jdeodorant.actions.ReusabilityV2")) {
			System.out.println("v2:" + ReusabilityV2.calculate());;
		}
		
		
		
	}

	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
		
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		
		this.part = targetPart;
		JavaCore.addElementChangedListener(ElementChangedListener.getInstance());
	}

	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	public void init(IWorkbenchWindow arg0) {
		// TODO Auto-generated method stub
		this.window = window;
	}

}
