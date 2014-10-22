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

        def scriptRunner

        if (scriptType == 'script') {
            scriptRunner = new ScriptRunner(execution, new StringReader(script))
        } else {
            scriptRunner = new ScriptRunner(execution, new WorkspaceFileReader(scriptFile).scriptFile)
        }

        def ret = scriptRunner.run(comb)

        ret
    }

    @Extension
    static class DescriptorImpl extends MatrixExecutionStrategyDescriptor {
        final String displayName = 'Groovy Script Matrix Executor Strategy'
    }

}
