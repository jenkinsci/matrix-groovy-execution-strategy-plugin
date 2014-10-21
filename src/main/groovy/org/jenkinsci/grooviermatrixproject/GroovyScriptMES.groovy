package org.jenkinsci.grooviermatrixproject

import hudson.Extension
import org.kohsuke.stapler.DataBoundConstructor
import hudson.matrix.Combination
import hudson.matrix.MatrixBuild
import hudson.matrix.MatrixBuild.MatrixBuildExecution
import hudson.matrix.MatrixExecutionStrategyDescriptor

/**
 * Created by jeremymarshall on 12/10/2014.
 */
class GroovyScriptMES extends BaseMES {

    String script
    String scriptFile
    String scriptType='script'

    GroovyScriptMES() {

    }

    @DataBoundConstructor
    GroovyScriptMES( String script, String scriptFile, String scriptType) {
        this.script = script
        this.scriptFile = scriptFile
        this.scriptType = scriptType
    }

    Map decideOrder(MatrixBuild.MatrixBuildExecution execution, List<Combination> comb) {
        def scriptRunner = new ScriptRunner(execution, scriptType == 'file' ? new File(scriptFile) : script)

        def ret = scriptRunner.run(comb)

        ret
    }

    @Extension
    public static class DescriptorImpl extends MatrixExecutionStrategyDescriptor {
        final String displayName = 'Groovy Script Matrix Executor Strategy'
    }

}
