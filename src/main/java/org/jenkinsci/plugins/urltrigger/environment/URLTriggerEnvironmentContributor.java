package org.jenkinsci.plugins.urltrigger.environment;

import hudson.EnvVars;
import hudson.Extension;
import hudson.matrix.MatrixRun;
import hudson.matrix.MatrixBuild;
import hudson.model.EnvironmentContributor;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.jenkinsci.plugins.urltrigger.URLTriggerCause;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.io.IOException;

/**
 * @author Fei Yang
 */
@Extension
public class URLTriggerEnvironmentContributor extends EnvironmentContributor {
    @Override
    public void buildEnvironmentFor(@NonNull Run r, @NonNull EnvVars envs, @NonNull TaskListener listener) throws IOException, InterruptedException {
        URLTriggerCause cause = null;
        if (r instanceof MatrixRun) {
            MatrixBuild parent = ((MatrixRun) r).getParentBuild();
            if (parent != null) {
                cause = parent.getCause(URLTriggerCause.class);
            }
        } else {
            cause = (URLTriggerCause) r.getCause(URLTriggerCause.class);
        }
        if (cause != null) {
            envs.override("URL_TRIGGER_CAUSE", cause.getUrlTrigger());
        }
    }
}
