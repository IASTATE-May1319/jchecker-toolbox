package edu.iastate.jchecker.gui.views;

import java.util.HashSet;
import java.util.Set;

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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
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

import scala.collection.Iterator;
import scala.collection.mutable.ListBuffer;
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
		tabFolder.setSize(parent.getSize());
		ruleTab = new TabItem(tabFolder, SWT.NONE);
		ruleTab.setText("Rules");

		Composite com = new Composite(tabFolder, SWT.FILL);
		com.setLayout(new GridLayout(2, false));

		GridData g1 = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
		GridData g2 = new GridData(GridData.FILL_HORIZONTAL);

		Group ruleGroup = new Group(com, SWT.None);
		ruleGroup.setLayoutData(g1);
		ruleGroup.setText("Current Rules");
		ruleGroup.setLayout(new GridLayout(1, true));

		ruleViewer = new TableViewer(ruleGroup, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		ruleViewer.setSorter(new NameSorter());
		ruleViewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));

		Group add = new Group(com, SWT.FILL);
		add.setLayout(new GridLayout(1, true));
		add.setText("Add Rule");
		add.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		Composite composite = new Composite(add, SWT.FILL);
		composite.setLayout(new GridLayout(2, true));

		Label label = new Label(composite, SWT.NULL);
		label.setText("Source:");

		annotation1Input = new Text(composite, SWT.SINGLE | SWT.BORDER);
		annotation1Input.setLayoutData(g2);

		Label label1 = new Label(composite, SWT.NULL);
		label1.setText("Destination:");

		annotation2Input = new Text(composite, SWT.SINGLE | SWT.BORDER);
		annotation2Input.setLayoutData(g2);

		final Button buttonOK = new Button(add, SWT.PUSH);
		buttonOK.setText("Add");
		buttonOK.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER | GridData.GRAB_HORIZONTAL));

		annotation1Input.addListener(SWT.Modify, new Listener() {
			@Override
			public void handleEvent(Event event) {
				try {
					String annotation1 = annotation1Input.getText();
					String annotation2 = annotation2Input.getText();
					if (annotation1 != null && annotation2 != null) {
						buttonOK.setEnabled(true);
					}
				} catch (Exception e) {
					buttonOK.setEnabled(false);
				}
			}
		});

		annotation2Input.addListener(SWT.Modify, new Listener() {
			@Override
			public void handleEvent(Event event) {
				try {
					String annotation1 = annotation1Input.getText().trim();
					String annotation2 = annotation2Input.getText().trim();
					if (annotation1 != null && annotation2 != null) {
						buttonOK.setEnabled(true);
					}
				} catch (Exception e) {
					buttonOK.setEnabled(false);
				}
			}
		});

		buttonOK.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				String annotation1 = annotation1Input.getText();
				String annotation2 = annotation2Input.getText();

				if (annotation1 != null && annotation2 != null) {
					RuleWrapper rule = new RuleWrapper(annotation1, annotation2);
					boolean success = rules.add(rule);
					if (success) {
						ruleViewer.add(rule);
					}
					annotation1Input.setText("");
					annotation2Input.setText("");
					refresh();
				}
			}
		});
		ruleTab.setControl(com);

		flows = new TabItem(tabFolder, SWT.NONE);
		flows.setText("Errors");
		Group flowGroup = new Group(tabFolder, SWT.FILL);
		flowGroup.setLayout(new GridLayout(1, true));
		flowGroup.setText("J-Checker Errors");
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
		for (RuleWrapper rule : rules) {
			if (rule.getDest() == null) {
				if (rule.getSource() == NULL_LITERAL) {
					ListBuffer<FlowWrapper> results = TargetFlowChecker.nullLiteralTest(null, false);
					Iterator<FlowWrapper> iter = results.iterator();
					while (iter.hasNext()) {
						FlowWrapper flow = iter.next();
						tableViewer.add(flow);
					}
				}
			} else {
				rule.run(tableViewer);
			}
		}
	}

	private void makeActions() {
		action1 = new Action("Run J-Checker") {
			@Override
			public void run() {
				refresh();
				tabFolder.setSelection(flows);
				action1.setChecked(!action1.isChecked());
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