package org.jenkinsci.grooviermatrixproject

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

    ScriptRunner(MatrixBuild.MatrixBuildExecution execution, Reader script) {
        this.execution = execution

        OneOffExecutor thr = Thread.currentThread()
        this.workspace = thr.currentWorkspace

        this.compiledScript = shell.parse(script.text)
    }

    @SuppressWarnings('InsecureRandom')
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
