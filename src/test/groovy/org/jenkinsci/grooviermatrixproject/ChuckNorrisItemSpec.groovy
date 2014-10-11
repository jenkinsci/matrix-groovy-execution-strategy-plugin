package org.jenkinsci.grooviermatrixproject

import spock.lang.Specification

class ChuckNorrisItemSpec extends Specification {

    def 'Create Param'() {

        def cni = new ChuckNorrisItem('Chuck Norris eats spock tests for breakfast')

        expect:
        cni.quote.matches('Chuck Norris eats spock tests for breakfast')
    }

    def 'Create No Param'() {

        def cni = new ChuckNorrisItem()

        expect:
        cni.quote.matches('Chuck Norris is his own DataBoundConstructor')
    }
}
