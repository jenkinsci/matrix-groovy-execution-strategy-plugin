package org.jenkinsci.plugins

import hudson.model.OneOffExecutor

/**
 * Created by jeremymarshall on 22/10/14.
 */
class WorkspaceFileReader {

    @Delegate Reader scriptFile
    String workspace

    WorkspaceFileReader() {
        OneOffExecutor thr = Thread.currentThread()
        this.workspace = thr.currentWorkspace
    }

    WorkspaceFileReader(String file) {
        this()
        File f = new File(file)

        if (f.isAbsolute()) {
            scriptFile = new FileReader(file)
        } else {
            scriptFile = new FileReader(workspace + File.separator + file)
        }
    }
}
