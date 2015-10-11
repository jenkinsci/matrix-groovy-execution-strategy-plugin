package org.jenkinsci.plugins

/**
 * Created by jeremymarshall on 11/10/2015.
 */
class GroovyScriptInFileException extends Exception {
    GroovyScriptInFileException() {
        this.message = 'File based matrix groovy execution script not allowed'
    }
}
