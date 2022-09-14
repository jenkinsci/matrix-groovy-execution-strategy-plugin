package org.jenkinsci.plugins.GroovyScriptMES
/*
This Groovy script is used to produce the global configuration option.

Jenkins uses a set of tag libraries to provide uniformity in forms.
To determine where this tag is defined, first check the namespace URI,
and then look under $JENKINS/views/. For example, section() is defined
in $JENKINS/views/lib/form/section.jelly.

It's also often useful to just check other similar scripts to see what
tags they use. Views are always organized according to its owner class,
so it should be straightforward to find them.
*/
namespace(lib.FormTagLib).with {

    if(descriptor.getSecureOnly()) {
        property(field = 'secureScript')
        invisibleEntry {
            input( name: "scriptFile", value: "", type: "hidden")
            input( name: "scriptType", value: "script", type: "hidden")
            input( name: "sandbox", value: false, type: "hidden")
        }
    } else {
        radioBlock(name: 'scriptType', title: 'Groovy Script', value: 'script', checked = instance ? instance.scriptType.equals('script') : true, inline: true) {
            property(field = 'secureScript')
        }
        radioBlock(name: 'scriptType', title: 'Groovy File', value: 'file', checked = instance?.scriptType.equals('file'), inline: true) {
            entry(title: _("Groovy File"), field: "scriptFile") {
                textbox()
            }
            entry(title: _("Use Groovy Sandbox"), field: "sandbox") {
                checkbox()
            }
        }
    }
}