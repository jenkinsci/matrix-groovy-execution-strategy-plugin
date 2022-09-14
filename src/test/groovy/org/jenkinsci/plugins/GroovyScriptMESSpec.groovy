package org.jenkinsci.plugins

import hudson.Launcher
import hudson.matrix.AxisList
import hudson.matrix.MatrixProject
import hudson.matrix.TextAxis
import hudson.model.AbstractBuild
import hudson.model.BuildListener
import org.junit.ClassRule
import org.jvnet.hudson.test.JenkinsRule
import org.jvnet.hudson.test.TestBuilder
import spock.lang.Shared
import spock.lang.Specification
import org.junit.Rule

import org.jenkinsci.plugins.scriptsecurity.sandbox.groovy.SecureGroovyScript

class GroovyScriptMESSpec extends Specification {

    //@Shared
    //@ClassRule
    @Rule
    JenkinsRule rule = new JenkinsRule()

    MatrixProject configure(Boolean failAX=false) {
        def matrixProject = rule.jenkins.createProject(MatrixProject, 'test1');

        def axis = new TextAxis('axis1', ['a', 'b', 'c'])
        def axis2 = new TextAxis('axis2', ['x', 'y', 'z'])
        def axl = new AxisList()

        axl << axis
        axl << axis2

        matrixProject.setAxes(axl)
        if(failAX)
            matrixProject.getBuildersList().add(new TestBuilder() {
                @Override public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
                    if( build.project.combination.get('axis1') == 'a' &&
                        build.project.combination.get('axis2') == 'x' )
                        return false
                    else
                        return true
                }
            })
        matrixProject
    }

    def script = """
            combinations.each{

             if(it.axis2 == 'z')
                   return

             if(it.axis1 == 'b')
                   return

             result[it.axis2] = result[it.axis2] ?: []
             result[it.axis2] << it
            }

            result
        """

    def scriptCont = """
            combinations.each{

             if(it.axis2 == 'z')
                   return

             if(it.axis1 == 'b')
                   return

             result[it.axis2] = result[it.axis2] ?: []
             result[it.axis2] << it
            }

            [result, true]
        """
    def 'two axis success script'() {

        given:

        def matrixProject = configure()
        matrixProject.executionStrategy = new GroovyScriptMES(new SecureGroovyScript(script, false), '', 'script', false)

        when:
        def build = matrixProject.scheduleBuild2(0).get()

        then:

        build.logFile.text.contains('SUCCESS')
        build.runs.every { it.logFile.text.contains('SUCCESS') }
        build.runs.size() == 4
    }

    def 'two axis success no script'() {

        given:

        def matrixProject = configure()
        matrixProject.executionStrategy = new GroovyScriptMES(new SecureGroovyScript('', false), '', 'script', false)

        when:
        def build = matrixProject.scheduleBuild2(0).get()

        then:

        build.logFile.text.contains('SUCCESS')
        build.runs.every { it.logFile.text.contains('SUCCESS') }
        build.runs.size() == 9
    }

    def 'two axis fail script'() {

        given:

        def matrixProject = configure(true)
        matrixProject.executionStrategy = new GroovyScriptMES(new SecureGroovyScript(script, false), '', 'script', false)

        when:
        def build = matrixProject.scheduleBuild2(0).get()

        then:

        build.logFile.text.contains('FAILURE')
        build.runs.count { it.logFile.text.contains('FAILURE') } == 1
        build.runs.count { it.logFile.text.contains('SUCCESS') } == 1
        build.runs.size() == 2
    }

    def 'two axis fail script carry on'() {

        given:

        def matrixProject = configure(true)
        matrixProject.executionStrategy = new GroovyScriptMES(new SecureGroovyScript(scriptCont, false), '', 'script', false)

        when:
        def build = matrixProject.scheduleBuild2(0).get()

        then:

        build.logFile.text.contains('FAILURE')
        build.runs.count { it.logFile.text.contains('FAILURE') } == 1
        build.runs.count { it.logFile.text.contains('SUCCESS') } == 3
        build.runs.size() == 4
    }

    def 'two axis fail no script'() {

        given:

        def matrixProject = configure(true)
        matrixProject.executionStrategy = new GroovyScriptMES(new SecureGroovyScript('', false), '', 'script', false)

        when:
        def build = matrixProject.scheduleBuild2(0).get()

        then:

        build.logFile.text.contains('FAILURE')
        build.runs.count { it.logFile.text.contains('FAILURE') } == 1
        build.runs.count { it.logFile.text.contains('SUCCESS') } == 8
        build.runs.size() == 9
    }
}
