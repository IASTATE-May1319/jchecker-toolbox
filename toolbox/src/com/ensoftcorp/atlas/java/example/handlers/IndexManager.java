package com.ensoftcorp.atlas.java.example.handlers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;

import com.ensoftcorp.atlas.java.core.index.ProjectPropertiesUtil;
import com.ensoftcorp.atlas.java.ui.preferences.IndexingUtil.IndexListener;
import com.ensoftcorp.atlas.java.ui.preferences.IndexingUtil.IndexOperation;


public class IndexManager {
	
	static {
		com.ensoftcorp.atlas.java.ui.preferences.IndexingUtil.addListener(new IndexListener() {
			
			@Override
			public void indexOperationStarted(IndexOperation paramIndexOperation) {
				// NOOP
			}
			
			@Override
			public void indexOperationError(IndexOperation paramIndexOperation,
					Throwable paramThrowable) {
				// TODO HANDLE
			}
			
			@Override
			public void indexOperationComplete(IndexOperation paramIndexOperation) {
				// NOOP
			}
			
			@Override
			public void indexOperationCancelled(IndexOperation paramIndexOperation) {
				// TODO HANDLE
			}
		});
	}

	public static enum AndroidApproximation {
		None, Stubs, Returns, Modeled, Full
	}

	private static class CloseProjectJob extends Job {
		private IProject project;
		private CoreException error;

		public CloseProjectJob(IProject project) {
			super("Close Project");
			this.project = project;
		}

		@Override
		protected org.eclipse.core.runtime.IStatus run(
				org.eclipse.core.runtime.IProgressMonitor monitor) {
			final IProgressMonitor fMonitor;

			if (monitor == null) {
				fMonitor = new NullProgressMonitor();
			} else {
				fMonitor = monitor;
			}

			fMonitor.beginTask("Close Project", 1);

			try {

				project.close(new SubProgressMonitor(fMonitor, 1));

			} catch (CoreException e) {
				error = e;
				e.printStackTrace();
				return Status.CANCEL_STATUS;
			} finally {
				fMonitor.done();
			}

			return Status.OK_STATUS;
		}
	}

	private static class CreateProjectJob extends Job {
		public String path;
		public IProject project;
		public CoreException error;

		public CreateProjectJob(String path) {
			super("Create Project from path");
			this.path = path;
		}

		@Override
		protected org.eclipse.core.runtime.IStatus run(
				org.eclipse.core.runtime.IProgressMonitor monitor) {
			IProjectDescription description;
			final IProgressMonitor fMonitor;

			if (monitor == null) {
				fMonitor = new NullProgressMonitor();
			} else {
				fMonitor = monitor;
			}

			fMonitor.beginTask("Create Project", 1);

			try {

				description = ResourcesPlugin.getWorkspace()
						.loadProjectDescription(
								new Path(new File(path + File.separatorChar
										+ ".project").getAbsolutePath()));

				project = ResourcesPlugin.getWorkspace().getRoot()
						.getProject(description.getName());
				// import project
				if (!project.exists())
					project.create(description, new SubProgressMonitor(
							fMonitor, 1));

			} catch (CoreException e) {
				error = e;
				e.printStackTrace();
				return Status.CANCEL_STATUS;
			} finally {
				fMonitor.done();
			}

			return Status.OK_STATUS;
		}
	}

	private static class OpenProjectJob extends Job {
		private IProject project;
		private CoreException error;

		public OpenProjectJob(IProject project) {
			super("Open Project");
			this.project = project;
		}

		@Override
		protected org.eclipse.core.runtime.IStatus run(IProgressMonitor monitor) {
			final IProgressMonitor fMonitor;

			if (monitor == null) {
				fMonitor = new NullProgressMonitor();
			} else {
				fMonitor = monitor;
			}

			fMonitor.beginTask("Open Project", 2);

			try {

				project.open(new SubProgressMonitor(fMonitor, 1));

				// Build it also
				project.build(IncrementalProjectBuilder.FULL_BUILD,
						new SubProgressMonitor(fMonitor, 1));

			} catch (CoreException e) {
				error = e;
				e.printStackTrace();
				return Status.CANCEL_STATUS;
			} finally {
				fMonitor.done();
			}

			return Status.OK_STATUS;
		}
	}

	private static Map<String, IProject> workspaceProjects = new HashMap<String, IProject>();

	private static final String STUBS_PROJECT_PATH = "../android-14-stubs";
	private static final String STUBS_RETURNS_PROJECT_PATH = "../android-14-stubs-returns";
	private static final String MODELED_PROJECT_PATH = "../android-14-modeled";
	private static final String FULL_PROJECT_PATH = "../android-14";
	
	private static final String TEST_APP_PATH = "resources/test/app/";

	private static File appToDir(TestApp app) {
		return new File(TEST_APP_PATH + app.getFilename());
	}

	/**
	 * Loads exactly the given test apps and indexes them.
	 * 
	 * 
	 * They are loaded into the workspace along with any projects required for
	 * the given approximation.
	 * 
	 * Any other currently loaded projects are unloaded
	 * 
	 * @param approximation
	 * @param apps
	 * @throws CoreException
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public static void load(AndroidApproximation approximation, TestApp... apps)
			throws CoreException, InterruptedException, IOException {

		ArrayList<File> projectDirs = new ArrayList<File>();

		switch (approximation) {
		case Stubs:
			projectDirs.add(new File(STUBS_PROJECT_PATH));
			break;
		case Full:
		  projectDirs.add(new File(FULL_PROJECT_PATH));
      break;
		case Modeled:
		  projectDirs.add(new File(MODELED_PROJECT_PATH));
      break;
		case Returns:
		  projectDirs.add(new File(STUBS_RETURNS_PROJECT_PATH));
      break;
		case None:
			break;
		default:
			throw new IllegalArgumentException(
					"Android approximation type not supported " + approximation);
		}

		// load new apps
		if (apps != null)
			for (TestApp app : apps) {

				// Load target app
				File appDir = appToDir(app);

				if (!appDir.exists())
					throw new IllegalArgumentException(
							"Failed to index app named: " + app);

				projectDirs.add(appDir);
			}

		load(projectDirs);
	}
	
	public static void load(List<File> apps) throws CoreException, InterruptedException, IOException{
	  loadExactly(apps);
	}

	private static IProject loadEclipseProject(File eclipseProjectDir)
			throws CoreException, InterruptedException, IOException {
		String path = eclipseProjectDir.getCanonicalPath();

		// Actually create project in testing workspace if it doesn't already
		// exist
		if (!workspaceProjects.containsKey(path)) {
			CreateProjectJob job = new CreateProjectJob(path);
			job.schedule();
			job.join();

			if (job.error != null)
				throw job.error;

			if (job.project == null)
				throw new IllegalStateException(
						"Failed to create project for directory "
								+ eclipseProjectDir);

			workspaceProjects.put(path, job.project);
		}

		// Now attempt to open the project
		IProject project = workspaceProjects.get(path);

		return openEclipseProject(project);

	}

	private static void loadExactly(List<File> projects) throws CoreException,
			InterruptedException, IOException {

		if (projects == null)
			projects = new ArrayList<File>();

		// Partial Unload
		// ArrayList<String> projectPaths = new ArrayList<String>();
		//
		// for (File project : projects) {
		// projectPaths.add(project.getAbsolutePath());
		// }
		// for (Entry<String, IProject> entry : workspaceProjects.entrySet()) {
		// if (!projectPaths.contains(entry.getKey())) {
		// // Unload
		// if (entry.getValue().isOpen()) {
		// unloadEclipseProject(entry.getValue());
		// }
		// }
		// }

		// Unload everything
		unloadAll();

		// Load
		for (File path : projects) {
			IProject project = loadEclipseProject(path);
			ProjectPropertiesUtil.setIndexingEnabled(project, true);
		}

		// Index synchronously
		com.ensoftcorp.atlas.java.ui.preferences.IndexingUtil
				.indexWorkspace(true);
	}

	private static IProject openEclipseProject(IProject project)
			throws CoreException, InterruptedException {
		OpenProjectJob job = new OpenProjectJob(project);
		job.schedule();
		job.join();

		if (job.error != null)
			throw job.error;

		if (!project.isOpen())
			throw new IllegalStateException("Failed to open project: "
					+ project.getName());

		return project;
	}

	@Deprecated
	public static void unload() throws CoreException, InterruptedException {
		unloadAll();
	}

	private static void unloadAll() throws CoreException, InterruptedException {
		for (Entry<String, IProject> entry : workspaceProjects.entrySet()) {

			// Unload
			ProjectPropertiesUtil.setIndexingEnabled(entry.getValue(), false);
			
			// Full unload - might save some memory in eclipse for many projects?
//			if (entry.getValue().isOpen()) {
//				unloadEclipseProject(entry.getValue());
//			}

		}

		// Empty the index?
		com.ensoftcorp.atlas.java.ui.preferences.IndexingUtil
				.indexWorkspace(true);
	}

	//
	// private static void unloadEclipseProject(File eclipseProjectDir)
	// throws CoreException, InterruptedException {
	// String path = eclipseProjectDir.getAbsolutePath();
	//
	// if (workspaceProjects.containsKey(path)) {
	// IProject project = workspaceProjects.get(path);
	//
	// unloadEclipseProject(project);
	// }
	// }

	private static void unloadEclipseProject(IProject project)
			throws CoreException, InterruptedException {

		CloseProjectJob job = new CloseProjectJob(project);
		job.schedule();
		job.join();

		if (job.error != null)
			throw job.error;

	}
}