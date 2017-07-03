package org.jenkinsci.plugins

import jenkins.model.Jenkins
import hudson.matrix.Combination
import hudson.model.OneOffExecutor
import hudson.matrix.MatrixBuild
import hudson.matrix.MatrixBuild.MatrixBuildExecution
import org.jenkinsci.plugins.scriptsecurity.sandbox.groovy.SecureGroovyScript
import hudson.model.ParametersAction
import org.jenkinsci.plugins.envinject.EnvInjectPluginAction

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

    def run( List<Combination> c) {

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

        def p = build?.actions.find { it instanceof ParametersAction }?.parameters
        p.each {
            def paramValue = resolver.resolve(it.name)

            parameters[it.name] = paramValue.value
        }
        binding.setVariable('parameters', parameters)

        //only one of these at most
        //only if envinject is here and it is in use
        //otherwise use the environment
        def env = [:]

        if (Jenkins.instance.getPlugin('envinject')) {
            def e = build?.actions.find { it instanceof EnvInjectPluginAction }?.envMap

            if (e) {
                env = e
            } else {
                env = System.getenv()
            }
        } else {
            env = System.getenv()
        }

        binding.setVariable('env', env )

        def res = script.evaluate(getClass().classLoader, binding)
        res
    }
}
