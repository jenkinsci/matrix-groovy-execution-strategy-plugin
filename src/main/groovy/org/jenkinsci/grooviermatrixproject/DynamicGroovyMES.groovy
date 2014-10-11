package org.jenkinsci.grooviermatrixproject

import hudson.Extension
import hudson.matrix.MatrixExecutionStrategy
import hudson.matrix.MatrixExecutionStrategyDescriptor
import hudson.matrix.MatrixConfiguration
import hudson.matrix.MatrixBuild
import hudson.matrix.MatrixRun
import hudson.model.Result
import org.kohsuke.stapler.DataBoundConstructor

/**
 * Created by jeremymarshall on 9/10/2014.
 */
class DynamicGroovyMES extends MatrixExecutionStrategy {

    String script
    volatile String combinationFilter
    volatile String touchStoneCombinationFilter
    volatile boolean runSequentially = false

    @DataBoundConstructor
    public DynamicGroovyMES(String script) {
        this.script = script;
    }

    @Override
    public Result run(MatrixBuild.MatrixBuildExecution execution) throws InterruptedException, IOException {

        Collection<MatrixConfiguration> touchStoneConfigurations = new HashSet<MatrixConfiguration>();
        Collection<MatrixConfiguration> delayedConfigurations = new HashSet<MatrixConfiguration>();

        filterConfigurations(
                execution,
                touchStoneConfigurations,
                delayedConfigurations
        );

        if (notifyStartBuild(execution.getAggregators())) return Result.FAILURE;

        if (sorter != null) {
            touchStoneConfigurations = createTreeSet(touchStoneConfigurations, sorter);
            delayedConfigurations    = createTreeSet(delayedConfigurations, sorter);
        }

        if(!runSequentially)
            for(MatrixConfiguration c : touchStoneConfigurations)
                scheduleConfigurationBuild(execution, c);

        Result r = Result.SUCCESS;
        for (MatrixConfiguration c : touchStoneConfigurations) {
            if(runSequentially)
                scheduleConfigurationBuild(execution, c);
            MatrixRun run = waitForCompletion(execution, c);
            notifyEndBuild(run,execution.getAggregators());
            r = r.combine(getResult(run));
        }

        PrintStream logger = execution.getListener().getLogger();

        if (touchStoneResultCondition != null && r.isWorseThan(touchStoneResultCondition)) {
            logger.printf("Touchstone configurations resulted in %s, so aborting...%n", r);
            return r;
        }

        if(!runSequentially)
            for(MatrixConfiguration c : delayedConfigurations)
                scheduleConfigurationBuild(execution, c);

        for (MatrixConfiguration c : delayedConfigurations) {
            if(runSequentially)
                scheduleConfigurationBuild(execution, c);
            MatrixRun run = waitForCompletion(execution, c);
            notifyEndBuild(run,execution.getAggregators());
            logger.println(Messages.MatrixBuild_Completed(ModelHyperlinkNote.encodeTo(c), getResult(run)));
            r = r.combine(getResult(run));
        }

        return r;
    }

    @Extension
    public static class DescriptorImpl extends MatrixExecutionStrategyDescriptor {
        @Override
        public String getDisplayName() {
            return "Dynamic Groovy";
        }
    }

}
