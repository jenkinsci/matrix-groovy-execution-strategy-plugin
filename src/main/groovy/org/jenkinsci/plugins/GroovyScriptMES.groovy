package org.jenkinsci.plugins

import hudson.Extension
import hudson.model.Descriptor
import jenkins.model.Jenkins
import org.kohsuke.stapler.DataBoundConstructor
import hudson.matrix.Combination
import hudson.matrix.MatrixBuild
import hudson.matrix.MatrixBuild.MatrixBuildExecution
import hudson.matrix.MatrixExecutionStrategyDescriptor
import org.jenkinsci.plugins.scriptsecurity.sandbox.groovy.SecureGroovyScript
import org.jenkinsci.plugins.scriptsecurity.scripts.ApprovalContext
import org.jenkinsci.plugins.scriptsecurity.scripts.ClasspathEntry
import net.sf.json.JSONObject
import org.kohsuke.stapler.StaplerRequest

/**
 * Created by jeremymarshall on 12/10/2014.
 */
class GroovyScriptMES extends BaseMES {

    SecureGroovyScript secureScript

    @SuppressWarnings('UnnecessaryTransientModifier')
    transient String script
    String scriptFile
    String scriptType = 'script'

    @DataBoundConstructor
    GroovyScriptMES( SecureGroovyScript secureScript, String scriptFile, String scriptType) {
        this.secureScript = secureScript
        this.scriptFile = scriptFile
        this.scriptType = scriptType

        this.secureScript.configuringWithKeyItem()
    }

    Map decideOrder(MatrixBuild.MatrixBuildExecution execution, List<Combination> comb) {

        ScriptRunner scriptRunner
        SecureGroovyScript myScript

        if (scriptType == 'script' || scriptType == '') {
            if (secureScript.script != '') {
                myScript = secureScript
            } else {
                def result = [:]
                result['default'] = comb
                return result
            }
        } else if (! Jenkins.instance.getDescriptor(this.class).secureOnly) {
            List<ClasspathEntry> cp = []

            def scriptInFile = new WorkspaceFileReader(scriptFile).scriptFile.text
            myScript = new SecureGroovyScript(scriptInFile, false, cp).configuring(ApprovalContext.create())
            myScript.configuringWithKeyItem()
        } else {
            throw new GroovyScriptInFileException('')
        }

        scriptRunner = new ScriptRunner(execution, myScript)
        def ret = scriptRunner.run(comb)

        ret
    }

    @SuppressWarnings('UnusedPrivateMethod')
    private Object readResolve() {
        if (script != null) {
            List<ClasspathEntry> cp = []

            secureScript = new SecureGroovyScript(script, false, cp).configuring(ApprovalContext.create())
            script = null
        }
        this
    }

    @Extension
    static class DescriptorImpl extends MatrixExecutionStrategyDescriptor {
        final String displayName = 'Groovy Script Matrix Executor Strategy'

        boolean secureOnly = false

        DescriptorImpl() {
            load()
        }

        @Override
        boolean configure(StaplerRequest req, JSONObject formData) throws Descriptor.FormException {
            // To persist global configuration information,
            // set that to properties and call save().
            req.bindJSON(this, formData)
            save()
            true
        }
    }
}
