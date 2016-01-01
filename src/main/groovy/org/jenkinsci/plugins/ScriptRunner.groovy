package org.jenkinsci.plugins

import jenkins.model.Jenkins
import hudson.matrix.Combination
import hudson.model.OneOffExecutor
import hudson.matrix.MatrixBuild
import hudson.matrix.MatrixBuild.MatrixBuildExecution
import org.jenkinsci.plugins.scriptsecurity.sandbox.groovy.SecureGroovyScript
import hudson.model.ParametersAction

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

    Map run( List<Combination> c) {

        Collections.shuffle(c)

        Binding binding = new Binding()
        binding.setVariable('jenkins', Jenkins.instance)
        binding.setVariable('combinations', c)
        binding.setVariable('workspace', this.workspace)
        binding.setVariable('execution', this.execution)
        binding.setVariable('result', new TreeMap<String, List<Combination>>())
        binding.setVariable('out', this.execution.listener.logger)


        def parameters = [:]
        def build = this.execution.build
        def resolver = build.buildVariableResolver

        def p = build?.actions.find{ it instanceof ParametersAction }?.parameters
        p.each {

            def param_value = resolver.resolve(it.name)

            parameters[it.name] = param_value.value
            //println "parameter ${it.name}:"
            //println it.dump()
        }

        binding.setVariable('parameters', parameters)
        binding.setVariable('env', System.getenv())

        def res = script.evaluate(getClass().classLoader, binding)
        res
    }
}
