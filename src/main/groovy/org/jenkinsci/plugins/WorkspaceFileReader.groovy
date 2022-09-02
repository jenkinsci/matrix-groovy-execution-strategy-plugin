package org.jenkinsci.plugins

import hudson.model.OneOffExecutor
import hudson.FilePath;

/**
 * Created by jeremymarshall on 22/10/14.
 */
class WorkspaceFileReader {

    String scriptContent
    String workspace
    hudson.remoting.VirtualChannel channel

    WorkspaceFileReader() {
        OneOffExecutor thr = Thread.currentThread()
        this.workspace = thr.currentWorkspace
        this.channel = thr.getOwner().getChannel()
    }

    WorkspaceFileReader(String file) {
        this()
        File f = new File(file)

        String path
        if (f.isAbsolute()) {
            path = file
        } else {
            path = workspace + File.separator + file
        }
        this.scriptContent = new FilePath(this.channel, path).readToString()
    }
}
