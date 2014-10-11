package org.jenkinsci.grooviermatrixproject

import jenkins.model.Jenkins
import org.jvnet.hudson.test.JenkinsRule
import org.junit.Rule
import spock.lang.Shared
import spock.lang.Specification

class ChuckNorrisContainerSpec extends Specification {

    @Rule
    JenkinsRule rule = new JenkinsRule()

    @Shared chuckNorrisAxisDescriptor
    @Shared chuckNorrisContainerDescriptor

    void configure(chuckNorrisPluginHandler, factCount) {

        if (chuckNorrisAxisDescriptor == null) {
            chuckNorrisAxisDescriptor = Jenkins.instance.getDescriptorOrDie(ChuckNorrisAxis)
         }

        if (chuckNorrisContainerDescriptor == null) {
            chuckNorrisContainerDescriptor = Jenkins.instance.getDescriptorOrDie(ChuckNorrisContainer)
        }

        chuckNorrisAxisDescriptor.chuckNorrisPluginHandler = chuckNorrisPluginHandler
        chuckNorrisAxisDescriptor.count = factCount
    }

    def 'Build'() {
        given:
        def chuckNorrisPluginHandler = Mock(IFact)
        chuckNorrisPluginHandler.installed() >> true
        3 * chuckNorrisPluginHandler.fact >> 'Chuck Norris is never Mocked'

        configure(chuckNorrisPluginHandler, 3)

        def cdc = new ChuckNorrisContainer(chuckNorrisContainerDescriptor.loadDefaultItems())

        expect:
        cdc.items.size() == 3
    }
}
