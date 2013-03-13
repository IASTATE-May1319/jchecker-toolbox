package edu.iastate.jchecker.gui.views;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import toolbox.script.FlowWrapper;
import toolbox.script.TargetFlowChecker;

/**
 * This sample class demonstrates how to plug-in a new workbench view. The view
 * shows data obtained from the model. The sample creates a dummy model on the
 * fly, but a real implementation would connect to the model available either in
 * this or another plug-in (e.g. the workspace). The view is connected to the
 * model using a content provider.
 * <p>
 * The view uses a label provider to define how model objects should be
 * presented in the view. Each view can present the same model objects using
 * different labels and icons, if needed. Alternatively, a single label provider
 * can be shared between views in order to ensure that objects of the same type
 * are presented in the same way everywhere.
 * <p>
 */

public class SampleView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = SampleView.class.getName();// "edu.iastate.jchecker.gui.views.SampleView";
	public static final String NULL_LITERAL = "Null Literal Check";

	private TableViewer tableViewer;
	private TableViewer viewer;
	private Set<RuleWrapper> rules;
	private TabFolder tabFolder;
	private Action action1;
	private Action nullTestAction;
	private Action doubleClickAction;
	private Text annotation1Input;
	private Text annotation2Input;
	private TableViewer ruleViewer;
	private TabItem flows;
	private TabItem ruleTab;
	private Label statusMessage;
	private final RuleWrapper nullRule = new RuleWrapper(NULL_LITERAL, null);

	/*
	 * The content provider class is responsible for providing objects to the
	 * view. It can wrap existing objects in adapters or simply return objects
	 * as-is. These objects may be sensitive to the current input of the view,
	 * or ignore it and always show the same content (like Task List, for
	 * example).
	 */

	class ViewContentProvider implements IStructuredContentProvider {
		@Override
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}

		@Override
		public void dispose() {
		}

		@Override
		public Object[] getElements(Object parent) {
			return new String[] {};
		}
	}

	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
		@Override
		public String getColumnText(Object obj, int index) {
			return getText(obj);
		}

		@Override
		public Image getColumnImage(Object obj, int index) {
			return getImage(obj);
		}

		@Override
		public Image getImage(Object obj) {
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
		}
	}

	class NameSorter extends ViewerSorter {
	}

	/**
	 * The constructor.
	 */
	public SampleView() {
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	@Override
	public void createPartControl(Composite parent) {
		rules = new HashSet<RuleWrapper>();

		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setSorter(new NameSorter());
		viewer.setInput(getViewSite());
		viewer.getTable().setLayout(new FillLayout());
		tabFolder = new TabFolder(viewer.getTable(), SWT.BORDER);
		ruleTab = new TabItem(tabFolder, SWT.NONE);
		ruleTab.setText("Rules");

		Composite com = new Composite(tabFolder, SWT.FILL);
		com.setLayout(new GridLayout(2, false));

		Composite rulesSection = new Composite(com, SWT.NONE);
		rulesSection.setLayout(new GridLayout(1, true));
		rulesSection.setLayoutData(new GridData(GridData.FILL_BOTH));
		Group ruleGroup = new Group(rulesSection, SWT.NONE);
		ruleGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));
		ruleGroup.setText("Current Rules");
		ruleGroup.setLayout(new GridLayout(1, true));

		ruleViewer = new TableViewer(ruleGroup, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		ruleViewer.setSorter(new NameSorter());
		ruleViewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite addSection = new Composite(com, SWT.NONE);
		addSection.setLayout(new GridLayout(1, true));
		addSection.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		Group add = new Group(addSection, SWT.NONE);
		add.setLayout(new GridLayout(1, true));
		add.setText("Add Rule");
		add.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		Composite composite = new Composite(add, SWT.FILL);
		composite.setLayout(new GridLayout(2, true));

		Label label = new Label(composite, SWT.NULL);
		label.setText("Source:");

		GridData g1 = new GridData(GridData.FILL_HORIZONTAL);

		annotation1Input = new Text(composite, SWT.SINGLE | SWT.BORDER);
		annotation1Input.setLayoutData(g1);

		Label label1 = new Label(composite, SWT.NULL);
		label1.setText("Destination:");

		annotation2Input = new Text(composite, SWT.SINGLE | SWT.BORDER);
		annotation2Input.setLayoutData(g1);

		final Button buttonOK = new Button(add, SWT.PUSH);
		buttonOK.setText("Add");
		buttonOK.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER | GridData.GRAB_HORIZONTAL));

		Listener clearStatus = new Listener() {
			@Override
			public void handleEvent(Event event) {
				statusMessage.setText("");
			}
		};

		Listener addRule = new Listener() {
			@Override
			public void handleEvent(Event event) {
				addRule();
			}
		};

		Listener keyPressed = new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (event.keyCode == SWT.CR) {
					addRule();
				}
			}
		};

		annotation1Input.addListener(SWT.Modify, clearStatus);
		annotation2Input.addListener(SWT.Modify, clearStatus);
		annotation1Input.addListener(SWT.FocusOut, clearStatus);
		annotation2Input.addListener(SWT.FocusOut, clearStatus);
		annotation1Input.addListener(SWT.KeyDown, keyPressed);
		annotation2Input.addListener(SWT.KeyDown, keyPressed);
		buttonOK.addListener(SWT.FocusOut, clearStatus);
		buttonOK.addListener(SWT.Selection, addRule);

		statusMessage = new Label(addSection, SWT.NONE);
		statusMessage.setFont(new Font(statusMessage.getDisplay(), "Arial", 8, SWT.NONE));
		statusMessage.setText("                                        ");
		statusMessage.setForeground(statusMessage.getDisplay().getSystemColor(SWT.COLOR_RED));
		statusMessage.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		ruleTab.setControl(com);

		flows = new TabItem(tabFolder, SWT.NONE);
		flows.setText("Violations");
		Group flowGroup = new Group(tabFolder, SWT.FILL);
		flowGroup.setLayout(new GridLayout(1, true));
		flowGroup.setText("J-Checker Violations");
		tableViewer = new TableViewer(flowGroup, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		tableViewer.setSorter(new NameSorter());
		tableViewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		flows.setControl(flowGroup);
		tabFolder.pack();

		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), "may1319.plugin.GUI.viewer");
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
	}

	private void addRule() {
		String annotation1 = annotation1Input.getText().trim();
		String annotation2 = annotation2Input.getText().trim();

		if (!"".equals(annotation1) && !"".equals(annotation2)) {
			RuleWrapper rule = new RuleWrapper(annotation1, annotation2);
			boolean success = rules.add(rule);
			if (success) {
				ruleViewer.add(rule);
				annotation1Input.setText("");
				annotation2Input.setText("");
				statusMessage.setText("");
			} else {
				statusMessage.setText("*Duplicate rule entry");
			}
		} else {
			statusMessage.setText("*Invalid rule entry");
		}
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				SampleView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(action1);
		manager.add(new Separator());
		// manager.add(action2);
	}

	private void fillContextMenu(IMenuManager manager) {
		// manager.add(action1);
		// Other plug-ins can contribute there actions here
		// manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(action1);
		manager.add(nullTestAction);
	}

	private void refresh() {
		tableViewer.getTable().removeAll();
		Job job = new Job("Running J-Checker Rules...") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask("Executing Rule", rules.size());
				int i = 0;
				for (final RuleWrapper rule : rules) {
					monitor.subTask(rule.toString());

					rule.run(tableViewer);

					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							rule.postRun(tableViewer);
						}
					});

					monitor.worked(i++);
				}

				monitor.done();
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	private void makeActions() {
		action1 = new Action("Run J-Checker") {
			@Override
			public void run() {
				tabFolder.setSelection(flows);
				action1.setChecked(!action1.isChecked());
				statusMessage.setText("");
				refresh();
			}
		};
		action1.setToolTipText("Run J-Checker");
		action1.setImageDescriptor(ImageDescriptor.createFromFile(SampleView.class, "/icons/run.gif"));

		nullTestAction = new Action() {
			@Override
			public void run() {
				boolean success = rules.add(nullRule);
				if (success) {
					ruleViewer.add(nullRule);
				} else {
					ruleViewer.remove(nullRule);
					rules.remove(nullRule);
				}
				tabFolder.setSelection(ruleTab);
			}
		};
		nullTestAction.setText("Toggle Null Literal Checker");
		nullTestAction.setToolTipText("Toggle Null Literal Checker");
		nullTestAction.setImageDescriptor(ImageDescriptor.createFromFile(SampleView.class, "/icons/null.gif"));
		doubleClickAction = new Action() {
			@Override
			public void run() {
				ISelection selection = tableViewer.getSelection();
				Object obj = ((IStructuredSelection) selection).getFirstElement();

				if (obj instanceof FlowWrapper) {
					FlowWrapper object = (FlowWrapper) obj;
					TargetFlowChecker.highlightSubgraph(object.getFullGraph(), object.getHighlightedSubgraph(),
							object.getSpecialNodes(), object.getSpecialEdges(), false);
				}
			}
		};
	}

	private void hookDoubleClickAction() {
		tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}
}