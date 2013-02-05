package com.ensoftcorp.atlas.java.example.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;

import com.ensoftcorp.atlas.java.core.log.Log;

public abstract class AbstractQueryHandler extends AbstractHandler {

	
	/**
	 * the command has been executed, so extract the needed information
	 * from the application context.
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {

		Job j = new Job("Atlas query") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					monitor.beginTask("Atlas query", IProgressMonitor.UNKNOWN);
					
					runQuery(new SubProgressMonitor(monitor, IProgressMonitor.UNKNOWN));
					
				} catch (Exception e) {
					Log.error("Error while running Atlas query", e);
				}finally{
					monitor.done();
				}

				return Status.OK_STATUS;
			}

		};

		j.schedule();

		return null;
	}

	protected abstract void runQuery(IProgressMonitor monitor);
	
}
