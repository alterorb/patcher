module net.alterorb.patcher {
    requires java.net.http;
    requires org.slf4j;
    requires org.objectweb.asm.tree;
    requires jopt.simple;
    requires com.fasterxml.jackson.databind;
    opens net.alterorb.patcher to com.fasterxml.jackson.databind;
}