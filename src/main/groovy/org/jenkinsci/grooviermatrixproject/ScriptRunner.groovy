package org.jenkinsci.grooviermatrixproject

import hudson.Util
import jenkins.model.Jenkins
import hudson.matrix.Combination
import hudson.model.OneOffExecutor
import hudson.matrix.MatrixBuild
import hudson.matrix.MatrixBuild.MatrixBuildExecution

/**
 * Created by jeremymarshall on 10/10/2014.
 */
class ScriptRunner {

    final GroovyShell shell = new GroovyShell(Jenkins.instance.pluginManager.uberClassLoader)
    Script compiledScript
    String workspace
    MatrixBuild.MatrixBuildExecution execution

    ScriptRunner(MatrixBuild.MatrixBuildExecution execution) {
        OneOffExecutor thr = Thread.currentThread()
        this.workspace = thr.getCurrentWorkspace()
        this.execution = execution
    }

    ScriptRunner(MatrixBuild.MatrixBuildExecution execution, String script) {
        this(execution)
        compiledScript = shell.parse(script)
    }

    ScriptRunner(MatrixBuild.MatrixBuildExecution execution, File scriptFile) {
        this(execution)
        String script
        if( scriptFile.isAbsolute() ) {
            script = Util.loadFile(scriptFile)
        } else {
            script = Util.loadFile( new File(workspace + File.separator + scriptFile.path) )
        }
        compiledScript = shell.parse(script)
    }

    //@SuppressWarnings('InsecureRandom')
    Map run( List<Combination> c) {

        c.sort { Math.random() }

        Binding binding = new Binding()
        binding.setVariable('jenkins', Jenkins.instance)
        binding.setVariable('combinations', c)
        binding.setVariable('workspace', this.workspace)
        binding.setVariable('execution', this.execution)
        binding.setVariable('result', new TreeMap<String, List<Combination>>())

        compiledScript.setBinding(binding)

        def res = compiledScript.run()
        res
    }
}
