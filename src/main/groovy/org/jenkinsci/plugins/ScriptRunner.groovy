package org.jenkinsci.plugins

import jenkins.model.Jenkins
import hudson.matrix.Combination
import hudson.model.OneOffExecutor
import hudson.matrix.MatrixBuild
import hudson.matrix.MatrixBuild.MatrixBuildExecution
import org.jenkinsci.plugins.scriptsecurity.sandbox.groovy.SecureGroovyScript
/**
 * Created by jeremymarshall on 10/10/2014.
 */
class ScriptRunner {

    String workspace
    MatrixBuild.MatrixBuildExecution execution
    SecureGroovyScript script

    ScriptRunner(MatrixBuild.MatrixBuildExecution execution, SecureGroovyScript script) {
        this.execution = execution
        this.script = script
        OneOffExecutor thr = Thread.currentThread()
        this.workspace = thr.currentWorkspace
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

        def res = script.evaluate(getClass().getClassLoader(), binding);
        res
    }
}
