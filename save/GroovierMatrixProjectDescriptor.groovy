package org.jenkinsci.grooviermatrixproject

import hudson.init.InitMilestone
import hudson.init.Initializer
import hudson.matrix.Axis
import hudson.matrix.AxisDescriptor
import hudson.matrix.MatrixExecutionStrategyDescriptor
import hudson.matrix.MatrixProject
import hudson.model.AbstractProject.AbstractProjectDescriptor
import hudson.model.ItemGroup
import hudson.model.Items
import hudson.model.Run
import hudson.tools.Messages
import jenkins.scm.SCMCheckoutStrategyDescriptor

/**
 * Created by jeremymarshall on 8/10/2014.
 */
class GroovierMatrixProjectDescriptor extends AbstractProjectDescriptor {

    public String getDisplayName() {
        return 'Groovier Matrix Project'
    }

    GroovierMatrixProject newInstance(ItemGroup parent, String name) {
        return new GroovierMatrixProject(parent, name)
    }

    /**
     * All {@link AxisDescriptor}s that contribute to the UI.
     */
    List<AxisDescriptor> getAxisDescriptors() {
        List<AxisDescriptor> r = new ArrayList<AxisDescriptor>()
        for (AxisDescriptor d : Axis.all()) {
            if (d.isInstantiable())
                r.add(d)
        }
        return r;
    }

    List<GroovyExecutionStrategyDescriptor> getExecutionStrategyDescriptors() {
        return GroovyExecutionStrategyDescriptor.all()
    }

    List<SCMCheckoutStrategyDescriptor> getMatrixRunCheckoutStrategyDescriptors() {
        return SCMCheckoutStrategyDescriptor.all()
    }

    @Initializer(before = InitMilestone.EXTENSIONS_AUGMENTED)
    public static void alias() {
        Items.XSTREAM.alias("matrix-project", GroovierMatrixProject.class);
    }
}
