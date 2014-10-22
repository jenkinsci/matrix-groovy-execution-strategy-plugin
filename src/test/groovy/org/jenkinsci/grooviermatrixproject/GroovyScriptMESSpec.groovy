package org.jenkinsci.grooviermatrixproject

import hudson.matrix.AxisList
import hudson.matrix.MatrixProject
import hudson.matrix.TextAxis
import spock.lang.Specification
import org.junit.Rule
import org.jvnet.hudson.test.GroovyJenkinsRule

class GroovyScriptMESSpec extends Specification {

    @Rule
    GroovyJenkinsRule rule = new GroovyJenkinsRule()

    MatrixProject configure() {

        def matrixProject = rule.createMatrixProject()

        def axis = new TextAxis('axis1', ['a', 'b', 'c'])
        def axis2 = new TextAxis('axis2', ['x', 'y', 'z'])
        def axl = new AxisList()

        axl << axis
        axl << axis2

        matrixProject.setAxes(axl)

        matrixProject
    }

    def 'two axis script'() {

        given:

        def script = """\
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

        def matrixProject = configure()
        matrixProject.executionStrategy = new GroovyScriptMES(script, '', 'script')

        when:
        def build = matrixProject.scheduleBuild2(0).get()

        then:

        build.logFile.text.contains('SUCCESS')
        build.runs.every { it.logFile.text.contains('SUCCESS') }
        build.runs.size() == 4
    }

}
