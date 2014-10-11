package org.jenkinsci.grooviermatrixproject

import spock.lang.Specification

class ChuckNorrisItemROSpec extends Specification {

    def 'Create Param'() {

        def cni = new ChuckNorrisItemRO('Chuck Norris passes every test')

        expect:
        cni.quote.matches('Chuck Norris passes every test')
    }
}
