module net.alterorb.patcher {
    requires org.slf4j;
    requires org.objectweb.asm.tree;
    requires jopt.simple;
    requires okio;
    requires com.fasterxml.jackson.databind;
    opens net.alterorb.patcher to com.fasterxml.jackson.databind;
}