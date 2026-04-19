///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 25+
//DEPS dev.tamboui:tamboui-core:LATEST
//DEPS dev.tamboui:tamboui-tui:LATEST
//DEPS dev.tamboui:tamboui-toolkit:LATEST
//DEPS dev.tamboui:tamboui-widgets:LATEST
//DEPS dev.tamboui:tamboui-annotations:LATEST
//DEPS dev.tamboui:tamboui-css:LATEST
//DEPS dev.tamboui:tamboui-tfx:LATEST
//DEPS dev.tamboui:tamboui-tfx-tui:LATEST
//DEPS dev.tamboui:tamboui-tfx-toolkit:LATEST
//DEPS dev.tamboui:tamboui-jline3-backend:LATEST
//SOURCES snippets/**/*.java

/**
 * Compiles every Java snippet referenced by the slide deck via
 * asciidoctor {@code include::} directives. Running this script
 * (or {@code jbang build verify-snippets.java}) guarantees that
 * every code sample shown on stage actually compiles against the
 * real TamboUI APIs.
 */
void main() {
        System.out.println("All slide snippets compiled successfully.");
}

