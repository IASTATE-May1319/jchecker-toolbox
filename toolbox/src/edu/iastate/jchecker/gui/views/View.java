package edu.iastate.jchecker.gui.views;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import scala.collection.Iterator;
import scala.collection.mutable.ListBuffer;
import edu.iastate.jchecker.toolbox.Checker;

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

public class View extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = View.class.getName();
	public static final String NULL_LITERAL = "Null Literal Checker";
	public static final String NON_NULL = "NonNull";
	public static final String RULE_COUNT = "rule_count";
	public static final String RULE = "rule";

	private TableViewer violationViewer;
	private TableViewer viewer;
	private final Set<RuleWrapper> rules;
	private List<ListBuffer<ViolationWrapper>> violations;
	private TabFolder tabFolder;
	private Action runAction;
	private Action nullTestAction;
	private Action deleteRuleAction;
	private Action editRuleAction;
	private Action doubleClickAction;
	private Text annotation1Input;
	private Text annotation2Input;
	private TableViewer ruleViewer;
	private TabItem violationsTab;
	private TabItem ruleTab;
	private Label statusMessage;
	private final RuleWrapper nullRule = new RuleWrapper(NULL_LITERAL, null, NON_NULL);
	private Menu menu;

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
	 * Create a new J-Checker view
	 */
	public View() {
		rules = new HashSet<RuleWrapper>();
	}

	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		if (memento != null && memento.getInteger(View.RULE_COUNT) != null) {
			for (int i = 0; i < memento.getInteger(View.RULE_COUNT); i++) {
				RuleWrapper rule = new RuleWrapper(memento.getString(View.RULE + i));
				if (rule.getChecker().equals(View.NULL_LITERAL)) {
					rules.add(nullRule);
				} else {
					rules.add(rule);
				}
			}
		}
	}

	@Override
	public void saveState(IMemento memento) {
		memento.putInteger(View.RULE_COUNT, rules.size());
		int num = 0;
		for (RuleWrapper rule : rules) {
			memento.putString(View.RULE + num, rule.serialize());
			num++;
		}
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	@Override
	public void createPartControl(Composite parent) {

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
		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 0;
		com.setLayout(layout);

		Composite rulesSection = new Composite(com, SWT.NONE);
		GridLayout rulesLayout = new GridLayout(1, true);
		rulesLayout.marginWidth = 0;
		rulesSection.setLayout(rulesLayout);
		rulesSection.setLayoutData(new GridData(GridData.FILL_BOTH));
		Group ruleGroup = new Group(rulesSection, SWT.NONE);
		ruleGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));
		ruleGroup.setText("Current Rules");
		ruleGroup.setLayout(rulesLayout);

		ruleViewer = new TableViewer(ruleGroup, SWT.CHECK | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		ruleViewer.setSorter(new NameSorter());

		Table ruleTable = ruleViewer.getTable();
		ruleTable.setLayoutData(new GridData(GridData.FILL_BOTH));
		ruleTable.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (event.detail == SWT.CHECK) {
					RuleWrapper rule = (RuleWrapper) event.item.getData();
					rule.setActive(!rule.isActive());
				}
			}
		});
		if (rules.size() != 0) {
			for (RuleWrapper rule : rules) {
				createTableItemFromRule(rule);
			}
		}

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

		violationsTab = new TabItem(tabFolder, SWT.NONE);
		violationsTab.setText("Violations");
		Group flowGroup = new Group(tabFolder, SWT.FILL);
		flowGroup.setLayout(new GridLayout(1, true));
		flowGroup.setText("J-Checker Violations");
		violationViewer = new TableViewer(flowGroup, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		violationViewer.setSorter(new NameSorter());
		GridData vioData = new GridData(GridData.FILL_BOTH);
		violationViewer.getTable().setLayoutData(vioData);
		Table table = violationViewer.getTable();
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		String[] titles = { "Checker", "Source Annotation", "Destination Annotation", "Project", "Violation Start",
				"Violation End" };
		Integer[] widths = { 100, 150, 150, 75, 100, 100 };
		for (int i = 0; i < titles.length; i++) {
			TableColumn column = new TableColumn(table, SWT.NONE);
			column.setWidth(widths[i]);
			column.setText(titles[i]);
		}
		violationsTab.setControl(flowGroup);

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

		annotation1Input.setFocus();

		if (!"".equals(annotation1) && !"".equals(annotation2)) {
			RuleWrapper rule = new RuleWrapper("Custom Checker", annotation1, annotation2);
			boolean success = rules.add(rule);
			if (success) {
				createTableItemFromRule(rule);
				annotation1Input.setText("");
				annotation2Input.setText("");
				statusMessage.setText("");
			} else {
				statusMessage.setText("* Duplicate rule entry");
				annotation1Input.selectAll();
			}
		} else {
			statusMessage.setText("* Invalid rule entry");
			annotation1Input.selectAll();
		}
	}

	private void deleteRule() {
		Table table = ruleViewer.getTable();
		int[] selected = table.getSelectionIndices();
		if (selected.length > 0) {
			for (int selection : selected) {
				TableItem item = table.getItem(selection);
				if (item.getData() != null && item.getData() instanceof RuleWrapper) {
					rules.remove(item.getData());
				}
			}
		}
		table.remove(selected);
	}

	private void editRule() {

		Table table = ruleViewer.getTable();
		int[] selected = table.getSelectionIndices();
		if (selected.length > 0) {
			TableItem item = table.getItem(selected[0]);
			if (item.getData() != null && item.getData() instanceof RuleWrapper) {
				RuleWrapper rule = (RuleWrapper) item.getData();
				if (!rule.getChecker().equals(View.NULL_LITERAL)) {
					table.remove(selected[0]);
					rules.remove(rule);
					annotation1Input.setText(rule.getSourceAnnotation());
					annotation2Input.setText(rule.getDestinationAnnotation());
					annotation1Input.setFocus();
					annotation1Input.selectAll();
				}
			}
		}
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				if (View.this.ruleViewer.getTable().getSelectionCount() > 0) {
					fillContextMenu(manager);
				} else {
					menu.setVisible(false);
				}
			}
		});
		menu = menuMgr.createContextMenu(ruleViewer.getControl());
		ruleViewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, ruleViewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(editRuleAction);
		manager.add(deleteRuleAction);
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(runAction);
		manager.add(nullTestAction);
	}

	private void refresh() {
		violationViewer.getTable().removeAll();
		violations = new LinkedList<ListBuffer<ViolationWrapper>>();
		Job job = new Job("Running J-Checker Rules...") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask("Executing Rule", rules.size());
				int i = 0;
				for (final RuleWrapper rule : rules) {
					if (rule.isActive()) {
						monitor.subTask(rule.toString());

						violations.add(rule.run());

						monitor.worked(i++);
					}
				}

				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						for (ListBuffer<ViolationWrapper> list : violations) {
							Iterator<ViolationWrapper> iter = list.iterator();
							while (iter.hasNext()) {
								createTableItemFromViolation(iter.next());
							}
						}
					}
				});

				monitor.done();
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	private TableItem createTableItemFromViolation(ViolationWrapper violation) {
		TableItem item = new TableItem(violationViewer.getTable(), SWT.NONE);
		if (violation.getChecker() != null) {
			item.setText(0, violation.getChecker());
		}
		if (violation.getSourceAnnotation() != null) {
			item.setText(1, violation.getSourceAnnotation());
		}
		if (violation.getDestinationAnnotation() != null) {
			item.setText(2, violation.getDestinationAnnotation());
		}
		item.setText(3, violation.getProject());
		item.setText(4, violation.getSource());
		item.setText(5, violation.getDestination());
		item.setData(violation);
		return item;
	}

	private TableItem createTableItemFromRule(RuleWrapper rule) {
		TableItem item = new TableItem(ruleViewer.getTable(), SWT.NONE);
		item.setChecked(rule.isActive());
		// if (rule.getChecker() != null) {
		// item.setText(1, rule.getChecker());
		// }
		// if (rule.getSourceAnnotation() != null) {
		// item.setText(2, rule.getSourceAnnotation());
		// }
		// if (rule.getDestinationAnnotation() != null) {
		// item.setText(3, rule.getDestinationAnnotation());
		// }
		item.setText(0, rule.toString());
		item.setData(rule);
		return item;
	}

	private void makeActions() {
		runAction = new Action("Run J-Checker") {
			@Override
			public void run() {
				tabFolder.setSelection(violationsTab);
				statusMessage.setText("");
				refresh();
			}
		};
		runAction.setToolTipText("Run J-Checker");
		runAction.setImageDescriptor(ImageDescriptor.createFromFile(View.class, "/icons/run.gif"));

		deleteRuleAction = new Action("Delete Rule") {
			@Override
			public void run() {
				deleteRule();
			}
		};
		deleteRuleAction.setToolTipText("Delete Rule");
		deleteRuleAction.setText("Delete Rule");

		editRuleAction = new Action("Edit Rule") {
			@Override
			public void run() {
				editRule();
			}
		};
		editRuleAction.setToolTipText("Edit Rule");
		editRuleAction.setText("Edit Rule");

		nullTestAction = new Action() {
			@Override
			public void run() {
				boolean success = rules.add(nullRule);
				if (success) {
					nullRule.setActive(true);
					createTableItemFromRule(nullRule);
				} else {
					for (TableItem item : ruleViewer.getTable().getItems()) {
						RuleWrapper rule = (RuleWrapper) item.getData();
						if (rule == nullRule) {
							ruleViewer.getTable().remove(ruleViewer.getTable().indexOf(item));
							break;
						}
					}
					rules.remove(nullRule);
				}
				tabFolder.setSelection(ruleTab);
				nullTestAction.setChecked(!nullTestAction.isChecked());
			}
		};
		nullTestAction.setText("Toggle Null Literal Checker");
		nullTestAction.setToolTipText("Toggle Null Literal Checker");
		nullTestAction.setImageDescriptor(ImageDescriptor.createFromFile(View.class, "/icons/null.gif"));
		doubleClickAction = new Action() {
			@Override
			public void run() {
				ISelection selection = violationViewer.getSelection();
				Object obj = ((IStructuredSelection) selection).getFirstElement();

				if (obj instanceof ViolationWrapper) {
					ViolationWrapper object = (ViolationWrapper) obj;
					Checker.highlightSubgraph(object.getFullGraph(), object.getHighlightedSubgraph(),
							object.getSpecialNodes(), object.getSpecialEdges(), false);
				}
			}
		};
	}

	private void hookDoubleClickAction() {
		violationViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});

		ruleViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				editRuleAction.run();
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