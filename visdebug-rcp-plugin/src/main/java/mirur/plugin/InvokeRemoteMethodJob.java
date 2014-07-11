package mirur.plugin;

import static org.eclipse.jdt.internal.debug.core.JavaDebugUtils.resolveJavaProject;
import mirur.core.MirurAgent;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.debug.core.IJavaClassType;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;

@SuppressWarnings("restriction")
public class InvokeRemoteMethodJob extends Job {
    private final IJavaVariable var;
    private final IJavaStackFrame frame;

    public InvokeRemoteMethodJob(IJavaVariable var, IJavaStackFrame frame) {
        super("invokeing method");
        this.frame = frame;
        this.var = var;

        setPriority(Job.SHORT);
        setUser(false);
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
        IJavaThread thread = (IJavaThread) frame.getThread();
        IJavaDebugTarget target = (IJavaDebugTarget) thread.getDebugTarget();
        IJavaProject project = resolveJavaProject(frame);

        try {
            if (!isValidJVMVersion(target.getVersion())) {
                return Status.OK_STATUS;
            }
        } catch (DebugException ex) {
            throw new RuntimeException(ex);
        }

        if (target instanceof IJavaDebugTarget && project != null && thread.isSuspended()) {
            deployAgent(project);

            thread.queueRunnable(new AgentInvokeRunnable(target, thread, frame, var));
        }

        return Status.OK_STATUS;
    }

    private boolean isValidJVMVersion(String version) {
        int secondDot = version.indexOf('.');
        secondDot = version.indexOf('.', secondDot + 1);
        String major = version.substring(0, secondDot);

        return Double.parseDouble(major) >= 1.5;
    }

    private void deployAgent(IJavaProject project) {
        new RemoteAgentDeployer().install(project);
    }

    private static class AgentInvokeRunnable implements Runnable {
        final IJavaDebugTarget target;
        final IJavaStackFrame frame;
        final IJavaThread thread;
        final IJavaVariable var;

        AgentInvokeRunnable(IJavaDebugTarget target, IJavaThread thread, IJavaStackFrame frame, IJavaVariable var) {
            this.target = target;
            this.thread = thread;
            this.var = var;
            this.frame = frame;
        }

        @Override
        public void run() {
            try {
                IJavaClassType agentType = getRemoteAgentClass(target, thread);
                IJavaValue[] args = new IJavaValue[] { (IJavaValue) var.getValue() };
                IJavaValue result = agentType.sendMessage("toArray", "(Ljava/lang/Object;)Ljava/lang/Object;", args, thread);

                String name = var.getName();

                if (!result.isNull()) {
                    new CopyJDIArrayJob(name, result, frame).schedule();
                }
            } catch (DebugException ex) {
                IStatus status = ex.getStatus();
                if (status != null && status.getException() != null) {
                    status.getException().printStackTrace();
                }
            }
        }

        IJavaClassType getRemoteAgentClass(IJavaDebugTarget target, IJavaThread thread) throws DebugException {
            IJavaType[] types = target.getJavaTypes(MirurAgent.class.getName());
            if (types == null) {
                loadRemoteAgentClass(target, thread);
                types = target.getJavaTypes(MirurAgent.class.getName());
            }

            return (IJavaClassType) types[0];
        }

        void loadRemoteAgentClass(IJavaDebugTarget target, IJavaThread thread) throws DebugException {
            IJavaType[] types = target.getJavaTypes(Class.class.getName());
            IJavaClassType classClass = (IJavaClassType) types[0];
            IJavaValue[] args = new IJavaValue[] { target.newValue(MirurAgent.class.getName()) };
            classClass.sendMessage("forName", "(Ljava/lang/String;)Ljava/lang/Class;", args, thread);
        }

    }
}
