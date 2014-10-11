package org.jenkinsci.grooviermatrixproject

import jenkins.model.Jenkins
import hudson.matrix.Combination

/**
 * Created by jeremymarshall on 10/10/2014.
 */
class ScriptRunner {

    final GroovyShell SHELL = new GroovyShell(Jenkins.instance.pluginManager.uberClassLoader)
    Script compiledScript

    ScriptRunner(String script) {
        compiledScript = SHELL.parse(script)
    }

    boolean run( Combination c) {

        Binding binding = new Binding()
        //binding.setVariable('current', current)
        //binding.setVariable('selected', selected)

        compiledScript.setBinding(binding)

        boolean differentEnough = compiledScript.run()
    }
}
