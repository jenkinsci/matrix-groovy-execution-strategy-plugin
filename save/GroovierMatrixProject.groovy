package org.jenkinsci.grooviermatrixproject

import groovy.transform.InheritConstructors
import hudson.Extension
import hudson.matrix.MatrixBuild
import hudson.matrix.MatrixProject
import hudson.model.ItemGroup
import jenkins.model.Jenkins

/**
 * Created by jeremymarshall on 8/10/2014.
 */


class GroovierMatrixProject extends MatrixProject   {

    GroovierMatrixProject(String name) {
        this(Jenkins.instance, name)
    }

    GroovierMatrixProject(ItemGroup parent, String name) {
        super(parent, name)
    }

    @Extension
    static final class DescriptorImpl extends GroovierMatrixProjectDescriptor {
    }
}
