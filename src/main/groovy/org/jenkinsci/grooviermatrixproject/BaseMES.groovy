package org.jenkinsci.grooviermatrixproject

import hudson.Extension
import hudson.console.ModelHyperlinkNote
import hudson.matrix.MatrixExecutionStrategy
import hudson.matrix.MatrixExecutionStrategyDescriptor
import hudson.matrix.MatrixConfiguration
import hudson.matrix.MatrixBuild
import hudson.matrix.MatrixRun
import hudson.matrix.MatrixAggregator
import hudson.model.Action
import hudson.model.Cause
import hudson.model.ParametersAction
import hudson.model.Result
import hudson.model.Run
import org.kohsuke.stapler.DataBoundConstructor
import hudson.matrix.Combination
import hudson.matrix.MatrixChildAction
import hudson.AbortException
import hudson.model.Queue
import hudson.model.BuildListener
import hudson.matrix.MatrixBuild.MatrixBuildExecution

import javax.annotation.Nullable

/**
 * Created by jeremymarshall on 9/10/2014.
 */
abstract class BaseMES extends MatrixExecutionStrategy {

    @Override
    Result run(MatrixBuild.MatrixBuildExecution execution) throws InterruptedException, IOException {

        //final Collection<MatrixConfiguration> configurations = new HashSet<MatrixConfiguration>()
        List<Combination> combs = new ArrayList<Combination>()
        Map<Combination, MatrixConfiguration> mc = new HashMap<Combination, MatrixConfiguration>()
        execution.activeConfigurations.each {
            def c = it.combination
            combs << c
            mc[c] = it
        }

        Result r = Result.SUCCESS

        def multiCombs = decideOrder(execution, combs)

        multiCombs.any { k,v ->

            execution.getListener().getLogger().println("Running ${k}")

            v.each { inner ->

                def mc2 = mc[inner]
                scheduleConfigurationBuild(execution, mc2)
            }

            v.each { inner ->
                def mc2 = mc[inner]

                MatrixRun run = waitForCompletion(execution, mc2);
                notifyEndBuild(run, execution.getAggregators());
                execution.getListener().getLogger().println('Completed ' + ModelHyperlinkNote.encodeTo(mc2) + ' ' + getResult(run))
                r = r.combine(getResult(run))
            }

            //choke if we have a failure
            r == Result.FAILURE
        }
        r
    }

    //override this and return a list of list of combinations
    //and the builds will be run each inner list in parallel then do the next list
    //and if anything fails it stops
    abstract Map decideOrder(MatrixBuild.MatrixBuildExecution execution, List<Combination> comb)

    void scheduleConfigurationBuild(MatrixBuildExecution exec, MatrixConfiguration c) {
        MatrixBuild build = exec.getBuild();
        exec.getListener().getLogger().println('Triggering ' + ModelHyperlinkNote.encodeTo(c))

        // filter the parent actions for those that can be passed to the individual jobs.
        List<Action> childActions = new ArrayList<Action>(build.getActions(MatrixChildAction.class));
        childActions.addAll(build.getActions(ParametersAction.class)); // used to implement MatrixChildAction
        c.scheduleBuild(childActions, new Cause.UpstreamCause((Run) build));
    }

    MatrixRun waitForCompletion(MatrixBuildExecution exec, MatrixConfiguration c) throws InterruptedException, IOException {
        BuildListener listener = exec.getListener();
        String whyInQueue = "";
        long startTime = System.currentTimeMillis();

        // wait for the completion
        int appearsCancelledCount = 0;
        while (true) {
            MatrixRun b = c.getBuildByNumber(exec.getBuild().getNumber());

            // two ways to get beyond this. one is that the build starts and gets done,
            // or the build gets cancelled before it even started.
            if (b != null && !b.isBuilding()) {
                Result buildResult = b.getResult();
                if (buildResult != null)
                    return b;
            }
            Queue.Item qi = c.getQueueItem();
            if (b == null && qi == null)
                appearsCancelledCount++;
            else
                appearsCancelledCount = 0;

            if (appearsCancelledCount >= 5) {
                // there's conceivably a race condition in computating b and qi, as their computation
                // are not synchronized. There are indeed several reports of Hudson incorrectly assuming
                // builds being cancelled. See
                // http://www.nabble.com/Master-slave-problem-tt14710987.html and also
                // http://www.nabble.com/Anyone-using-AccuRev-plugin--tt21634577.html#a21671389
                // because of this, we really make sure that the build is cancelled by doing this 5
                // times over 5 seconds
                listener.getLogger().println(ModelHyperlinkNote.encodeTo(c) + ' appears to be cancelled')
                return null;
            }

            if (qi != null) {
                // if the build seems to be stuck in the queue, display why
                String why = qi.getWhy();
                if (why != null && !why.equals(whyInQueue) && System.currentTimeMillis() - startTime > 5000) {
                    listener.getLogger().print('Configuration ' + ModelHyperlinkNote.encodeTo(c) + ' is still in the queue: ');
                    qi.getCauseOfBlockage().print(listener); //this is still shown on the same line
                    whyInQueue = why;
                }
            }

            Thread.sleep(1000);
        }
    }

    Result getResult(@Nullable MatrixRun run) {
        // null indicates that the run was cancelled before it even gets going
        return run != null ? run.getResult() : Result.ABORTED;
    }

    void notifyEndBuild(MatrixRun b, List<MatrixAggregator> aggregators) throws InterruptedException, IOException {
        if (b == null) return; // can happen if the configuration run gets cancelled before it gets started.
        for (MatrixAggregator a : aggregators)
            if (!a.endRun(b))
                throw new AbortException()
    }

    //@Extension
    //public static class DescriptorImpl extends MatrixExecutionStrategyDescriptor {
    //    @Override
    //    public String getDisplayName() {
    //        return 'Groovier Matrix Executor Strategy'
    //    }
    //}

}
